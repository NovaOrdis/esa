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

import io.novaordis.clad.UserErrorException;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.httpd.HttpEvent;
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
 * @since 4/22/16
 */
public class BusinessScenarioTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BusinessScenario.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // update() --------------------------------------------------------------------------------------------------------

    @Test
    public void update_ClosedScenario() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        assertFalse(bs.isClosed());

        bs.close();

        HttpEvent e = new HttpEvent(1L);

        try {

            bs.update(e);
            fail("should throw exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }
    }

    @Test
    public void update_StartMarkerArrivesBeforeEndMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        assertNull(bs.getType());
        assertEquals(0L, bs.getBeginTimestamp());
        assertEquals(BusinessScenarioState.NEW, bs.getState());

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(77L);

        assertFalse(bs.update(e));

        assertEquals("TYPE-A", bs.getType());
        assertFalse(bs.isClosed());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(1, bs.getRequestCount());
        assertEquals(77L, bs.getDuration());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        HttpEvent e2 = new HttpEvent(55L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e2.setRequestDuration(2L);

        assertTrue(bs.update(e2));

        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.CLOSED_BY_START_MARKER, bs.getState());
        assertEquals("TYPE-A", bs.getType());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(55L, bs.getEndTimestamp());
        assertEquals(1, bs.getRequestCount());
        assertEquals(77L, bs.getDuration());
    }

    @Test
    public void update_NoDuration_StartMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        assertEquals(BusinessScenarioState.NEW, bs.getState());

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");

        try {

            bs.update(e);
            fail("should throw exception");
        }
        catch(BusinessScenarioException ex) {
            log.info(ex.getMessage());
        }

        assertEquals("TYPE-A", bs.getType());
        assertFalse(bs.isClosed());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(1, bs.getRequestCount());
        assertEquals(0L, bs.getDuration());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());
    }

    @Test
    public void update_NoDuration_RegularRequest() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        assertNull(bs.getType());
        assertEquals(BusinessScenarioState.NEW, bs.getState());

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals("TYPE-A", bs.getType());
        assertFalse(bs.isClosed());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getDuration());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        // no duration
        HttpEvent e2 = new HttpEvent(2L);

        try {

            bs.update(e2);
            fail("should throw exception");
        }
        catch(BusinessScenarioException ex) {
            log.info(ex.getMessage());
        }

        assertEquals("TYPE-A", bs.getType());
        assertFalse(bs.isClosed());
        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getDuration());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());
    }

    @Test
    public void update() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        assertEquals(0L, bs.getDuration());
        assertEquals(0, bs.getRequestCount());
        assertEquals(0L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertNull(bs.getType());
        assertEquals(BusinessScenarioState.NEW, bs.getState());
        assertTrue(bs.getRequestSequenceIds().isEmpty());
        assertNull(bs.getIterationId());

        HttpEvent firstRequest = new HttpEvent(100L);
        firstRequest.setLongProperty(Event.LINE_NUMBER_PROPERTY_NAME, 777L);
        firstRequest.setRequestDuration(7L);
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "A");
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "10");

        assertFalse(bs.update(firstRequest));

        assertEquals(7L, bs.getDuration());
        assertEquals(1, bs.getRequestCount());
        assertEquals(100L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertEquals("TYPE-A", bs.getType());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());
        List<String> requestSequenceIds = bs.getRequestSequenceIds();
        assertEquals(1, requestSequenceIds.size());
        assertEquals("A", requestSequenceIds.get(0));
        assertEquals("10", bs.getIterationId());
        assertEquals(777L, bs.getLineNumber().longValue());

        HttpEvent secondRequest = new HttpEvent(200L);
        secondRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "B");
        secondRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "10");
        secondRequest.setRequestDuration(8L);

        assertFalse(bs.update(secondRequest));

        assertEquals(15L, bs.getDuration());
        assertEquals(2, bs.getRequestCount());
        assertEquals(100L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertEquals("TYPE-A", bs.getType());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());
        requestSequenceIds = bs.getRequestSequenceIds();
        assertEquals(2, requestSequenceIds.size());
        assertEquals("A", requestSequenceIds.get(0));
        assertEquals("B", requestSequenceIds.get(1));
        assertEquals("10", bs.getIterationId());

        assertFalse(bs.isClosed());
    }

    @Test
    public void update_EmptyStopMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        assertTrue(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(11L, bs.getEndTimestamp());
        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.CLOSED_NORMALLY, bs.getState());

        HttpEvent e3 = new HttpEvent(7L);

        try {
            bs.update(e3);
            fail("should have thrown exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.CLOSED_NORMALLY, bs.getState());
    }

    @Test
    public void update_SameStopMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "11");

        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());
        List<String> requestSequenceIds = bs.getRequestSequenceIds();
        assertEquals(1, requestSequenceIds.size());
        assertEquals("A", requestSequenceIds.get(0));
        assertEquals("11", bs.getIterationId());

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-A");
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "B");
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "11");

        assertTrue(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(11L, bs.getEndTimestamp());
        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.CLOSED_NORMALLY, bs.getState());
        requestSequenceIds = bs.getRequestSequenceIds();
        assertEquals(2, requestSequenceIds.size());
        assertEquals("A", requestSequenceIds.get(0));
        assertEquals("B", requestSequenceIds.get(1));
        assertEquals("11", bs.getIterationId());

        HttpEvent e3 = new HttpEvent(7L);

        try {
            bs.update(e3);
            fail("should have thrown exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.CLOSED_NORMALLY, bs.getState());
    }

    @Test
    public void update_DifferentStopMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-B");

        try {
            bs.update(e2);
            fail("should have thrown exception");
        }
        catch(UserErrorException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.FAULT, bs.getState());
    }

    @Test
    public void update_MissingStopMarker_NewStartMarker_SameType() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        HttpEvent e2 = new HttpEvent(2L);
        e2.setRequestDuration(2L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");

        assertTrue(bs.update(e2));

        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.CLOSED_BY_START_MARKER, bs.getState());
        assertEquals("TYPE-A", bs.getType());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(2L, bs.getEndTimestamp());
        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getDuration());
    }

    @Test
    public void update_UnknownRequestShouldThrowException() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        //
        // this scenario was not started
        //

        assertEquals(BusinessScenarioState.NEW, bs.getState());
        assertFalse(bs.isOpen());

        HttpEvent e = new HttpEvent(1L);
        e.setRequestDuration(1L);

        try {
            bs.update(e);
            fail("should throw exception");
        }
        catch(BusinessScenarioException ex) {
            log.info(ex.getMessage());
        }

        assertFalse(bs.isClosed());
        assertFalse(bs.isOpen());
        assertEquals(0, bs.getRequestCount());
        assertEquals(0, bs.getDuration());
        assertNull(bs.getType());
        assertEquals(BusinessScenarioState.NEW, bs.getState());
    }

    @Test
    public void update_RequestThatBelongsToADifferentSession() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent firstRequest = new HttpEvent(100L);
        firstRequest.setRequestDuration(7L);
        firstRequest.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "session-1");
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");

        assertFalse(bs.update(firstRequest));

        assertEquals("session-1", bs.getJSessionId());

        HttpEvent requestFromAnotherSession = new HttpEvent(200L);
        requestFromAnotherSession.setRequestDuration(1L);
        requestFromAnotherSession.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "session-2");

        try {
            bs.update(requestFromAnotherSession);
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("was updated with a request that belongs to a different session"));
        }
    }

    @Test
    public void update_duplicateRequestSequenceId() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        HttpEvent e = new HttpEvent(1L);
        e.setRequestDuration(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "samevalue");

        assertFalse(bs.update(e));

        HttpEvent e2 = new HttpEvent(2L);
        e2.setRequestDuration(2L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "samevalue");

        try {
            bs.update(e2);
            fail("should throw exception");
        }
        catch(BusinessScenarioException bse) {
            String msg = bse.getMessage();
            log.info(msg);
            assertTrue(msg.matches(".* received duplicate request sequence ID \"samevalue\""));
            assertEquals(BusinessScenarioFaultType.DUPLICATE_REQUEST_SEQUENCE_ID, bse.getFaultType());
        }
    }

    @Test
    public void update_suddenIterationID() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        //
        // we start with NO iteration ID
        //
        assertFalse(bs.update(e));
        assertNull(bs.getIterationId());

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "something");

        try {
            //
            // we cannot update a business scenario with a request that has a non-null Iteration ID if the
            // scenario was started without Iteration ID.
            //
            bs.update(e2);
        }
        catch(BusinessScenarioException bse) {

            String msg = bse.getMessage();
            log.info(msg);
            assertTrue(msg.matches(".* is suddenly starting to see iteration IDs after it started without one: .*"));
            assertEquals(BusinessScenarioFaultType.SUDDEN_ITERATION_IDS, bse.getFaultType());
        }
    }

    @Test
    public void update_differentIterationIds() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "something");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals("something", bs.getIterationId());

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "something-else");

        try {
            //
            // we cannot update a business scenario with a request that has a non-null Iteration ID if the
            // scenario was started without Iteration ID.
            //
            bs.update(e2);
        }
        catch(BusinessScenarioException bse) {

            String msg = bse.getMessage();
            log.info(msg);
            assertTrue(msg.matches(".* exposed to multiple iterations: .*"));
            assertEquals(BusinessScenarioFaultType.MULTIPLE_ITERATION_IDS, bse.getFaultType());
        }
    }

    @Test
    public void update_missingIterationId() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "something");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals("something", bs.getIterationId());

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        try {
            //
            // we cannot update a business scenario with a request that has a non-null Iteration ID if the
            // scenario was started without Iteration ID.
            //
            bs.update(e2);
        }
        catch(BusinessScenarioException bse) {

            String msg = bse.getMessage();
            log.info(msg);
            assertTrue(msg.matches(".* does not see iteration IDs anymore"));
            assertEquals(BusinessScenarioFaultType.MISSING_ITERATION_ID, bse.getFaultType());
        }
    }

    // toEvent() -------------------------------------------------------------------------------------------------------

    @Test
    public void toEvent() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(777L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "iteration-one");
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "a-session");
        e.setRequestDuration(7L);

        assertFalse(bs.update(e));

        HttpEvent e2 = new HttpEvent(888L);
        e2.setRequestDuration(8L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-A");
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "iteration-one");
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "a-session");

        assertTrue(bs.update(e2));

        assertTrue(bs.isClosed());

        BusinessScenarioEvent bse = bs.toEvent();

        assertEquals(777L, bse.getTimestamp().longValue());
        assertEquals(15L, bse.getLongProperty(BusinessScenarioEvent.DURATION).getLong().longValue());
        assertEquals(2, bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT).getInteger().intValue());
        assertEquals("TYPE-A", bse.getStringProperty(BusinessScenarioEvent.TYPE).getValue());
        assertEquals(BusinessScenarioState.CLOSED_NORMALLY.name(),
                bse.getStringProperty(BusinessScenarioEvent.STATE).getValue());
        assertEquals("a-session", bse.getJSessionId());
        assertEquals("iteration-one", bse.getIterationId());
    }

    @Test
    public void toEvent_SpecialClosedState() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        bs.setState(BusinessScenarioState.CLOSED_BY_START_MARKER);
        bs.setType("SOME-TYPE");
        bs.setBeginTimestamp(101L);
        bs.updateScenarioStatistics(11L, null, null);
        bs.updateScenarioStatistics(null, null, null);
        bs.updateScenarioStatistics(22L, null, null);

        BusinessScenarioEvent bse = bs.toEvent();

        assertEquals(BusinessScenarioState.CLOSED_BY_START_MARKER.name(),
                bse.getStringProperty(BusinessScenarioEvent.STATE).getString());
        assertEquals(101L, bse.getTimestamp().longValue());
        assertEquals(33L, bse.getLongProperty(BusinessScenarioEvent.DURATION).getLong().longValue());
        assertEquals(3, bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT).getInteger().intValue());
        assertEquals("SOME-TYPE", bse.getStringProperty(BusinessScenarioEvent.TYPE).getValue());
    }

    // setType() -------------------------------------------------------------------------------------------------------

    @Test
    public void setType_TypeCanOnlyBeSetOnce() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        assertNull(bs.getType());

        bs.setType("something");
        assertEquals("something", bs.getType());

        bs.setType("something");
        assertEquals("something", bs.getType());

        try {
            bs.setType("something else");
            fail("should throw exception");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }
    }

    // state -----------------------------------------------------------------------------------------------------------

    @Test
    public void state_NEW() {

        BusinessScenario bs = new BusinessScenario();
        assertEquals(BusinessScenarioState.NEW, bs.getState());
        assertTrue(bs.isNew());
    }

    // close() ---------------------------------------------------------------------------------------------------------

    @Test
    public void close_NEW() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        assertTrue(bs.isNew());
        bs.close();
        assertFalse(bs.isNew());
        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.INCOMPLETE, bs.getState());
    }

    @Test
    public void close_OPEN() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestDuration(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");

        assertFalse(bs.update(e));

        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        bs.close();

        assertFalse(bs.isNew());
        assertFalse(bs.isOpen());
        assertEquals(BusinessScenarioState.INCOMPLETE, bs.getState());
        assertEquals(-1L, bs.getEndTimestamp());
    }

    @Test
    public void close_CLOSED() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestDuration(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");
        bs.update(e);
        e = new HttpEvent(2L);
        e.setRequestDuration(2L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TEST");
        assertTrue(bs.update(e));

        assertEquals(BusinessScenarioState.CLOSED_NORMALLY, bs.getState());

        try {
            bs.close();
            fail("should throw exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.CLOSED_NORMALLY, bs.getState());
    }

    @Test
    public void close_CLOSED_BY_START_MARKER() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestDuration(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");
        bs.update(e);
        e = new HttpEvent(2L);
        e.setRequestDuration(2L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");
        assertTrue(bs.update(e));

        assertEquals(BusinessScenarioState.CLOSED_BY_START_MARKER, bs.getState());

        try {
            bs.close();
            fail("should throw exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.CLOSED_BY_START_MARKER, bs.getState());
    }

    @Test
    public void close_FAULT() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        bs.setState(BusinessScenarioState.FAULT);
        assertEquals(BusinessScenarioState.FAULT, bs.getState());

        try {
            bs.close();
            fail("should throw exception");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        assertEquals(BusinessScenarioState.FAULT, bs.getState());
    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
