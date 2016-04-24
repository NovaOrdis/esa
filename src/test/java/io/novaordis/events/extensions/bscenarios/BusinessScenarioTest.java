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
import io.novaordis.events.httpd.HttpEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());

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
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());
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
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());

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
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());
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

        HttpEvent firstRequest = new HttpEvent(100L);
        firstRequest.setRequestDuration(7L);
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");

        assertFalse(bs.update(firstRequest));

        assertEquals(7L, bs.getDuration());
        assertEquals(1, bs.getRequestCount());
        assertEquals(100L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertEquals("TYPE-A", bs.getType());
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());

        HttpEvent secondRequest = new HttpEvent(200L);
        secondRequest.setRequestDuration(8L);

        assertFalse(bs.update(secondRequest));

        assertEquals(15L, bs.getDuration());
        assertEquals(2, bs.getRequestCount());
        assertEquals(100L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertEquals("TYPE-A", bs.getType());
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());

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
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        assertTrue(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(11L, bs.getEndTimestamp());
        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.CLOSED, bs.getState());

        HttpEvent e3 = new HttpEvent(7L);

        try {
            bs.update(e3);
            fail("should have thrown exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.CLOSED, bs.getState());
    }

    @Test
    public void update_SameStopMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertFalse(bs.isClosed());
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-A");

        assertTrue(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(11L, bs.getEndTimestamp());
        assertTrue(bs.isClosed());
        assertEquals(BusinessScenarioState.CLOSED, bs.getState());

        HttpEvent e3 = new HttpEvent(7L);

        try {
            bs.update(e3);
            fail("should have thrown exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.CLOSED, bs.getState());
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
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());

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
        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());

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
        assertFalse(bs.isActive());

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
        assertFalse(bs.isActive());
        assertEquals(0, bs.getRequestCount());
        assertEquals(0, bs.getDuration());
        assertNull(bs.getType());
        assertEquals(BusinessScenarioState.NEW, bs.getState());
    }

    // toEvent() -------------------------------------------------------------------------------------------------------

    @Test
    public void toEvent() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(777L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(7L);

        assertFalse(bs.update(e));

        HttpEvent e2 = new HttpEvent(888L);
        e2.setRequestDuration(8L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-A");

        assertTrue(bs.update(e2));

        assertTrue(bs.isClosed());

        BusinessScenarioEvent bse = bs.toEvent();

        assertEquals(777L, bse.getTimestamp().longValue());
        assertEquals(15L, bse.getLongProperty(BusinessScenarioEvent.DURATION).getLong().longValue());
        assertEquals(2, bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT).getInteger().intValue());
        assertEquals("TYPE-A", bse.getStringProperty(BusinessScenarioEvent.TYPE).getValue());
        assertEquals(BusinessScenarioState.CLOSED.name(),
                bse.getStringProperty(BusinessScenarioEvent.STATE).getValue());
    }

    @Test
    public void toEvent_SpecialClosedState() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        bs.setState(BusinessScenarioState.CLOSED_BY_START_MARKER);
        bs.setType("SOME-TYPE");
        bs.setBeginTimestamp(101L);
        bs.updateCounters(11L);
        bs.updateCounters(null);
        bs.updateCounters(22L);

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
        assertEquals(BusinessScenarioState.CLOSED_EXPLICITLY, bs.getState());
    }

    @Test
    public void close_ACTIVE() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestDuration(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TEST");

        assertFalse(bs.update(e));

        assertEquals(BusinessScenarioState.ACTIVE, bs.getState());

        bs.close();

        assertFalse(bs.isNew());
        assertFalse(bs.isActive());
        assertEquals(BusinessScenarioState.CLOSED_EXPLICITLY, bs.getState());
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

        assertEquals(BusinessScenarioState.CLOSED, bs.getState());

        try {
            bs.close();
            fail("should throw exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }

        assertEquals(BusinessScenarioState.CLOSED, bs.getState());
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
