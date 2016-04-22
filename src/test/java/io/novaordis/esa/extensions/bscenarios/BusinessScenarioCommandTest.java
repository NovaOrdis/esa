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

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNull;
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
    public void process_NoJSESSIONID() throws Exception {

        BusinessScenarioCommand c = new BusinessScenarioCommand();

        HttpEvent e = new HttpEvent(0L);

        FaultEvent fe = (FaultEvent)c.process(e);

        String s = fe.getMessage();
        assertTrue(s.matches("HTTP request .* does not carry a \"" + HttpEvent.JSESSIONID_COOKIE_KEY + "\" cookie"));
    }

    @Test
    public void process() throws Exception {

        BusinessScenarioCommand c = new BusinessScenarioCommand();

        //noinspection Convert2Lambda,Anonymous2MethodRef
        c.setHttpSessionFactory(new HttpSessionFactory() {
            @Override
            public HttpSession create() {
                return new MockHttpSession();
            }
        });

        HttpEvent e = new HttpEvent(0L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));

        assertNull(c.process(e));

        HttpEvent e2 = new HttpEvent(0L);
        e2.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e2.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        assertNull(c.process(e2));

        HttpEvent e3 = new HttpEvent(0L);
        e3.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "test-session-1");
        e3.setProperty(new LongProperty(HttpEvent.REQUEST_DURATION, 1L));
        e3.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        BusinessScenarioEvent bse = (BusinessScenarioEvent)c.process(e3);
        assertNotNull(bse);

        IntegerProperty ip = bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT);
        assertEquals(3, ip.getInteger().intValue());

        LongProperty lp = bse.getLongProperty(BusinessScenarioEvent.DURATION);
        assertEquals(3, lp.getLong().longValue());
    }

    @Test
    public void process2() throws Exception {

        BusinessScenarioCommand c = new BusinessScenarioCommand();

        //
        // this will be ignored, as it does not carry any cookie
        //

        HttpEvent e = new HttpEvent(1L);
        assertNull(e.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY));
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestUri("/test/A");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 1L);

        Event re = c.process(e);

        assertTrue(re instanceof FaultEvent);
        log.info(((FaultEvent) re).getMessage());

        e = new HttpEvent(2L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-001");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestUri("/test/A");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 1L);

        re = c.process(e);
        assertNull(re);

        e = new HttpEvent(3L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-002");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME, "scenario-1");
        e.setRequestUri("/test/A");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 10L);

        re = c.process(e);
        assertNull(re);

        e = new HttpEvent(4L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-001");
        e.setRequestUri("/test/B");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 2L);

        re = c.process(e);
        assertNull(re);

        e = new HttpEvent(5L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-002");
        e.setRequestUri("/test/B");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 20L);

        re = c.process(e);
        assertNull(re);

        e = new HttpEvent(6L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-001");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);
        e.setRequestUri("/test/C");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 3L);

        BusinessScenarioEvent bs = (BusinessScenarioEvent)c.process(e);

        e = new HttpEvent(7L);
        e.setCookie(HttpEvent.JSESSIONID_COOKIE_KEY, "cookie-002");
        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);
        e.setRequestUri("/test/C");
        e.setLongProperty(HttpEvent.REQUEST_DURATION, 30L);

        BusinessScenarioEvent bs2 = (BusinessScenarioEvent)c.process(e);

        assertEquals(6L, bs.getLongProperty(BusinessScenarioEvent.DURATION).getLong().longValue());
        assertEquals(3, bs.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT).getInteger().intValue());

        assertEquals(60L, bs2.getLongProperty(BusinessScenarioEvent.DURATION).getLong().longValue());
        assertEquals(3, bs2.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT).getInteger().intValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
