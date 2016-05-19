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

import io.novaordis.events.core.CsvOutputFormatterTest;
import io.novaordis.events.core.event.MockEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 5/18/16
 */
public class BusinessScenarioOutputFormatterTest extends CsvOutputFormatterTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BusinessScenarioOutputFormatterTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Overrides -------------------------------------------------------------------------------------------------------

    @Test
    @Override
    public void process_RegularTimedEvent_NoConfiguredOutputFormat() {
        // noop
    }

    @Test
    @Override
    public void process_RegularTimedEvent_WithConfiguredOutputFormat() {
        // noop
    }

    @Test
    @Override
    public void process_RegularUntimedEvent_NoConfiguredOutputFormat() {
        // noop
    }

    @Test
    @Override
    public void process_RegularUntimedEvent_WithConfiguredOutputFormat() {
        // noop
    }

    @Test
    @Override
    public void process_MultipleUncollectedEventsFollowedByEndOfStream() {
        // noop
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void toString_NotABusinessScenarioEvent() throws Exception {

        BusinessScenarioOutputFormatter f = getConversionLogicToTest();

        try {
            f.toString(new MockEvent());
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void toString_Reference() throws Exception {

        BusinessScenarioOutputFormatter f = getConversionLogicToTest();

        BusinessScenario bs = new BusinessScenario();
        bs.setBeginTimestamp(1L);
        bs.setId(2L);
        bs.setJSessionId("test-jsession-id");
        bs.setIterationId("test-iteration-id");
        bs.setType("test-type");
        bs.setState(BusinessScenarioState.COMPLETE);

//                BusinessScenarioEvent.REQUEST_COUNT,
//                BusinessScenarioEvent.SUCCESSFUL_REQUEST_COUNT,
//                BusinessScenarioEvent.DURATION,

        BusinessScenarioEvent bse = bs.toEvent();

        String s = f.toString(bse);

        assertEquals("12/31/69 16:00:00, 2, test-jsession-id, test-iteration-id, test-type, COMPLETE, 0, 0, 0", s);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected BusinessScenarioOutputFormatter getConversionLogicToTest() throws Exception {
        return new BusinessScenarioOutputFormatter();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
