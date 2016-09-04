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

import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.StringProperty;
import io.novaordis.events.metric.MetricCollectionException;
import io.novaordis.events.metric.MetricDefinition;
import io.novaordis.events.metric.source.MetricSource;
import io.novaordis.jboss.cli.JBossCliException;
import io.novaordis.jboss.cli.JBossControllerClient;
import io.novaordis.jboss.cli.model.JBossControllerAddress;
import io.novaordis.utilities.os.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a JBoss controller, bound to a specific host:port and accessible using a certain user account.
 *
 * A JBossCliMetricSource instance is equal with another JBossCliMetricSource instance if those instances have the
 * same hosts, ports and user.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/31/16
 */
public class JBossCliMetricSource implements MetricSource {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(JBossCliMetricSource.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private JBossControllerAddress controllerAddress;

    //
    // lazily instantiated and connected
    //
    private JBossControllerClient controllerClient;

    // Constructors ----------------------------------------------------------------------------------------------------

    public JBossCliMetricSource(JBossControllerAddress controllerAddress) {

        this.controllerAddress = controllerAddress;
    }

    /**
     * Uses the default controller address ("localhost:9999")
     */
    public JBossCliMetricSource() {

        this(new JBossControllerAddress());
    }

    // MetricSource implementation -------------------------------------------------------------------------------------

    @Override
    public List<Property> collectAllMetrics(OS os) throws MetricCollectionException {

        //
        // this method won't be used with a JBoss CLI metric source, we need to pull individual metrics, so always
        // return an empty list.
        //

        return Collections.emptyList();
    }

    @Override
    public List<Property> collectMetrics(List<MetricDefinition> metricDefinitions) throws MetricCollectionException {

        List<Property> properties = new ArrayList<>();

        for(MetricDefinition md : metricDefinitions) {

            if (!(md instanceof JBossCliMetricDefinition)) {

                log.debug(this + " does not handle non-jboss CLI metrics " + md);
                properties.add(null);
                continue;
            }

            JBossCliMetricDefinition jbmd = (JBossCliMetricDefinition) md;

            JBossCliMetricSource thatSource = jbmd.getSource();

            if (!this.equals(thatSource)) {

                log.debug(jbmd + " has a different source than " + this + ", ignorning ...");
                properties.add(null);
                continue;
            }

            //
            // lazy instantiation
            //

            if (controllerClient == null) {

                setControllerClient(JBossControllerClient.getInstance());
            }

            //
            // if the client is not connected, attempt to connect it, every time we collect metrics. This is useful if
            // the JBoss instance is started after os-stats, or if the JBoss instance becomes inaccessible and then
            // reappears
            //

            if (!controllerClient.isConnected()) {

                try {

                    log.debug("attempting to connect " + controllerClient);

                    controllerClient.connect();
                }
                catch(Exception e) {

                    log.warn(e.getMessage());
                    log.debug("controller client connection failure", e);
                    continue;
                }
            }

            String path = jbmd.getPath();
            String attributeName = jbmd.getAttributeName();
            Object attributeValue = null;

            try {

                attributeValue = controllerClient.getAttributeValue(path, attributeName);
            }
            catch (JBossCliException e) {

                log.warn(e.getMessage());
            }

            if (attributeValue == null) {
                properties.add(null);
                continue;
            }

            String name = jbmd.getName();
            Property p;

            if (attributeValue instanceof String) {

                p = new StringProperty(name, (String)attributeValue);
            }
            else if (attributeValue instanceof Integer) {

                p = new IntegerProperty(name, (Integer)attributeValue);
            }
            else {
                throw new RuntimeException(attributeValue.getClass() + " SUPPORT NOT YET IMPLEMENTED");
            }

            properties.add(p);
        }

        return properties;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public JBossControllerAddress getControllerAddress() {
        return controllerAddress;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (controllerAddress == null) {
            return false;
        }

        if (!(o instanceof JBossCliMetricSource)) {
            return false;
        }

        JBossCliMetricSource that = (JBossCliMetricSource)o;

        return controllerAddress.equals(that.controllerAddress);
    }

    @Override
    public int hashCode() {

        if (controllerAddress == null) {
            return 0;
        }

        return 7 + 11 * controllerAddress.hashCode();
    }

    @Override
    public String toString() {

        return controllerAddress == null ? "null" : controllerAddress.toString();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void setControllerClient(JBossControllerClient controllerClient) {

        log.debug(this + " sets the controller client " + controllerClient);
        this.controllerClient = controllerClient;

        if(controllerClient != null) {
            this.controllerAddress = controllerClient.getControllerAddress();
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
