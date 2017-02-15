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

package io.novaordis.events.core;

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
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
 * @since 1/24/16
 */
public abstract class ProcessingLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ProcessingLogicTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void processAndGetEvents() throws Exception {

        ProcessingLogic pl = getProcessingLogicToTest();

        assertTrue(pl.getEvents().isEmpty());
        assertTrue(pl.getEvents().isEmpty());
        
        Event inputEvent = getInputEventRelevantToProcessingLogic();

        assertTrue(pl.process(inputEvent));

        List<Event> outputEvents = pl.getEvents();

        assertEquals(1, outputEvents.size());
        Event outputEvent = outputEvents.get(0);
        assertNotNull(outputEvent);

        assertTrue(pl.getEvents().isEmpty());
    }

    @Test
    public void processEndOfStreamEvent() throws Exception {

        ProcessingLogic pl = getProcessingLogicToTest();

        pl.process(new EndOfStreamEvent());

        Event inputEvent = getInputEventRelevantToProcessingLogic();

        try {
            // make sure the processing logic is closed
            pl.process(inputEvent);
            fail("should throw exception");
        }
        catch(ClosedException e) {
            log.info(e.getMessage());
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract ProcessingLogic getProcessingLogicToTest() throws Exception;

    /**
     * @return an Event that is meaningful to the processing logic instance and that, once processed, produces a
     * corresponding output Event.
     */
    protected abstract Event getInputEventRelevantToProcessingLogic() throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
