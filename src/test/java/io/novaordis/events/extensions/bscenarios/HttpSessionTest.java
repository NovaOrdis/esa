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
import io.novaordis.events.core.event.FaultEvent;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.httpd.HttpEvent;
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
 * @since 2/4/16
 */
public class HttpSessionTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpSessionTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        HttpSession s = new HttpSession("test");
        assertEquals("test", s.getJSessionId());
    }

    // processBusinessScenario() ---------------------------------------------------------------------------------------

    @Test
    public void process_RequestDoesNotBelongToTheSession() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        HttpEvent e = new HttpEvent(new TimestampImpl(0L));

        try {
            s.process(e);
            fail("should have thrown exception, the request is not associated with the session");
        }
        catch(UserErrorException ex) {
            String message = ex.getMessage();
            log.info(message);
            assertTrue(message.matches("HTTP request .* does not belong to .*"));
        }
    }

    @Test
    public void process_TwoRequestScenario() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        HttpEvent startRequest = new HttpEvent(new TimestampImpl(7L));
        startRequest.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        startRequest.setLongProperty(HttpEvent.REQUEST_DURATION, 10L);
        startRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");

        List<Event> events = s.process(startRequest);
        assertTrue(events.isEmpty());

        HttpEvent stopRequest = new HttpEvent(new TimestampImpl(8L));
        stopRequest.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        stopRequest.setLongProperty(HttpEvent.REQUEST_DURATION, 20L);
        stopRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "scenario-1");

        List<Event> events2 = s.process(stopRequest);
        assertEquals(1, events2.size());
        BusinessScenarioEvent bse = (BusinessScenarioEvent)events2.get(0);

        assertEquals("scenario-1", bse.getStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME).getValue());
        assertEquals(2, bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
        assertEquals(30L, bse.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(7L, bse.getTime().longValue());
    }

    @Test
    public void process_JSessionIDPresent_NoBusinessScenarioMarker() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 1L);

        List<Event> events = s.process(e);
        assertEquals(1, events.size());
        FaultEvent fe = (FaultEvent)events.get(0);

        String msg = fe.toString();
        log.info(msg);
        assertTrue(msg.contains("there is no open business scenario for"));

        assertEquals(1, s.getFaultCount());
    }

    @Test
    public void process() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        BusinessScenario bs = s.getCurrentBusinessScenario();
        assertFalse(bs.isOpen());
        assertFalse(bs.isClosed());
        assertNull(bs.getType());
        assertNull(bs.getJSessionId());

        List<Event> re;

        HttpEvent e = new HttpEvent(new TimestampImpl(10L));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        re = s.process(e);
        assertTrue(re.isEmpty());

        bs = s.getCurrentBusinessScenario();
        assertTrue(bs.isOpen());
        assertEquals(1, bs.getRequestCount());
        assertEquals("scenario-1", bs.getType());
        assertEquals("test-session-1", bs.getJSessionId());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(20L));
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        re = s.process(e2);
        assertTrue(re.isEmpty());

        bs = s.getCurrentBusinessScenario();
        assertEquals(2, bs.getRequestCount());

        HttpEvent e3 = new HttpEvent(new TimestampImpl(30L));
        e3.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e3.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        e3.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        re = s.process(e3);
        assertEquals(1, re.size());


        bs = s.getCurrentBusinessScenario();
        //
        // the current business scenario has been replaced with a fresh one
        //
        assertEquals(0, bs.getRequestCount());
        assertFalse(bs.isOpen());
        assertFalse(bs.isClosed());
        assertNull(bs.getType());

        BusinessScenarioEvent bse = (BusinessScenarioEvent)re.get(0);

        assertEquals(3, bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
        assertEquals(3L, bse.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(10L, bse.getTime().longValue());
        assertEquals("scenario-1", bse.getStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME).getString());

        //
        // successive scenario
        //

        HttpEvent e4 = new HttpEvent(new TimestampImpl(40L));
        e4.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e4.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-2");
        e4.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        re = s.process(e4);
        assertTrue(re.isEmpty());

        bs = s.getCurrentBusinessScenario();
        assertTrue(bs.isOpen());
        assertEquals(1, bs.getRequestCount());
        assertEquals("scenario-2", bs.getType());

        HttpEvent e5 = new HttpEvent(new TimestampImpl(50L));
        e5.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e5.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        e5.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        re = s.process(e3);
        assertEquals(1, re.size());

        bs = s.getCurrentBusinessScenario();
        //
        // the current business scenario has been replaced with a fresh one
        //
        assertEquals(0, bs.getRequestCount());
        assertFalse(bs.isOpen());
        assertFalse(bs.isClosed());
        assertNull(bs.getType());

        BusinessScenarioEvent bse2 = (BusinessScenarioEvent)re.get(0);

        assertEquals(2, bse2.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
        assertEquals(2, bse2.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(40L, bse2.getTime().longValue());
        assertEquals("scenario-2", bse2.getStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME).getString());

        assertEquals(5, s.getRequestsProcessedBySessionCount());
    }

    @Test
    public void process_StopMarkerSameTypeLabel() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        List<Event> re;

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 1L);

        re = s.process(e);
        assertTrue(re.isEmpty());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(2L));
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "scenario-1");

        re = s.process(e2);
        assertEquals(1, re.size());

        BusinessScenarioEvent bse = (BusinessScenarioEvent)re.get(0);

        assertEquals(2, bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
        assertEquals(2, bse.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals("scenario-1", bse.getStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME).getString());
    }

    @Test
    public void process_StopMarkerDifferentTypeLabel() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        List<Event> re;

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestDuration(1L);

        re = s.process(e);
        assertTrue(re.isEmpty());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(2L));
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setRequestDuration(2L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "scenario-2");

        try {
            s.process(e2);
            fail("should throw exception");
        }
        catch(UserErrorException ex) {
            log.info(ex.getMessage());
        }
    }

    @Test
    public void process_InvalidRequest() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        List<Event> re;

        HttpEvent e = new HttpEvent(new TimestampImpl(1L));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestDuration(1L);

        re = s.process(e);
        assertTrue(re.isEmpty());

        HttpEvent noDurationRequest = new HttpEvent(new TimestampImpl(2L));
        noDurationRequest.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");

        List<Event> re2 = s.process(noDurationRequest);
        assertEquals(1, re2.size());
        FaultEvent fe = (FaultEvent)re2.get(0);
        log.info("" + fe);
        assertEquals(1, s.getFaultCount());
    }

    @Test
    public void process_StartMarkerArrivesBeforeEndMarker() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        HttpEvent startRequest = new HttpEvent(new TimestampImpl(7L));
        startRequest.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        startRequest.setLongProperty(HttpEvent.REQUEST_DURATION, 10L);
        startRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");

        List<Event> events = s.process(startRequest);
        assertTrue(events.isEmpty());

        HttpEvent secondStartRequest = new HttpEvent(new TimestampImpl(20L));
        secondStartRequest.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        secondStartRequest.setLongProperty(HttpEvent.REQUEST_DURATION, 20L);
        secondStartRequest.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");

        events = s.process(secondStartRequest);
        assertEquals(1, events.size());
        BusinessScenarioEvent bse = (BusinessScenarioEvent)events.get(0);

        assertEquals("scenario-1", bse.getStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME).getValue());
        assertEquals(1, bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
        assertEquals(10L, bse.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(7L, bse.getTime().longValue());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(30L));
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setLongProperty(HttpEvent.REQUEST_DURATION, 30L);

        events = s.process(e2);
        assertTrue(events.isEmpty());

        HttpEvent end = new HttpEvent(new TimestampImpl(40L));
        end.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        end.setLongProperty(HttpEvent.REQUEST_DURATION, 40L);
        end.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "scenario-1");

        events = s.process(end);
        assertEquals(1, events.size());
        BusinessScenarioEvent bse2 = (BusinessScenarioEvent)events.get(0);

        assertEquals("scenario-1", bse2.getStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME).getValue());
        assertEquals(3, bse2.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
        assertEquals(90L, bse2.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(20L, bse2.getTime().longValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
