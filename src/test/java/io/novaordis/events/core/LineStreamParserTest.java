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

import io.novaordis.events.api.parser.ParsingException;
import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.FaultEvent;
import io.novaordis.events.api.event.LineEvent;
import io.novaordis.events.core.event.MockEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/5/16
 */
public class LineStreamParserTest extends ProcessingLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(LineStreamParserTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void setAndGetLineParser() throws Exception {

        LineStreamParser lsp = new LineStreamParser();

        assertNull(lsp.getLineParser());

        MockLineParser mlp = new MockLineParser();

        lsp.setLineParser(mlp);

        assertEquals(mlp, lsp.getLineParser());
    }

    // process() -------------------------------------------------------------------------------------------------------

    @Test
    public void invalidState_NullLineParser() throws Exception {

        LineStreamParser lsp = getProcessingLogicToTest();
        lsp.setLineParser(null);
        assertNull(lsp.getLineParser());

        assertNull(lsp.getLineParser());

        // this will trigger illegal state
        LineEvent se = new LineEvent(1L, "does not matter");

        try {
            lsp.process(se);
            fail("should throw IllegalStateException");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void endOfStreamEvent() throws Exception {

        LineStreamParser lsp = getProcessingLogicToTest();

        assertFalse(lsp.isClosed());

        MockLineParser mlp = new MockLineParser();
        lsp.setLineParser(mlp);

        boolean outputEventsAvailableForRetrieval = lsp.process(new EndOfStreamEvent());
        assertFalse(outputEventsAvailableForRetrieval);

        assertTrue(lsp.isClosed());

        //
        // one more invocation
        //

        try {
            lsp.process(new MockEvent());
            fail("should have thrown exception");
        }
        catch(ClosedException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void faultEvent() throws Exception {

        LineStreamParser lsp = getProcessingLogicToTest();

        MockLineParser mlp = new MockLineParser();
        lsp.setLineParser(mlp);

        FaultEvent fe = new FaultEvent("SYNTHETIC");

        boolean outputEventsAvailableForRetrieval = lsp.process(fe);

        assertTrue(outputEventsAvailableForRetrieval);

        //
        // will propagate fault events
        //

        List<Event> outputEvents = lsp.getEvents();
        assertEquals(1, outputEvents.size());
        assertEquals(fe, outputEvents.get(0));

        assertFalse(lsp.isClosed());
    }

    @Test
    public void unknownEvent() throws Exception {

        LineStreamParser lsp = getProcessingLogicToTest();

        MockLineParser mlp = new MockLineParser();
        lsp.setLineParser(mlp);

        MockEvent me = new MockEvent();

        boolean outputEventsAvailableForRetrieval = lsp.process(me);

        assertTrue(outputEventsAvailableForRetrieval);

        //
        // will turn the unknown event into a fault event
        //

        List<Event> outputEvents = lsp.getEvents();
        assertEquals(1, outputEvents.size());

        FaultEvent fe = (FaultEvent)outputEvents.get(0);

        assertFalse(lsp.isClosed());
        String msg = fe.getMessage();
        assertTrue(msg.matches(".+ does not know how to handle .+"));
    }

    @Test
    public void nullEvent() throws Exception {

        LineStreamParser lsp = getProcessingLogicToTest();

        MockLineParser mlp = new MockLineParser();
        lsp.setLineParser(mlp);

        boolean outputEventsAvailableForRetrieval = lsp.process(null);

        assertTrue(outputEventsAvailableForRetrieval);

        //
        // will turn the unknown event into a fault event
        //

        List<Event> outputEvents = lsp.getEvents();
        assertEquals(1, outputEvents.size());

        FaultEvent fe = (FaultEvent)outputEvents.get(0);

        assertFalse(lsp.isClosed());
        String msg = fe.getMessage();
        assertTrue(msg.matches(".+ does not know how to handle null"));
    }

    @Test
    public void stringEvent_CorrectSyntax() throws Exception {

        LineStreamParser lsp = getProcessingLogicToTest();

        MockLineParser mlp = new MockLineParser();
        lsp.setLineParser(mlp);

        // this can be parsed and produces a MockEvent
        String validLine = mlp.getValidLine();
        LineEvent se = new LineEvent(1L, validLine);

        boolean outputEventsAvailableForRetrieval = lsp.process(se);
        assertTrue(outputEventsAvailableForRetrieval);

        List<Event> outputEvents = lsp.getEvents();
        assertEquals(1, outputEvents.size());
        assertTrue(outputEvents.get(0) instanceof MockEvent);
    }

    @Test
    public void stringEvent_InvalidSyntax() throws Exception {

        LineStreamParser lsp = getProcessingLogicToTest();

        MockLineParser mlp = new MockLineParser();
        lsp.setLineParser(mlp);

        // this cannot be parsed and produces a FaultEvent
        String invalidLine = mlp.getInvalidLine();
        LineEvent se = new LineEvent(1L, invalidLine);

        boolean outputEventsAvailableForRetrieval = lsp.process(se);
        assertTrue(outputEventsAvailableForRetrieval);

        List<Event> outputEvents = lsp.getEvents();
        assertEquals(1, outputEvents.size());
        assertTrue(outputEvents.get(0) instanceof FaultEvent);
        FaultEvent fe = (FaultEvent)outputEvents.get(0);
        assertTrue(fe.getCause() instanceof ParsingException);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected LineStreamParser getProcessingLogicToTest() throws Exception {

        return new LineStreamParser(new MockLineParser());
    }

    @Override
    protected Event getInputEventRelevantToProcessingLogic() throws Exception {

        return new LineEvent(1L, MockLineParser.VALID_LINE);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
