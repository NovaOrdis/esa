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

import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.FaultEvent;
import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.httpd.HttpEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    public void processBusinessScenario_EventDoesNotBelongToTheSession() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        HttpEvent e = new HttpEvent(0L);

        try {
            s.processBusinessScenario(e);
            fail("should have thrown exception, the request is not associated with the session");
        }
        catch(IllegalArgumentException ex) {
            log.info(ex.getMessage());
        }
    }

    @Test
    public void processBusinessScenario_JSessionIDPresent_NoBusinessScenarioMarker() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        HttpEvent e = new HttpEvent(0L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        FaultEvent fe = (FaultEvent)s.processBusinessScenario(e);

        String msg = fe.getMessage();
        log.info(msg);
        assertTrue(msg.matches("HTTP request .* does not belong to any business scenario"));
    }

    @Test
    public void processBusinessScenario() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        Event re;

        HttpEvent e = new HttpEvent(0L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        re = s.processBusinessScenario(e);
        assertNull(re);

        HttpEvent e2 = new HttpEvent(0L);
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        re = s.processBusinessScenario(e2);
        assertNull(re);

        HttpEvent e3 = new HttpEvent(0L);
        e3.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e3.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        e3.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        re = s.processBusinessScenario(e3);
        assertNotNull(re);

        BusinessScenarioEvent bse = (BusinessScenarioEvent)re;

        IntegerProperty ip = bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT);
        assertEquals(3, ip.getInteger().intValue());

        LongProperty lp = bse.getLongProperty(BusinessScenarioEvent.DURATION);
        assertEquals(3, lp.getLong().longValue());

        //
        // successive scenario
        //

        HttpEvent e4 = new HttpEvent(0L);
        e4.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e4.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e4.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        re = s.processBusinessScenario(e4);
        assertNull(re);

        HttpEvent e5 = new HttpEvent(0L);
        e5.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e5.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        e5.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        re = s.processBusinessScenario(e3);
        assertNotNull(re);

        BusinessScenarioEvent bse2 = (BusinessScenarioEvent)re;

        IntegerProperty ip2 = bse2.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT);
        assertEquals(2, ip2.getInteger().intValue());

        LongProperty lp2 = bse2.getLongProperty(BusinessScenarioEvent.DURATION);
        assertEquals(2, lp2.getLong().longValue());
    }

    @Test
    public void processBusinessScenario_StopMarkerSameTypeLabel() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        Event re;

        HttpEvent e = new HttpEvent(0L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        re = s.processBusinessScenario(e);
        assertNull(re);

        HttpEvent e2 = new HttpEvent(0L);
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "scenario-1");

        re = s.processBusinessScenario(e2);
        assertNotNull(re);

        BusinessScenarioEvent bse = (BusinessScenarioEvent)re;

        IntegerProperty ip = bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT);
        assertEquals(2, ip.getInteger().intValue());

        LongProperty lp = bse.getLongProperty(BusinessScenarioEvent.DURATION);
        assertEquals(2, lp.getLong().longValue());
    }

    @Test
    public void processBusinessScenario_StopMarkerDifferentTypeLabel() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        Event re;

        HttpEvent e = new HttpEvent(1L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestDuration(1L);

        re = s.processBusinessScenario(e);
        assertNull(re);

        HttpEvent e2 = new HttpEvent(2L);
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setRequestDuration(2L);
        e2.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME, "scenario-2");

        try {
            s.processBusinessScenario(e2);
            fail("should throw exception");
        }
        catch(IllegalArgumentException ex) {
            log.info(ex.getMessage());
        }
    }

    @Test
    public void processBusinessScenario_InvalidRequest() throws Exception {

        HttpSession s = new HttpSession("test-session-1");

        Event re;

        HttpEvent e = new HttpEvent(1L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestDuration(1L);

        re = s.processBusinessScenario(e);
        assertNull(re);

        HttpEvent noDurationRequest = new HttpEvent(2L);
        noDurationRequest.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");

        FaultEvent fe = (FaultEvent)s.processBusinessScenario(noDurationRequest);
        log.info("" + fe);
    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
