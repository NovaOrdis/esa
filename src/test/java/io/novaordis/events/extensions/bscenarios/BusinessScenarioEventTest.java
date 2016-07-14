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

package io.novaordis.events.extensions.bscenarios;

import io.novaordis.events.core.event.TimedEventTest;
import io.novaordis.utilities.time.TimestampImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/23/16
 */
public class BusinessScenarioEventTest extends TimedEventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BusinessScenarioEventTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Overrides -------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void getState_NoState() throws Exception {

        BusinessScenarioEvent bse = getEventToTest(1L);
        assertNull(bse.getState());
    }

    @Test
    public void getState_InvalidState() throws Exception {

        BusinessScenarioEvent bse = getEventToTest(1L);
        bse.setStringProperty(BusinessScenarioEvent.STATE_PROPERTY_NAME, "I-am-pretty-sure-there-is-no-such-value-in-enum");

        try {
            bse.getState();
            fail("should have thrown exception");
        }
        catch(IllegalStateException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches(
                    ".* carries an invalid BusinessScenarioState value \"I-am-pretty-sure-there-is-no-such-value-in-enum\""));

            Throwable t = e.getCause();
            assertTrue(t instanceof IllegalArgumentException);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected BusinessScenarioEvent getEventToTest(Long timestamp) throws Exception {
        return new BusinessScenarioEvent(timestamp == null ? null : new TimestampImpl(timestamp.longValue()));
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
