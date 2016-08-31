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

import io.novaordis.events.metric.MetricDefinition;
import io.novaordis.events.metric.MetricDefinitionException;
import io.novaordis.events.metric.MetricDefinitionTest;
import io.novaordis.utilities.UserErrorException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public class JBossCLIMetricDefinitionTest extends MetricDefinitionTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(JBossCLIMetricDefinitionTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // getInstance() ---------------------------------------------------------------------------------------------------

    @Test
    public void getInstance_JBoss_NoHostNoPort() throws Exception {

        JBossCLIMetricDefinition d = (JBossCLIMetricDefinition)MetricDefinition.getInstance(
                "jboss:/a=b/c=d/f");

        fail("return here: " + d);
    }

    @Test
    public void getInstance_JBoss_LocalhostNoPort() throws Exception {

        JBossCLIMetricDefinition d = (JBossCLIMetricDefinition)MetricDefinition.getInstance(
                "jboss:localhost/a=b/c=d/f");

        fail("return here: " + d);

    }

    @Test
    public void getInstance_JBoss_HostNoPort() throws Exception {

        JBossCLIMetricDefinition d = (JBossCLIMetricDefinition)MetricDefinition.getInstance(
                "jboss:blue/a=b/c=d/f");

        fail("return here: " + d);

    }

    @Test
    public void getInstance_JBoss_LocalhostAndPort() throws Exception {

        JBossCLIMetricDefinition d = (JBossCLIMetricDefinition)MetricDefinition.getInstance(
                "jboss:localhost:9999/a=b/c=d/f");

        fail("return here: " + d);

    }

    @Test
    public void getInstance_JBoss_HostAndPort() throws Exception {

        JBossCLIMetricDefinition d = (JBossCLIMetricDefinition)MetricDefinition.getInstance(
                "jboss:blue:9999/a=b/c=d/f");

        fail("return here: " + d);

    }

    @Test
    public void getInstance_InvalidMetricDefinition() throws Exception {

        try {
            MetricDefinition.getInstance("jboss:this-should-fail");
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            MetricDefinitionException cause = (MetricDefinitionException)e.getCause();
            assertNotNull(cause);
            log.info(msg);
            assertTrue(msg.startsWith("invalid jboss metric definition: "));
        }
    }

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_NullArgument() throws Exception {

        try {
            new JBossCLIMetricDefinition(null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("null jboss CLI metric definition", msg);
        }
    }

    @Test
    public void constructor_NoPrefix() throws Exception {

        try {
            new JBossCLIMetricDefinition("something");
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid jboss CLI metric, no prefix: \"something\"", msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected JBossCLIMetricDefinition getMetricDefinitionToTest() throws Exception {

        return new JBossCLIMetricDefinition("jboss:/name");
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
