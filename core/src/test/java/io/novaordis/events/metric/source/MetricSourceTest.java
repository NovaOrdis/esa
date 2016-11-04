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

package io.novaordis.events.metric.source;

import io.novaordis.events.core.event.Property;
import io.novaordis.events.metric.MetricDefinition;
import io.novaordis.events.metric.MockMetricDefinition;
import io.novaordis.events.metric.MockOS;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/4/16
 */
public abstract class MetricSourceTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // collectAllMetrics() ---------------------------------------------------------------------------------------------

    @Test
    public void collectAllMetrics() throws Exception {

        MetricSource s = getMetricSourceToTest();

        MockOS mos = new MockOS();

        List<Property> properties = s.collectAllMetrics(mos);

        assertNotNull(properties);
    }

    // collectMetrics() ------------------------------------------------------------------------------------------------

    @Test
    public void collectMetrics_SourceDoesNotProduceMetricForADefinition() throws Exception {

        MetricSource s = getMetricSourceToTest();

        MockMetricDefinition mmd = new MockMetricDefinition("MOCK");

        //noinspection ArraysAsListWithZeroOrOneArgument
        List<MetricDefinition> definitions = Arrays.asList(mmd);

        List<Property> properties = s.collectMetrics(definitions);

        assertNotNull(properties);
        assertEquals(1, properties.size());

        Property p = properties.get(0);
        assertNull(p);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract MetricSource getMetricSourceToTest() throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
