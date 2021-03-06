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

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.FaultEvent;
import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.api.event.LongProperty;
import io.novaordis.events.httpd.HttpEvent;
import io.novaordis.utilities.time.TimestampImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class BusinessScenarioCommandTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BusinessScenarioCommandTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Static Private --------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // process() -------------------------------------------------------------------------------------------------------

    @Test
    public void process_EndOfStream() throws Exception {

        BusinessScenarioCommand c = new BusinessScenarioCommand();

        //
        // initialize two sessions - one has a NEW scenario and one has an OPEN scenario
        //

        HttpEvent e = new HttpEvent(new TimestampImpl(1));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        List<Event> events = c.processHttpEvent(e);
        assertTrue(events.isEmpty());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(1));
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "scenario-1");

        events = c.processHttpEvent(e2);
        assertEquals(1, events.size());
        BusinessScenarioEvent bse = (BusinessScenarioEvent)events.get(0);
        assertEquals(BusinessScenarioState.COMPLETE.name(),
                bse.getStringProperty(BusinessScenarioEvent.STATE_PROPERTY_NAME).getString());


        HttpEvent e3 = new HttpEvent(new TimestampImpl(1));
        e3.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-2");
        e3.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-2");
        e3.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 7L));

        events = c.processHttpEvent(e3);
        assertTrue(events.isEmpty());

        List<Event> output = c.process(new EndOfStreamEvent());
        assertEquals(2, output.size());

        BusinessScenarioEvent bse2 = (BusinessScenarioEvent)output.get(0);
        assertEquals(BusinessScenarioState.INCOMPLETE.name(),
                bse2.getStringProperty(BusinessScenarioEvent.STATE_PROPERTY_NAME).getString());
        assertEquals(7L, bse2.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(1, bse2.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
        assertEquals("scenario-2", bse2.getStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME).getString());

        EndOfStreamEvent eose = (EndOfStreamEvent)output.get(1);
        assertNotNull(eose);
    }

    // processHttpEvent() ----------------------------------------------------------------------------------------------

    @Test
    public void processHttpEvent_NoJSESSIONID() throws Exception {

        BusinessScenarioCommand c = new BusinessScenarioCommand();

        HttpEvent e = new HttpEvent(new TimestampImpl(1));

        List<Event> events = c.processHttpEvent(e);
        assertEquals(1, events.size());
        FaultEvent fe = (FaultEvent)events.get(0);

        String s = fe.getMessage();
        assertTrue(s.matches("HTTP request .* does not carry a \"" + HttpEvent.JSESSIONID_COOKIE_KEY + "\" cookie"));
    }

    @Test
    public void processHttpEvent() throws Exception {

        BusinessScenarioCommand c = new BusinessScenarioCommand();

        //noinspection Convert2Lambda,Anonymous2MethodRef
        c.setHttpSessionFactory(new HttpSessionFactory() {
            @Override
            public HttpSession create() {
                return new MockHttpSession();
            }
        });

        HttpEvent e = new HttpEvent(new TimestampImpl(1));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        List<Event> events = c.processHttpEvent(e);
        assertTrue(events.isEmpty());

        HttpEvent e2 = new HttpEvent(new TimestampImpl(1));
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        events = c.processHttpEvent(e2);
        assertTrue(events.isEmpty());

        HttpEvent e3 = new HttpEvent(new TimestampImpl(1));
        e3.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e3.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        e3.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        events = c.processHttpEvent(e3);
        assertEquals(1, events.size());
        BusinessScenarioEvent bse = (BusinessScenarioEvent)events.get(0);
        assertNotNull(bse);

        IntegerProperty ip = bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME);
        assertEquals(3, ip.getInteger().intValue());

        LongProperty lp = bse.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME);
        assertEquals(3, lp.getLong().longValue());
    }

    @Test
    public void processHttpEvent2() throws Exception {

        BusinessScenarioCommand c = new BusinessScenarioCommand();

        //
        // this will be ignored, as it does not carry any cookie
        //

        HttpEvent e = new HttpEvent(new TimestampImpl(1));
        assertNull(e.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestUri("/test/A");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 1L);

        List<Event> re = c.processHttpEvent(e);

        assertEquals(1, re.size());
        FaultEvent fe = (FaultEvent)re.get(0);
        log.info((fe).getMessage());

        e = new HttpEvent(new TimestampImpl(1));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-001");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestUri("/test/A");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 1L);

        re = c.processHttpEvent(e);
        assertTrue(re.isEmpty());

        e = new HttpEvent(new TimestampImpl(1));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-002");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestUri("/test/A");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 10L);

        re = c.processHttpEvent(e);
        assertTrue(re.isEmpty());

        e = new HttpEvent(new TimestampImpl(1));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-001");
        e.setRequestUri("/test/B");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 2L);

        re = c.processHttpEvent(e);
        assertTrue(re.isEmpty());

        e = new HttpEvent(new TimestampImpl(1));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-002");
        e.setRequestUri("/test/B");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 20L);

        re = c.processHttpEvent(e);
        assertTrue(re.isEmpty());

        e = new HttpEvent(new TimestampImpl(1));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-001");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);
        e.setRequestUri("/test/C");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 3L);

        List<Event> re2 = c.processHttpEvent(e);
        assertEquals(1, re2.size());
        BusinessScenarioEvent bs = (BusinessScenarioEvent)re2.get(0);

        e = new HttpEvent(new TimestampImpl(1));
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-002");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);
        e.setRequestUri("/test/C");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 30L);

        List<Event> re3 = c.processHttpEvent(e);
        assertEquals(1, re3.size());
        BusinessScenarioEvent bs2 = (BusinessScenarioEvent)re3.get(0);

        assertEquals(6L, bs.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(3, bs.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());

        assertEquals(60L, bs2.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
        assertEquals(3, bs2.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
    }

    //
    // TODO N7aq32 RETURN HERE
    //

//    @Test
//    public void endToEnd_TwoSuccessiveScenarios() throws Exception {
//
//        BusinessScenarioCommand c = new BusinessScenarioCommand();
//
//        HttpEvent e = new HttpEvent(new TimestampImpl(1));
//        assertNull(e.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY));
//        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
//        e.setRequestUri("/test/A");
//        e.setLongProperty(HttpEvent.REQUEST_DURATION, 1L);
//
//        List<Event> re = c.processHttpEvent(e);
//
//        assertEquals(1, re.size());
//        FaultEvent fe = (FaultEvent)re.get(0);
//        log.info((fe).getMessage());
//
//        e = new HttpEvent(new TimestampImpl(1));
//        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-001");
//        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
//        e.setRequestUri("/test/A");
//        e.setLongProperty(HttpEvent.REQUEST_DURATION, 1L);
//
//        re = c.processHttpEvent(e);
//        assertTrue(re.isEmpty());
//
//        e = new HttpEvent(new TimestampImpl(1));
//        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-002");
//        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
//        e.setRequestUri("/test/A");
//        e.setLongProperty(HttpEvent.REQUEST_DURATION, 10L);
//
//        re = c.processHttpEvent(e);
//        assertTrue(re.isEmpty());
//
//        e = new HttpEvent(new TimestampImpl(1));
//        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-001");
//        e.setRequestUri("/test/B");
//        e.setLongProperty(HttpEvent.REQUEST_DURATION, 2L);
//
//        re = c.processHttpEvent(e);
//        assertTrue(re.isEmpty());
//
//        e = new HttpEvent(new TimestampImpl(1));
//        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-002");
//        e.setRequestUri("/test/B");
//        e.setLongProperty(HttpEvent.REQUEST_DURATION, 20L);
//
//        re = c.processHttpEvent(e);
//        assertTrue(re.isEmpty());
//
//        e = new HttpEvent(new TimestampImpl(1));
//        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-001");
//        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);
//        e.setRequestUri("/test/C");
//        e.setLongProperty(HttpEvent.REQUEST_DURATION, 3L);
//
//        List<Event> re2 = c.processHttpEvent(e);
//        assertEquals(1, re2.size());
//        BusinessScenarioEvent bs = (BusinessScenarioEvent)re2.get(0);
//
//        e = new HttpEvent(new TimestampImpl(1));
//        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-002");
//        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);
//        e.setRequestUri("/test/C");
//        e.setLongProperty(HttpEvent.REQUEST_DURATION, 30L);
//
//        List<Event> re3 = c.processHttpEvent(e);
//        assertEquals(1, re3.size());
//        BusinessScenarioEvent bs2 = (BusinessScenarioEvent)re3.get(0);
//
//        assertEquals(6L, bs.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
//        assertEquals(3, bs.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
//
//        assertEquals(60L, bs2.getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME).getLong().longValue());
//        assertEquals(3, bs2.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME).getInteger().intValue());
//    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
