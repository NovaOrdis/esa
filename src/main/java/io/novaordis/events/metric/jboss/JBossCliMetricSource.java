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
import io.novaordis.events.metric.MetricCollectionException;
import io.novaordis.events.metric.MetricDefinition;
import io.novaordis.events.metric.source.MetricSource;
import io.novaordis.jboss.cli.JBossCliException;
import io.novaordis.jboss.cli.JBossControllerClient;
import io.novaordis.jboss.cli.JBossControllerClientImpl;
import io.novaordis.utilities.os.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/31/16
 */
public class JBossCliMetricSource implements MetricSource {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(JBossCliMetricSource.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private JBossControllerClient controllerClient;

    // Constructors ----------------------------------------------------------------------------------------------------

    public JBossCliMetricSource() throws Exception {

        this.controllerClient = new JBossControllerClientImpl();
    }

    // MetricSource implementation -------------------------------------------------------------------------------------

    @Override
    public List<Property> collectMetrics(OS os) throws MetricCollectionException {

        //
        // this method is not used (yet) for a JBoss CLI controller, so we're not implementing it. When we need it,
        // we'll implement it
        //
        throw new RuntimeException("collectMetrics(" + os + ") NOT YET IMPLEMENTED");
    }

    @Override
    public List<Property> collectMetrics(List<MetricDefinition> metricDefinitions, OS os)
            throws MetricCollectionException {

        List<Property> properties = new ArrayList<>();

        for(MetricDefinition d: metricDefinitions) {

            if (!(d instanceof JBossCliMetricDefinition)) {
                throw new MetricCollectionException("RETURN HERE");
            }

            JBossCliMetricDefinition jbmd = (JBossCliMetricDefinition)d;

            String path = jbmd.getPath();
            String attributeName = jbmd.getAttributeName();
            String attributeValue = null;


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
                    continue;

                }

                //
                // TODO - disconnect
                //
            }

            try {

                attributeValue = controllerClient.getAttributeValue(path, attributeName);
            }
            catch (JBossCliException e) {

                log.warn(e.getMessage());
            }

            Property p = null;

            if (attributeValue != null) {

                //
                // figure out how I converted Strings to Properties for top, and document that in the MetricDefintion doc.
                //

                p = new IntegerProperty("xxx");

            }

            properties.add(p);
        }

        return properties;
    }

    // Public ----------------------------------------------------------------------------------------------------------
//
//    public void connect() throws Exception {
//
//        controllerClient.connect();
//    }
//
//    public void disconnect() {
//
//        controllerClient.disconnect();
//    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
