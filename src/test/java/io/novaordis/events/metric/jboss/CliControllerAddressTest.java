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

import io.novaordis.events.metric.MetricDefinitionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/31/16
 */
public class CliControllerAddressTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(JBossCliMetricDefinitionTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // constructors ----------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        CliControllerAddress a = new CliControllerAddress("something", 1);

        assertEquals("something", a.getHost());
        assertEquals(1, a.getPort());
        assertEquals("something:1", a.getAddress());
    }

    @Test
    public void constructor_String_DefaultPort() throws Exception {

        CliControllerAddress a = new CliControllerAddress("something");

        assertEquals("something", a.getHost());
        assertEquals(CliControllerAddress.DEFAULT_PORT, a.getPort());
        assertEquals("something", a.getAddress());
    }

    @Test
    public void constructor_String_MissingPort() throws Exception {

        try {
            new CliControllerAddress("something:");
            fail("should have thrown exception");
        }
        catch(MetricDefinitionException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("missing port information", msg);
        }
    }

    @Test
    public void constructor_String_InvalidPort() throws Exception {

        try {
            new CliControllerAddress("something:blah");
            fail("should have thrown exception");
        }
        catch(MetricDefinitionException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid port value \"blah\"" , msg);
        }
    }

    @Test
    public void constructor_String_HostAndPort() throws Exception {

        CliControllerAddress a = new CliControllerAddress("something:5");

        assertEquals("something", a.getHost());
        assertEquals(5, a.getPort());
        assertEquals("something:5", a.getAddress());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
