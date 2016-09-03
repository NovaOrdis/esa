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

import io.novaordis.events.core.event.Property;
import io.novaordis.events.metric.MockOS;
import io.novaordis.events.metric.source.MetricSourceTest;
import io.novaordis.jboss.cli.JBossControllerClient;
import io.novaordis.jboss.cli.model.JBossControllerAddress;
import org.junit.Test;

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

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    @Test
    public void collectMetrics() throws Exception {

        JBossCliMetricSource s = getMetricSourceToTest();
        MockOS mos = new MockOS();

        List<Property> metrics =  s.collectAllMetrics(mos);
        assertTrue(metrics.isEmpty());
    }

    // collectMetrics() ------------------------------------------------------------------------------------------------

    @Test
    public void collectMetrics_SomeOfTheDefinitionsAreNotJBossCliMetricDefinitions() throws Exception {

        fail("RETURN HERE");
    }

    @Test
    public void collectMetrics_SomeOfTheDefinitionsDoNotExistOnController() throws Exception {

        fail("RETURN HERE");
    }

    @Test
    public void collectMetrics_SomeOfTheDefinitionsDoNotHaveCorrespondingValues() throws Exception {

        fail("RETURN HERE");
    }

    // host, port and username support ---------------------------------------------------------------------------------

    @Test
    public void defaultValues() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource();

        assertEquals(JBossControllerClient.DEFAULT_HOST, s.getHost());
        assertEquals(JBossControllerClient.DEFAULT_PORT, s.getPort());
        assertNull(s.getUsername());
        assertNull(s.getPassword());
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

        JBossCliMetricSource s = new JBossCliMetricSource(new JBossControllerAddress("somehost"));
        JBossCliMetricSource s2 = new JBossCliMetricSource(new JBossControllerAddress("somehost"));

        assertEquals(s, s2);
        assertEquals(s2, s);
    }

    @Test
    public void equals_SameControllerAddress() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource(new JBossControllerAddress("somehost", 1234));
        JBossCliMetricSource s2 = new JBossCliMetricSource(new JBossControllerAddress("somehost", 1234));

        assertEquals(s, s2);
        assertEquals(s2, s);
    }

    @Test
    public void equals_SameControllerAddress2() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource(
                new JBossControllerAddress("someuser", new char[] {'a'}, "somehost", 1234));
        JBossCliMetricSource s2 = new JBossCliMetricSource(
                new JBossControllerAddress("someuser", new char[] {'b'}, "somehost", 1234));

        assertEquals(s, s2);
        assertEquals(s2, s);
    }

    @Test
    public void notEquals_DifferentUser() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource(
                new JBossControllerAddress("someuser", new char[] {'a'}, "somehost", 1234));
        JBossCliMetricSource s2 = new JBossCliMetricSource(
                new JBossControllerAddress("someuser2", new char[] {'a'}, "somehost", 1234));

        assertFalse(s.equals(s2));
        assertFalse(s2.equals(s));
    }

    @Test
    public void notEquals_DifferentPort() throws Exception {

        JBossCliMetricSource s = new JBossCliMetricSource(new JBossControllerAddress("localhost", 1234));
        JBossCliMetricSource s2 = new JBossCliMetricSource(new JBossControllerAddress("localhost", 1235));

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
