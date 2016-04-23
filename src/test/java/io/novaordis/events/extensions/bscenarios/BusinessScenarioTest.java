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

        bs.close(System.currentTimeMillis());

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
    public void update_SuccessiveStartMarkers() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        assertNull(bs.getType());
        assertEquals(0L, bs.getBeginTimestamp());

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals("TYPE-A", bs.getType());
        assertFalse(bs.isClosed());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getDuration());

        HttpEvent e2 = new HttpEvent(2L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e2.setRequestDuration(2L);

        try {

            bs.update(e2);
            fail("should throw exception");
        }
        catch(UserErrorException ex) {
            String msg = ex.getMessage();
            log.info(msg);
            assertTrue(msg.matches("a start marker .* arrived on the already opened scenario .*"));
        }
    }

    @Test
    public void update_NoDuration_StartMarker() throws Exception {

        BusinessScenario bs = new BusinessScenario();

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
    }

    @Test
    public void update_NoDuration_RegularRequest() throws Exception {

        BusinessScenario bs = new BusinessScenario();
        assertNull(bs.getType());

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertEquals("TYPE-A", bs.getType());
        assertFalse(bs.isClosed());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(1, bs.getRequestCount());
        assertEquals(1L, bs.getDuration());

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
    }

    @Test
    public void update() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        assertEquals(0L, bs.getDuration());
        assertEquals(0, bs.getRequestCount());
        assertEquals(0L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertNull(bs.getType());

        HttpEvent firstRequest = new HttpEvent(100L);
        firstRequest.setRequestDuration(7L);
        firstRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");

        assertFalse(bs.update(firstRequest));

        assertEquals(7L, bs.getDuration());
        assertEquals(1, bs.getRequestCount());
        assertEquals(100L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertEquals("TYPE-A", bs.getType());

        HttpEvent secondRequest = new HttpEvent(200L);
        secondRequest.setRequestDuration(8L);

        assertFalse(bs.update(secondRequest));

        assertEquals(15L, bs.getDuration());
        assertEquals(2, bs.getRequestCount());
        assertEquals(100L, bs.getBeginTimestamp());
        assertEquals(0L, bs.getEndTimestamp());
        assertEquals("TYPE-A", bs.getType());

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

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        assertTrue(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(11L, bs.getEndTimestamp());
        assertTrue(bs.isClosed());

        HttpEvent e3 = new HttpEvent(7L);

        try {
            bs.update(e3);
            fail("should have thrown exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }
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

        HttpEvent e2 = new HttpEvent(5L);
        e2.setRequestDuration(6L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "TYPE-A");

        assertTrue(bs.update(e2));

        assertEquals(2, bs.getRequestCount());
        assertEquals(1L, bs.getBeginTimestamp());
        assertEquals(11L, bs.getEndTimestamp());
        assertTrue(bs.isClosed());

        HttpEvent e3 = new HttpEvent(7L);

        try {
            bs.update(e3);
            fail("should have thrown exception");
        }
        catch(IllegalStateException ex) {
            log.info(ex.getMessage());
        }
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
    }

    @Test
    public void update_MissingStopMarker_NewStartMarker_SameType() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        HttpEvent e = new HttpEvent(1L);
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");
        e.setRequestDuration(1L);

        assertFalse(bs.update(e));

        assertFalse(bs.isClosed());

        HttpEvent e2 = new HttpEvent(2L);
        e2.setRequestDuration(2L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "TYPE-A");

        try {
            bs.update(e2);
            fail("should have thrown exception");
        }
        catch(UserErrorException ex) {
            String msg = ex.getMessage();
            log.info(msg);
            assertTrue(msg.matches("a start marker .* arrived on the already opened scenario .*"));
        }
    }

    @Test
    public void update_UnknownRequestShouldThrowException() throws Exception {

        BusinessScenario bs = new BusinessScenario();

        //
        // this scenario was not started
        //

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
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
