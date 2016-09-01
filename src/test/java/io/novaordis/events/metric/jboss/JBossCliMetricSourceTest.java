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

import io.novaordis.events.metric.source.MetricSource;
import io.novaordis.events.metric.source.MetricSourceTest;
import org.junit.Test;

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

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected MetricSource getMetricSourceToTest() throws Exception {

        return new JBossCliMetricSource();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
