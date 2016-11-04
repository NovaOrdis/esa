/*
 * Copyright (c) 2016 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.events.metric.jboss;

import io.novaordis.events.core.event.MeasureUnit;
import io.novaordis.events.metric.MetricDefinitionBase;
import io.novaordis.events.metric.MetricDefinitionException;
import io.novaordis.events.metric.source.MetricSource;
import io.novaordis.jboss.cli.JBossCliException;
import io.novaordis.jboss.cli.model.JBossControllerAddress;

import java.util.Collections;
import java.util.List;

/**
 * A metric that can be read from a JBoss controller.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/31/16
 */
public class JBossCliMetricDefinition extends MetricDefinitionBase {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String PREFIX = "jboss:";

    // Static ----------------------------------------------------------------------------------------------------------

    // Package protected static ----------------------------------------------------------------------------------------

    /**
     * @return the original name of the metric definition, the way it was specified in the string it was parsed from.
     */
    static String toLiteralName(JBossControllerAddress address, CliPath path, CliAttribute attribute) {

        String hostLiteral = address.getHostLiteral();
        String portLiteral = address.getPortLiteral();

        String name;

        if (hostLiteral != null) {

            name = hostLiteral;
        }
        else {
            name = "";
        }

        if (portLiteral != null) {

            name += ":" + portLiteral;
        }

        name += path.getPath() + "/" + attribute.getName();

        return name;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private CliPath path;
    private CliAttribute attribute;

    //
    // we preserve the original name because the underlying source (which otherwise would be the originator for
    // name) might change in subtle ways
    //
    private String name;

    //
    // a JBoss CLI metric definition has only one source, irrespective of the OS
    //

    private JBossCliMetricSource source;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param definition a string metric definition similar to
     *                   "jboss:username1:password1@localhost/subsystem=remoting/worker-task-core-threads"
     *
     * @throws MetricDefinitionException in case an invalid metric definition is encountered. The error message
     * must be human-readable, as it will most likely end up in error messages.
     * @throws IllegalArgumentException
     */
    public JBossCliMetricDefinition(String definition) throws MetricDefinitionException {

        if (definition == null) {

            throw new IllegalArgumentException("null jboss CLI metric definition");
        }

        if (!definition.startsWith(PREFIX)) {

            throw new IllegalArgumentException("invalid jboss CLI metric, no prefix: \"" + definition + "\"");
        }

        parse(definition.substring(PREFIX.length()));

        // cache the name, to "freeze" as soon as possible; the source, which otherwise would be the name originator,
        // may change in subtle ways
        this.name = toLiteralName(source.getControllerAddress(), path, attribute);
    }

    // MetricDefinitionBase overrides ----------------------------------------------------------------------------------

    @Override
    public String getName() {

        return name;
    }

    @Override
    public String getSimpleLabel() {

        //
        // it would be nice if we could come up with a human readable label - we'll see how we do that; in the mean
        // time, we'll just report path and attribute
        //

        return path.getPath() + "/" + attribute.getName();
    }

    @Override
    public MeasureUnit getMeasureUnit() {

        //
        // it would be nice if we could come up with a valid value - we'll see how we do that; in the mean
        // time, we'll just return null
        //

        return null;
    }

    @Override
    public String getDescription() {

        //
        // it would be nice if we could come up with a description - we'll see how we do that; in the mea time, we'll
        // just return the simple label
        //

        return getSimpleLabel();
    }

    @Override
    public Class getType() {
        throw new RuntimeException("getType() NOT YET IMPLEMENTED");
    }

    //
    // we need to override all source-related methods, as we override the storage
    //

    /**
     * Base override, we only have just one source.
     */
    @Override
    public List<MetricSource> getSources(String osName) {

        return Collections.singletonList(source);
    }

    @Override
    public boolean addSource(String osName, MetricSource source) {

        if (!(source instanceof JBossCliMetricSource)) {
            throw new IllegalArgumentException("the metric source not a JBossCliMetricSource");
        }

        //
        // osName does not matter, we're ignoring it, warn if we're seeing a non-null one
        //

        if (osName != null) {
            log.warn("specifying an OS name is superfluous, " + osName + " will be ignored");
        }

        this.source = (JBossCliMetricSource)source;
        return true;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getPath() {

        if (path == null) {
            return null;
        }

        return path.getPath();
    }

    public String getAttributeName() {

        if (attribute == null) {
            return null;
        }

        return attribute.getName();
    }

    /**
     * The only source, OS-independent. Never returns null.
     */
    public JBossCliMetricSource getSource() {

        return source;
    }

    @Override
    public String toString() {

        return PREFIX + source + ":" + path + "/" + attribute;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    CliAttribute getAttribute() {
        return attribute;
    }

    CliPath getPathInstance() {
        return path;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    /**
     * @param definition - without prefix
     */
    private void parse(String definition) throws MetricDefinitionException {

        int i = definition.indexOf('/');

        if (i == -1) {
            throw new MetricDefinitionException(
                    "the jboss CLI metric definition does not contain a path: \"" + definition + "\"");
        }

        JBossControllerAddress controllerAddress;
        String pathAndAttribute;

        if (i != 0) {

            //
            // controller address
            //

            try {

                controllerAddress = JBossControllerAddress.parseAddress(definition.substring(0, i));
            }
            catch(JBossCliException e) {

                throw new MetricDefinitionException("invalid jboss CLI metric definition: " + e.getMessage(), e);
            }

            pathAndAttribute = definition.substring(i);

        }
        else {

            controllerAddress = new JBossControllerAddress();
            pathAndAttribute = definition;
        }

        //
        // we don't need a map storage in the superclass, we have one source for any OS, so we simply get rid of the
        // map storage
        //
        setSources(null);
        addSource(null, new JBossCliMetricSource(controllerAddress));

        i = pathAndAttribute.lastIndexOf('/');

        this.path = new CliPath(pathAndAttribute.substring(0, i));
        this.attribute = new CliAttribute(pathAndAttribute.substring(i + 1));
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
