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
public class JBossCliMetricDefinitionTest extends MetricDefinitionTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(JBossCliMetricDefinitionTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // getInstance() ---------------------------------------------------------------------------------------------------

    @Test
    public void getInstance_JBoss_NoHostNoPort() throws Exception {

        String s = "jboss:/a=b/c=d/f";

        JBossCliMetricDefinition d = (JBossCliMetricDefinition)MetricDefinition.getInstance(s);

        CliControllerAddress cliControllerAddress = d.getControllerAddress();
        assertEquals(CliControllerAddress.DEFAULT_HOST, cliControllerAddress.getHost());
        assertEquals(CliControllerAddress.DEFAULT_PORT, cliControllerAddress.getPort());

        CliPath path = d.getPathInstance();

        assertEquals("/a=b/c=d", path.getPath());
        assertEquals("/a=b/c=d", d.getPath());

        CliAttribute attribute = d.getAttribute();
        assertEquals("f", attribute.getName());

        assertEquals("/a=b/c=d/f", d.getSimpleLabel());
        assertEquals("/a=b/c=d/f", d.getDescription());
    }

    @Test
    public void getInstance_JBoss_LocalhostNoPort() throws Exception {

        String s = "jboss:localhost/a=b/c=d/f";

        JBossCliMetricDefinition d = (JBossCliMetricDefinition)MetricDefinition.getInstance(s);

        CliControllerAddress cliControllerAddress = d.getControllerAddress();
        assertEquals("localhost", cliControllerAddress.getHost());
        assertEquals(CliControllerAddress.DEFAULT_PORT, cliControllerAddress.getPort());

        CliPath path = d.getPathInstance();

        assertEquals("/a=b/c=d", path.getPath());
        assertEquals("/a=b/c=d", d.getPath());

        CliAttribute attribute = d.getAttribute();
        assertEquals("f", attribute.getName());

        assertEquals("/a=b/c=d/f", d.getSimpleLabel());
        assertEquals("/a=b/c=d/f", d.getDescription());
    }

    @Test
    public void getInstance_JBoss_HostNoPort() throws Exception {

        String s = "jboss:blue/a=b/c=d/f";

        JBossCliMetricDefinition d = (JBossCliMetricDefinition)MetricDefinition.getInstance(s);

        CliControllerAddress cliControllerAddress = d.getControllerAddress();
        assertEquals("blue", cliControllerAddress.getHost());
        assertEquals(CliControllerAddress.DEFAULT_PORT, cliControllerAddress.getPort());

        CliPath path = d.getPathInstance();

        assertEquals("/a=b/c=d", path.getPath());
        assertEquals("/a=b/c=d", d.getPath());

        CliAttribute attribute = d.getAttribute();
        assertEquals("f", attribute.getName());

        assertEquals("/a=b/c=d/f", d.getSimpleLabel());
        assertEquals("/a=b/c=d/f", d.getDescription());
    }

    @Test
    public void getInstance_JBoss_LocalhostAndPort() throws Exception {

        String s = "jboss:localhost:9999/a=b/c=d/f";

        JBossCliMetricDefinition d = (JBossCliMetricDefinition)MetricDefinition.getInstance(s);

        CliControllerAddress cliControllerAddress = d.getControllerAddress();
        assertEquals("localhost", cliControllerAddress.getHost());
        assertEquals(9999, cliControllerAddress.getPort());

        CliPath path = d.getPathInstance();

        assertEquals("/a=b/c=d", path.getPath());
        assertEquals("/a=b/c=d", d.getPath());

        CliAttribute attribute = d.getAttribute();
        assertEquals("f", attribute.getName());

        assertEquals("/a=b/c=d/f", d.getSimpleLabel());
        assertEquals("/a=b/c=d/f", d.getDescription());
    }

    @Test
    public void getInstance_JBoss_HostAndPort() throws Exception {

        String s = "jboss:blue:9999/a=b/c=d/f";
        JBossCliMetricDefinition d = (JBossCliMetricDefinition)MetricDefinition.getInstance(s);

        CliControllerAddress cliControllerAddress = d.getControllerAddress();
        assertEquals("blue", cliControllerAddress.getHost());
        assertEquals(9999, cliControllerAddress.getPort());

        CliPath path = d.getPathInstance();

        assertEquals("/a=b/c=d", path.getPath());
        assertEquals("/a=b/c=d", d.getPath());

        CliAttribute attribute = d.getAttribute();
        assertEquals("f", attribute.getName());

        assertEquals("/a=b/c=d/f", d.getSimpleLabel());
        assertEquals("/a=b/c=d/f", d.getDescription());
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
            new JBossCliMetricDefinition(null);
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
            new JBossCliMetricDefinition("something");
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid jboss CLI metric, no prefix: \"something\"", msg);
        }
    }

    @Test
    public void constructor_NoPathSeparator() throws Exception {

        try {
            new JBossCliMetricDefinition("jboss:something");
            fail("should have thrown exception");
        }
        catch(MetricDefinitionException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.startsWith("the jboss CLI metric defintion does not contain a path: \""));
        }
    }

    @Test
    public void constructor_And_Accessors() throws Exception {

        JBossCliMetricDefinition md = new JBossCliMetricDefinition("jboss:some-host:1000/a=b/c=d/f");

        CliAttribute attribute = md.getAttribute();
        assertEquals("f", attribute.getName());
        assertEquals("f", md.getAttributeName());

        CliPath pathInstance = md.getPathInstance();
        assertEquals("/a=b/c=d", pathInstance.getPath());

        String path = md.getPath();
        assertEquals("/a=b/c=d", path);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected JBossCliMetricDefinition getMetricDefinitionToTest() throws Exception {

        return new JBossCliMetricDefinition("jboss:/name");
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
