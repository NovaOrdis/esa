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

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.FaultEvent;
import io.novaordis.events.core.event.MockEvent;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test some aspects of base processing.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/7/16
 */
public class ProcessingLogicBaseTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void process_UnderlyingLogicThrowsExceptions() throws Exception {

        MockProcessingLogicBase mplb = new MockProcessingLogicBase();

        assertTrue(mplb.getEvents().isEmpty());

        // MockProcessingLogicBase is configured to throw a SYNTHETIC RuntimeException which should turn into
        // a FaultEvent

        mplb.setBroken(true);

        assertTrue(mplb.process(new MockEvent()));

        List<Event> events = mplb.getEvents();
        assertEquals(1, events.size());
        FaultEvent fe = (FaultEvent)events.get(0);

        Throwable cause = fe.getCause();
        assertTrue(cause instanceof RuntimeException);
        assertEquals("SYNTHETIC", cause.getMessage());
    }

    @Test
    public void process_ImplementationProducesNullsFromTimeToTime() throws Exception {

        MockProcessingLogicBase mplb = new MockProcessingLogicBase();

        assertTrue(mplb.getEvents().isEmpty());

        // MockProcessingLogicBase has a configurable output event generation rate - it only generates an output
        // event for r input events

        mplb.setRate(3);

        MockEvent me = new MockEvent();

        assertFalse(mplb.process(me));
        assertTrue(mplb.getEvents().isEmpty());

        MockEvent me2 = new MockEvent();

        assertFalse(mplb.process(me2));
        assertTrue(mplb.getEvents().isEmpty());

        MockEvent me3 = new MockEvent();

        assertTrue(mplb.process(me3));
        List<Event> events = mplb.getEvents();
        assertEquals(1, events.size());

        MockEvent outputMockEvent = (MockEvent)events.get(0);

        //noinspection unchecked
        List<MockEvent> contributors = (List<MockEvent>)outputMockEvent.getPayload();
        assertEquals(3, contributors.size());
        assertTrue(contributors.contains(me));
        assertTrue(contributors.contains(me2));
        assertTrue(contributors.contains(me3));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
