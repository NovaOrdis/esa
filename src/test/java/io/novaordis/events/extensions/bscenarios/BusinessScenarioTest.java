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

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.ListProperty;
import io.novaordis.events.httpd.HttpEvent;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.time.TimestampImpl;
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

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));

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
        assertNull(bs.getBeginTimestamp());
        assertEquals(BusinessScenarioState.NEW, bs.getState());

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(77L);

        assertFalse(bs.update(e));

        assertEquals("TYPE-A", bs.getType());
        assertFalse(bs.isClosed());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(1, bs.getRequestCount());
        assertEquals(77L, bs.getDuration());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(55L));
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e2.setRequestDuration(2L);

        assertTrue(bs.update(e2));

        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.CLOSED_BY_START_MARKER, bs.getState());
        assertEquals("TYPE-A", bs.getType());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(55L, bs.getEndTimestamp().getTime());
        assertEquals(1, bs.getRequestCount());
        assertEquals(77L, bs.getDuration());
    }

    @Test
    public void update_NoDuration_StartMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        assertEquals(BusinessScenarioState.NEW, bs.getState());

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
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
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(1, bs.getRequestCount());
        assertEquals(0L, bs.getDuration());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());
    }

    @Test
    public void update_NoDuration_RegularRequest() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        assertNull(bs.getType());
        assertEquals(BusinessScenarioState.NEW, bs.getState());

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals("TYPE-A", bs.getType());
        assertFalse(bs.isClosed());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getDuration());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        // no duration
        HttpEvent e2 = new HttpEvent(new TimestampImpl(2L));

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
        assertNull(bs.getBeginTimestamp());
        assertNull(bs.getEndTimestamp());
        assertNull(bs.getType());
        assertEquals(BusinessScenarioState.NEW, bs.getState());
        assertTrue(bs.getRequestSequenceIds().isEmpty());
        assertNull(bs.getIterationId());

        HttpEvent firstRequest = new HttpEvent(new TimestampImpl(100L));
        firstRequest.setLongProperty(Event.LINE_NUMBER_PROPERTY_NAME, 777L);
        firstRequest.setRequestDuration(7L);
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "A");
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "10");

        assertFalse(bs.update(firstRequest));

        assertEquals(7L, bs.getDuration());
        assertEquals(1, bs.getRequestCount());
        assertEquals(100L, bs.getBeginTimestamp().getTime());
        assertEquals(100L + 7L, bs.getEndTimestamp().getTime());
        assertEquals("TYPE-A", bs.getType());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());
        List<String> requestSequenceIds = bs.getRequestSequenceIds();
        assertEquals(1, requestSequenceIds.size());
        assertEquals("A", requestSequenceIds.get(0));
        assertEquals("10", bs.getIterationId());
        assertEquals(777L, bs.getLineNumber().longValue());

        HttpEvent secondRequest = new HttpEvent(new TimestampImpl(200L));
        secondRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "B");
        secondRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "10");
        secondRequest.setRequestDuration(8L);

        assertFalse(bs.update(secondRequest));

        assertEquals(15L, bs.getDuration());
        assertEquals(2, bs.getRequestCount());
        assertEquals(100L, bs.getBeginTimestamp().getTime());
        assertEquals(200L + 8L, bs.getEndTimestamp().getTime());
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

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(1L + 1L, bs.getEndTimestamp().getTime());
        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(5L));
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        assertTrue(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(11L, bs.getEndTimestamp().getTime());
        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.COMPLETE, bs.getState());

        HttpEvent e3 = new HttpEvent(new TimestampImpl(7L));

        try {
            bs.update(e3);
            fail("should have thrown exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.COMPLETE, bs.getState());
    }

    @Test
    public void update_SameStopMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "11");

        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(1L + 1L, bs.getEndTimestamp().getTime());
        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());
        List<String> requestSequenceIds = bs.getRequestSequenceIds();
        assertEquals(1, requestSequenceIds.size());
        assertEquals("A", requestSequenceIds.get(0));
        assertEquals("11", bs.getIterationId());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(5L));
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-A");
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "B");
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "11");

        assertTrue(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(11L, bs.getEndTimestamp().getTime());
        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.COMPLETE, bs.getState());
        requestSequenceIds = bs.getRequestSequenceIds();
        assertEquals(2, requestSequenceIds.size());
        assertEquals("A", requestSequenceIds.get(0));
        assertEquals("B", requestSequenceIds.get(1));
        assertEquals("11", bs.getIterationId());

        HttpEvent e3 = new HttpEvent(new TimestampImpl(7L));

        try {
            bs.update(e3);
            fail("should have thrown exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.COMPLETE, bs.getState());
    }

    @Test
    public void update_DifferentStopMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(1L + 1L, bs.getEndTimestamp().getTime());
        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(5L));
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

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(2L));
        e2.setRequestDuration(2L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");

        assertTrue(bs.update(e2));

        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.CLOSED_BY_START_MARKER, bs.getState());
        assertEquals("TYPE-A", bs.getType());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(2L, bs.getEndTimestamp().getTime());
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

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
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

        HttpEvent firstRequest = new HttpEvent(new TimestampImpl(100L));
        firstRequest.setRequestDuration(7L);
        firstRequest.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "session-1");
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");

        assertFalse(bs.update(firstRequest));

        assertEquals("session-1", bs.getJSessionId());

        HttpEvent requestFromAnotherSession = new HttpEvent(new TimestampImpl(200L));
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
        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestDuration(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "samevalue");

        assertFalse(bs.update(e));

        HttpEvent e2 = new HttpEvent(new TimestampImpl(2L));
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

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        //
        // we start with NO iteration ID
        //
        assertFalse(bs.update(e));
        assertNull(bs.getIterationId());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(5L));
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

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "something");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals("something", bs.getIterationId());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(5L));
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

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "something");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals("something", bs.getIterationId());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(5L));
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

    @Test
    public void update_StartMarkerRequestHasNoDurationInformation() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");
        e.setLineNumber(1001L);
        assertNull(e.getRequestDuration());


        try {
            assertFalse(bs.update(e));
            fail("should throw exception");
        }
        catch(BusinessScenarioException ex) {

            assertEquals(1001L, ex.getLineNumber().longValue());
            assertEquals(BusinessScenarioFaultType.NO_REQUEST_DURATION_INFO, ex.getFaultType());
            String msg = ex.getMessage();
            log.info(msg);
            assertTrue(msg.matches("^.* does not have request duration information"));
        }

        //
        // the state of the scenario is consistent even if the exception was thrown
        //
        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(1L, bs.getEndTimestamp().getTime());
        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());
    }

    @Test
    public void update_ScenarioIsClosedButThereIsNoDuration() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        HttpEvent e2 = new HttpEvent(new TimestampImpl(10L));
        assertNull(e2.getRequestDuration());
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TEST");
        e2.setLineNumber(1001L);

        try {
            assertTrue(bs.update(e2));
            fail("should throw exception");
        }
        catch(BusinessScenarioException ex) {

            assertEquals(1001L, ex.getLineNumber().longValue());
            assertEquals(BusinessScenarioFaultType.NO_REQUEST_DURATION_INFO, ex.getFaultType());
            String msg = ex.getMessage();
            log.info(msg);
            assertTrue(msg.matches("^.* does not have request duration information"));
        }

        //
        // the state of the scenario is consistent even if the exception was thrown
        //
        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(10L, bs.getEndTimestamp().getTime());
        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.COMPLETE, bs.getState());
    }

    @Test
    public void update_EndToEnd() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        //
        // Start scenario
        //

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");
        e.setRequestDuration(1L);
        e.setStatusCode(200);

        assertFalse(bs.update(e));

        assertEquals(1, bs.getRequestCount());
        assertEquals(1, bs.getRequestCount(200));
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(2L, bs.getEndTimestamp().getTime());
        assertEquals(1L, bs.getDuration());
        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        //
        // 200 request
        //

        HttpEvent e2 = new HttpEvent(new TimestampImpl(5L));
        e2.setRequestDuration(6L);
        e2.setStatusCode(200);

        assertFalse(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(2, bs.getRequestCount(200));
        assertEquals(7L, bs.getDuration());
        assertEquals(11L, bs.getEndTimestamp().getTime());

        //
        // 400 request
        //

        HttpEvent e3 = new HttpEvent(new TimestampImpl(10L));
        e3.setRequestDuration(11L);
        e3.setStatusCode(400);

        assertFalse(bs.update(e3));

        assertEquals(3, bs.getRequestCount());
        assertEquals(2, bs.getRequestCount(200));
        assertEquals(1, bs.getRequestCount(400));
        assertEquals(21L, bs.getEndTimestamp().getTime());
        assertEquals(18L, bs.getDuration());
        assertFalse(bs.isClosed());

        //
        // Stop scenario
        //

        HttpEvent e4 = new HttpEvent(new TimestampImpl(15L));
        e4.setRequestDuration(16L);
        e4.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);
        e4.setStatusCode(500);

        assertTrue(bs.update(e4));

        assertEquals(4, bs.getRequestCount());
        assertEquals(2, bs.getRequestCount(200));
        assertEquals(1, bs.getRequestCount(400));
        assertEquals(1, bs.getRequestCount(500));
        assertEquals(1L, bs.getBeginTimestamp().getTime());
        assertEquals(31L, bs.getEndTimestamp().getTime());
        assertEquals(34L, bs.getDuration());
        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.COMPLETE, bs.getState());
    }

    // toEvent() -------------------------------------------------------------------------------------------------------

    @Test
    public void toEvent() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(new TimestampImpl(777L));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "iteration-one");
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "a-session");
        e.setRequestDuration(7L);
        e.setStatusCode(200);

        assertFalse(bs.update(e));

        HttpEvent e2 = new HttpEvent(new TimestampImpl(888L));
        e2.setRequestDuration(8L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-A");
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "iteration-one");
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "a-session");
        e2.setStatusCode(300);

        assertTrue(bs.update(e2));

        assertTrue(bs.isClosed());

        BusinessScenarioEvent bse = bs.toEvent();

        assertEquals(777L, bse.getTime().longValue());
        assertEquals(15L, bse.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(2, bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
        assertEquals("TYPE-A", bse.getStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME).getValue());
        assertEquals(BusinessScenarioState.COMPLETE.name(),
                bse.getStringProperty(BusinessScenarioEvent.STATE_PROPERTY_NAME).getValue());
        assertEquals("a-session", bse.getJSessionId());
        assertEquals("iteration-one", bse.getIterationId());

        //noinspection unchecked
        ListProperty<Long> requestDurationsProperty = bse.getListProperty(BusinessScenarioEvent.REQUEST_DURATIONS_PROPERTY_NAME);
        List<Long> requestDurations = requestDurationsProperty.getList();
        assertEquals(2, requestDurations.size());
        assertEquals(7L, requestDurations.get(0).longValue());
        assertEquals(8L, requestDurations.get(1).longValue());

        //noinspection unchecked
        ListProperty<Integer> requestStatusCodeProperty = bse.getListProperty(BusinessScenarioEvent.REQUEST_STATUS_CODES_PROPERTY_NAME);
        List<Integer> requestStatusCodes = requestStatusCodeProperty.getList();
        assertEquals(2, requestStatusCodes.size());
        assertEquals(200, requestStatusCodes.get(0).intValue());
        assertEquals(300, requestStatusCodes.get(1).intValue());
    }

    @Test
    public void toEvent_SpecialClosedState() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        bs.setState(BusinessScenarioState.CLOSED_BY_START_MARKER);
        bs.setType("SOME-TYPE");
        bs.setBeginTimestamp(new TimestampImpl(101L));

        HttpEvent e = new HttpEvent(new TimestampImpl(0L));
        e.setRequestDuration(11L);

        bs.updateScenarioStatistics(e);

        HttpEvent e2 = new HttpEvent(new TimestampImpl(0L));
        assertNull(e2.getRequestDuration());

        bs.updateScenarioStatistics(e2);

        HttpEvent e3 = new HttpEvent(new TimestampImpl(0L));
        e3.setRequestDuration(22L);

        bs.updateScenarioStatistics(e3);

        BusinessScenarioEvent bse = bs.toEvent();

        assertEquals(BusinessScenarioState.CLOSED_BY_START_MARKER.name(),
                bse.getStringProperty(BusinessScenarioEvent.STATE_PROPERTY_NAME).getString());
        assertEquals(101L, bse.getTime().longValue());
        assertEquals(33L, bse.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(3, bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
        assertEquals("SOME-TYPE", bse.getStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME).getValue());
    }

    @Test
    public void toEvent_getSuccessfulRequestCount_AllSuccessfulRequests() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        //
        // all requests are 200, so the scenario must be successful
        //

        HttpEvent e = new HttpEvent(new TimestampImpl(777L), 7L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setStatusCode(200);

        assertFalse(bs.update(e));

        HttpEvent e2 = new HttpEvent(new TimestampImpl(888L), 8L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-A");
        e2.setStatusCode(200);

        assertTrue(bs.update(e2));

        BusinessScenarioEvent bse = bs.toEvent();

        assertEquals(2, bse.getSuccessfulRequestCount().intValue());
    }

    @Test
    public void toEvent_getSuccessfulRequestCount_SomeRequestsNotSuccessful() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        //
        // some requests are not 200, so the scenario must not be successful
        //

        HttpEvent e = new HttpEvent(new TimestampImpl(777L), 7L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setStatusCode(200);

        assertFalse(bs.update(e));

        HttpEvent e2 = new HttpEvent(new TimestampImpl(888L), 8L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-A");
        e2.setStatusCode(201);

        assertTrue(bs.update(e2));

        BusinessScenarioEvent bse = bs.toEvent();

        assertEquals(1, bse.getSuccessfulRequestCount().intValue());
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

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setRequestDuration(2L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");

        assertFalse(bs.update(e));

        assertEquals(BusinessScenarioState.OPEN, bs.getState());

        bs.close();

        assertFalse(bs.isNew());
        assertFalse(bs.isOpen());
        assertEquals(BusinessScenarioState.INCOMPLETE, bs.getState());
        assertEquals(1L + 2L, bs.getEndTimestamp().getTime());
    }

    @Test
    public void close_CLOSED() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(new TimestampImpl(1));
        e.setRequestDuration(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");
        bs.update(e);
        e = new HttpEvent(new TimestampImpl(2L));
        e.setRequestDuration(2L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TEST");
        assertTrue(bs.update(e));

        assertEquals(BusinessScenarioState.COMPLETE, bs.getState());

        try {
            bs.close();
            fail("should throw exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.COMPLETE, bs.getState());
    }

    @Test
    public void close_CLOSED_BY_START_MARKER() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(new TimestampImpl(1));
        e.setRequestDuration(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");
        bs.update(e);
        e = new HttpEvent(new TimestampImpl(2L));
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

    // getRequestCount() -----------------------------------------------------------------------------------------------

    @Test
    public void getRequestCount() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        assertEquals(0, bs.getRequestCount(200));

        HttpEvent e = new HttpEvent(new TimestampImpl(1L), 1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");
        e.setStatusCode(200);

        assertFalse(bs.update(e));

        assertEquals(1, bs.getRequestCount());
        assertEquals(1, bs.getRequestCount(200));
        assertEquals(0, bs.getRequestCount(201));

        HttpEvent e2 = new HttpEvent(new TimestampImpl(2L), 2L);
        e2.setStatusCode(201);

        assertFalse(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(1, bs.getRequestCount(200));
        assertEquals(1, bs.getRequestCount(201));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
