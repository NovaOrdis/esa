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
import io.novaordis.events.metric.MockOS;
import io.novaordis.events.metric.source.MetricSource;
import io.novaordis.events.metric.source.MockMetricSource;
import io.novaordis.jboss.cli.JBossCliException;
import io.novaordis.jboss.cli.JBossControllerClient;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.OS;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    // Overrides -------------------------------------------------------------------------------------------------------

    /**
     * The OS makes no difference for this metric definition.
     * @throws Exception
     */
    @Override
    @Test
    public void getSources_UnknownOS() throws Exception {

        MetricDefinition d = getMetricDefinitionToTest();

        List<MetricSource> sources = d.getSources(MockOS.NAME);

        assertEquals(1, sources.size());

        assertTrue(sources.contains(((JBossCliMetricDefinition) d).getSource()));
    }

    /**
     * The OS makes no difference for this metric definition.
     * @throws Exception
     */
    @Override
    @Test
    public void getSources_NullOSName() throws Exception {

        JBossCliMetricDefinition d = getMetricDefinitionToTest();

        List<MetricSource> sources = d.getSources(null);
        assertEquals(1, sources.size());
        assertEquals(sources.get(0), d.getSource());
    }

    @Override
    @Test
    public void addSource() throws Exception {

        JBossCliMetricDefinition d = getMetricDefinitionToTest();

        try {
            d.addSource(null, new MockMetricSource());
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the metric source not a JBossCliMetricSource", msg);
        }
    }

    // getInstance() ---------------------------------------------------------------------------------------------------

    @Test
    public void getInstance_JBoss_NoHostNoPort() throws Exception {

        String s = "jboss:/a=b/c=d/f";

        JBossCliMetricDefinition d = (JBossCliMetricDefinition)MetricDefinition.getInstance(s);

        JBossCliMetricSource source = d.getSource();

        assertEquals(JBossControllerClient.DEFAULT_HOST, source.getHost());
        assertEquals(JBossControllerClient.DEFAULT_PORT, source.getPort());

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

        JBossCliMetricSource source = d.getSource();

        assertEquals("localhost", source.getHost());
        assertEquals(JBossControllerClient.DEFAULT_PORT, source.getPort());

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

        JBossCliMetricSource source = d.getSource();

        assertEquals("blue", source.getHost());
        assertEquals(JBossControllerClient.DEFAULT_PORT, source.getPort());

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

        JBossCliMetricSource source = d.getSource();

        assertEquals("localhost", source.getHost());
        assertEquals(9999, source.getPort());

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

        JBossCliMetricSource source = d.getSource();

        assertEquals("blue", source.getHost());
        assertEquals(9999, source.getPort());

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
            assertTrue(msg.startsWith("the jboss CLI metric definition does not contain a path: \""));
        }
    }

    @Test
    public void constructor_InvalidPort() throws Exception {

        try {
            new JBossCliMetricDefinition("jboss:some-host:70000/a=b/c=d/f");
            fail("should have thrown exception");
        }
        catch(MetricDefinitionException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.startsWith("invalid jboss CLI metric definition: invalid port value \"70000\""));

            JBossCliException cause = (JBossCliException)e.getCause();
            assertNotNull(cause);
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

    // getSource() -----------------------------------------------------------------------------------------------------

    @Test
    public void getSource() throws Exception {

        JBossCliMetricDefinition md = new JBossCliMetricDefinition("jboss:some-host:1000/a=b/c=d/f");

        JBossCliMetricSource source = md.getSource();

        List<MetricSource> sources = md.getSources(OS.Linux);
        assertEquals(1, sources.size());
        assertTrue(sources.contains(source));

        md.getSources(OS.MacOS);
        assertEquals(1, sources.size());
        assertTrue(sources.contains(source));

        md.getSources(OS.Windows);
        assertEquals(1, sources.size());
        assertTrue(sources.contains(source));
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