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
import io.novaordis.events.metric.MetricDefinition;
import io.novaordis.events.metric.MockMetricDefinition;
import io.novaordis.events.metric.MockOS;
import io.novaordis.events.metric.source.MetricSourceTest;
import io.novaordis.jboss.cli.JBossControllerClient;
import io.novaordis.jboss.cli.model.JBossControllerAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/31/16
 */
public class JBossCliMetricSourceTest extends MetricSourceTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Before
    public void setUp() throws Exception {

        //
        // install the mock JBossControllerClient
        //

        System.setProperty(
                JBossControllerClient.JBOSS_CONTROLLER_CLIENT_IMPLEMENTATION_SYSTEM_PROPERTY_NAME,
                MockJBossControllerClient.class.getName());
    }

    @After
    public void cleanUp() throws Exception {

        //
        // clean the mock JBossControllerClient
        //

        System.clearProperty(JBossControllerClient.JBOSS_CONTROLLER_CLIENT_IMPLEMENTATION_SYSTEM_PROPERTY_NAME);
    }

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    @Test
    public void collectAllMetrics() throws Exception {

        JBossCliMetricSource s = getMetricSourceToTest();
        MockOS mos = new MockOS();

        List<Property> metrics =  s.collectAllMetrics(mos);
        assertTrue(metrics.isEmpty());
    }

    // collectMetrics() ------------------------------------------------------------------------------------------------

    @Test
    public void collectMetrics_SourceDoesNotHandleJBossCLIMetricsWithOtherSources() throws Exception {

        //
        // configure the internal client as a mock client and install state
        //

        JBossControllerClient client = JBossControllerClient.getInstance();
        ((MockJBossControllerClient)client).setAttributeValue("/test-path", "test-attribute", 7);

        JBossCliMetricSource jbossSource = getMetricSourceToTest();
        jbossSource.setControllerClient(client);

        String host = client.getHost();
        String otherHost = "OTHER-" + host;

        JBossCliMetricDefinition jbossCliMetricWithOtherSource =
                new JBossCliMetricDefinition("jboss:" + otherHost + "/test-path/test-attribute");

        //noinspection ArraysAsListWithZeroOrOneArgument
        List<MetricDefinition> definitions = Arrays.asList(jbossCliMetricWithOtherSource);
        List<Property> properties = jbossSource.collectMetrics(definitions);

        assertEquals(1, properties.size());

        //
        // the metric with other source must be ignored
        //

        Property p = properties.get(0);
        assertNull(p);
    }

    @Test
    public void collectMetrics_SomeOfTheDefinitionsAreNotJBossCliMetricDefinitions() throws Exception {

        //
        // configure the internal client as a mock client and install state
        //

        JBossControllerClient client = JBossControllerClient.getInstance();
        ((MockJBossControllerClient)client).setAttributeValue("/test-path", "test-attribute", 7);

        JBossCliMetricSource jbossSource = getMetricSourceToTest();
        jbossSource.setControllerClient(client);

        MockMetricDefinition mmd = new MockMetricDefinition("MOCK");

        JBossCliMetricDefinition jbmd = new JBossCliMetricDefinition(
                "jboss:" + client.getUsername() + ":mock-password" + "@" +
                        client.getHost() + ":" + client.getPort() + "/test-path/test-attribute");

        MockMetricDefinition mmd2 = new MockMetricDefinition("MOCK2");

        List<MetricDefinition> definitions = Arrays.asList(mmd, jbmd, mmd2);

        List<Property> properties = jbossSource.collectMetrics(definitions);

        assertEquals(3, properties.size());

        assertNull(properties.get(0));

        IntegerProperty p = (IntegerProperty)properties.get(1);
        String name = p.getName();
        String expected = client.getHost() + ":" + client.getPort() + "/test-path/test-attribute";
        assertEquals(expected, name);
        assertEquals(7, p.getValue());

        assertNull(properties.get(2));
    }

    @Test
    public void collectMetrics_SomeOfTheDefinitionsDoNotExistOnController() throws Exception {

        fail("RETURN HERE");
    }

    @Test
    public void collectMetrics_SomeOfTheDefinitionsDoNotHaveCorrespondingValues() throws Exception {

        fail("RETURN HERE");
    }

    @Test
    public void collectMetrics_LazyClientInitialization() throws Exception {

        fail("RETURN HERE");

        //
        // make sure the first collectMetrics() correctly initializes the internal client
        //
    }

    // host, port and username support ---------------------------------------------------------------------------------

    @Test
    public void defaultValues() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource();

        JBossControllerAddress address = s.getControllerAddress();

        assertEquals(JBossControllerClient.DEFAULT_HOST, address.getHost());
        assertEquals(JBossControllerClient.DEFAULT_PORT, address.getPort());
        assertNull(address.getUsername());
        assertNull(address.getPassword());
    }

    // equals() and hashCode() -----------------------------------------------------------------------------------------

    @Test
    public void equals_DefaultController() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource();
        JBossCliMetricSource s2 = new JBossCliMetricSource();

        assertEquals(s, s2);
        assertEquals(s2, s);
    }

    @Test
    public void equals_DefaultControllerPort() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource(new JBossControllerAddress(
                null, null, "somehost", "somehost", JBossControllerClient.DEFAULT_PORT, null));
        JBossCliMetricSource s2 = new JBossCliMetricSource(new JBossControllerAddress(
                null, null, "somehost", "somehost", JBossControllerClient.DEFAULT_PORT, null));

        assertEquals(s, s2);
        assertEquals(s2, s);
    }

    @Test
    public void equals_SameControllerAddress() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource(new JBossControllerAddress(
                null, null, "somehost", "somehost", 1234, "1234"));
        JBossCliMetricSource s2 = new JBossCliMetricSource(new JBossControllerAddress(
                null, null, "somehost", "somehost", 1234, "1234"));

        assertEquals(s, s2);
        assertEquals(s2, s);
    }

    @Test
    public void equals_SameControllerAddress2() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource(
                new JBossControllerAddress("someuser", new char[] {'a'}, "somehost", "somehost", 1234, "1234"));
        JBossCliMetricSource s2 = new JBossCliMetricSource(
                new JBossControllerAddress("someuser", new char[] {'b'}, "somehost", "somehost", 1234, "1234"));

        assertEquals(s, s2);
        assertEquals(s2, s);
    }

    @Test
    public void notEquals_DifferentUser() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource(
                new JBossControllerAddress("someuser", new char[] {'a'}, "somehost", "somehost", 1234, "1234"));
        JBossCliMetricSource s2 = new JBossCliMetricSource(
                new JBossControllerAddress("someuser2", new char[] {'a'}, "somehost", "somehost", 1234, "1234"));

        assertFalse(s.equals(s2));
        assertFalse(s2.equals(s));
    }

    @Test
    public void notEquals_DifferentPort() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource(new JBossControllerAddress(
                null, null, "localhost", null, 1234, "1234"));
        JBossCliMetricSource s2 = new JBossCliMetricSource(new JBossControllerAddress(
                null, null, "localhost", null, 1235, "1235"));

        assertFalse(s.equals(s2));
        assertFalse(s2.equals(s));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected JBossCliMetricSource getMetricSourceToTest() throws Exception {

        return new JBossCliMetricSource();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
