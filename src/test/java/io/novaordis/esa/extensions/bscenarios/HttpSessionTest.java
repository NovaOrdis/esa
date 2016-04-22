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

package io.novaordis.esa.extensions.bscenarios;

import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.FaultEvent;
import io.novaordis.esa.core.event.IntegerProperty;
import io.novaordis.esa.core.event.LongProperty;
import io.novaordis.esa.httpd.HttpEvent;
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
        e.setProperty(new LongProperty(HttpEvent.REQUEST_PROCESSING_TIME, 1L));

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
        e.setProperty(new LongProperty(HttpEvent.REQUEST_PROCESSING_TIME, 1L));

        re = s.processBusinessScenario(e);
        assertNull(re);

        HttpEvent e2 = new HttpEvent(0L);
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setProperty(new LongProperty(HttpEvent.REQUEST_PROCESSING_TIME, 1L));

        re = s.processBusinessScenario(e2);
        assertNull(re);

        HttpEvent e3 = new HttpEvent(0L);
        e3.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e3.setProperty(new LongProperty(HttpEvent.REQUEST_PROCESSING_TIME, 1L));
        e3.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        re = s.processBusinessScenario(e3);
        assertNotNull(re);

        BusinessScenarioEvent bse = (BusinessScenarioEvent)re;

        IntegerProperty ip = bse.getIntegerProperty("request-count");
        assertEquals(3, ip.getInteger().intValue());

        LongProperty lp = bse.getLongProperty("total-processing-time");
        assertEquals(3, lp.getLong().longValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
