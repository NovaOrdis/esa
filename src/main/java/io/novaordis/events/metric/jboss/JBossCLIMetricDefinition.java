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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/31/16
 */
public class JBossCliMetricDefinition extends MetricDefinitionBase {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String PREFIX = "jboss:";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private CliControllerAddress controller;
    private CliPath path;
    private CliAttribute attribute;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
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
    }

    // MetricDefinitionBase overrides ----------------------------------------------------------------------------------

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

    // Public ----------------------------------------------------------------------------------------------------------

    public CliControllerAddress getControllerAddress() {
        return controller;
    }

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

    @Override
    public String toString() {

        return PREFIX + controller + ":" + path + "/" + attribute;
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
                    "the jboss CLI metric defintion does not contain a path: \"" + definition + "\"");
        }

        String pathAndAttribute;

        if (i != 0) {

            //
            // controller address
            //

            this.controller = new CliControllerAddress(definition.substring(0, i));
            pathAndAttribute = definition.substring(i);

        }
        else {

            this.controller = CliControllerAddress.DEFAULT_CONTROLLER;
            pathAndAttribute = definition;
        }

        i = pathAndAttribute.lastIndexOf('/');

        this.path = new CliPath(pathAndAttribute.substring(0, i));
        this.attribute = new CliAttribute(pathAndAttribute.substring(i + 1));
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
