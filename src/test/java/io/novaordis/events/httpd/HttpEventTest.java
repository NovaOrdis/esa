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

package io.novaordis.events.httpd;

import io.novaordis.events.core.event.MapProperty;
import io.novaordis.events.core.event.TimedEventTest;
import io.novaordis.events.extensions.bscenarios.BusinessScenario;
import io.novaordis.utilities.time.TimeOffset;
import io.novaordis.utilities.time.TimestampImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class HttpEventTest extends TimedEventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpEventTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        HttpEvent e = new HttpEvent(new TimestampImpl(1L, new TimeOffset("-0800")));
        assertEquals(1L, e.getTime().longValue());
        assertEquals(new TimeOffset("-0800"), e.getTimestamp().getTimeOffset());
    }

    // getCookie() -----------------------------------------------------------------------------------------------------

    @Test
    public void getCookie_NoCookie() throws Exception {

        HttpEvent e = getEventToTest(0L);
        assertNull(e.getCookie("no-such-cookie"));
    }

    @Test
    public void getCookie_NoCookiesInMap() throws Exception {

        HttpEvent e = getEventToTest(0L);
        MapProperty mp = new MapProperty(HttpEvent.COOKIES);
        e.setProperty(mp);
        assertNull(e.getCookie("no-such-cookie"));
    }

    @Test
    public void getCookie() throws Exception {

        HttpEvent e = getEventToTest(0L);
        MapProperty mp = new MapProperty(HttpEvent.COOKIES);
        mp.getMap().put("test-cookie-name", "test-cookie-value");
        e.setProperty(mp);

        assertEquals("test-cookie-value", e.getCookie("test-cookie-name"));
    }

    // setCookie() -----------------------------------------------------------------------------------------------------

    @Test
    public void setCookie() throws Exception {

        HttpEvent e = getEventToTest(0L);
        assertNull(e.getCookie("test-cookie-name"));
        e.setCookie("test-cookie-name", "test-cookie-value");
        assertEquals("test-cookie-value", e.getCookie("test-cookie-name"));
    }

    // getRequestHeader() ----------------------------------------------------------------------------------------------

    @Test
    public void getRequestHeader_NoHeader() throws Exception {

        HttpEvent e = getEventToTest(0L);
        assertNull(e.getRequestHeader("no-such-header"));
    }

    @Test
    public void getRequestHeader_NoHeadersInMap() throws Exception {

        HttpEvent e = getEventToTest(0L);
        MapProperty mp = new MapProperty(HttpEvent.REQUEST_HEADERS);
        e.setProperty(mp);
        assertNull(e.getRequestHeader("no-such-header"));
    }

    @Test
    public void getRequestHeader() throws Exception {

        HttpEvent e = getEventToTest(0L);
        MapProperty mp = new MapProperty(HttpEvent.REQUEST_HEADERS);
        mp.getMap().put("test-header-name", "test-header-value");
        e.setProperty(mp);

        assertEquals("test-header-value", e.getRequestHeader("test-header-name"));
    }

    // setCookie() -----------------------------------------------------------------------------------------------------

    @Test
    public void setRequestHeader() throws Exception {

        HttpEvent e = getEventToTest(0L);
        assertNull(e.getRequestHeader("test-header-name"));
        e.setRequestHeader("test-header-name", "test-header-value");
        assertEquals("test-header-value", e.getRequestHeader("test-header-name"));
    }

    @Test
    public void setRequestHeader_EmptyHeaderBody() throws Exception {

        HttpEvent e = getEventToTest(0L);
        assertNull(e.getRequestHeader("test-header-name"));
        assertNull(e.getRequestHeader("test-header-name-2"));

        e.setRequestHeader("test-header-name");
        assertEquals("", e.getRequestHeader("test-header-name"));

        e.setRequestHeader("est-header-name-2", null);
        assertEquals("", e.getRequestHeader("est-header-name-2"));
    }

    // setRequestUri()/getRequestUri() ---------------------------------------------------------------------------------

    @Test
    public void requestUri() throws Exception {

        HttpEvent e = getEventToTest(0L);
        assertNull(e.getRequestUri());
        e.setRequestUri("/test/");
        assertEquals("/test/", e.getRequestUri());
    }

    // setRequestDuration()/getRequestDuration() -----------------------------------------------------------------------

    @Test
    public void requestDuration() throws Exception {

        HttpEvent e = getEventToTest(0L);
        assertNull(e.getRequestDuration());
        e.setRequestDuration(7L);
        assertEquals(7L, e.getRequestDuration().longValue());
    }

    // getRequestSequenceId() ------------------------------------------------------------------------------------------

    @Test
    public void getRequestSequenceId() throws Exception {

        HttpEvent e = getEventToTest(1L);
        assertNull(e.getRequestSequenceId());

        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, "something");
        assertEquals("something", e.getRequestSequenceId());
    }

    // getIterationId() ------------------------------------------------------------------------------------------------

    @Test
    public void getIterationId() throws Exception {

        HttpEvent e = getEventToTest(1L);
        assertNull(e.getIterationId());

        e.setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME, "something");
        assertEquals("something", e.getIterationId());
    }

    // getStatusCode()/setStatusCode() ---------------------------------------------------------------------------------

    @Test
    public void getStatusCode_setStatusCode() throws Exception {

        HttpEvent e = getEventToTest(1L);

        assertNull(e.getStatusCode());

        e.setStatusCode(200);
        assertEquals(200, e.getStatusCode().intValue());
    }

    @Test
    public void setStatusCode_InvalidValue_SmallerThan200() throws Exception {

        HttpEvent e = getEventToTest(1L);

        try {

            e.setStatusCode(199);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException iae) {

            String msg = iae.getMessage();
            log.info(msg);
            assertEquals("invalid status code 199", msg);
        }
    }

    @Test
    public void setStatusCode_InvalidValue_LargerThan599() throws Exception {

        HttpEvent e = getEventToTest(1L);

        try {

            e.setStatusCode(600);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException iae) {

            String msg = iae.getMessage();
            log.info(msg);
            assertEquals("invalid status code 600", msg);
        }
    }

    // getFirstRequestLine() -------------------------------------------------------------------------------------------

    @Test
    public void getFirstRequestLine_ThreeElements() throws Exception {

        HttpEvent e = getEventToTest(1L);
        e.setMethod("GET");
        e.setRequestUri("/something?auto");

        // TODO - this will fail someday
        e.setHttpVersion("somethingelse");

        assertEquals("GET /something?auto somethingelse", e.getFirstRequestLine());
    }

    @Test
    public void getFirstRequestLine_TwoElements() throws Exception {

        HttpEvent e = getEventToTest(1L);
        e.setMethod("GET");
        e.setRequestUri("/something?auto");

        assertEquals("GET /something?auto", e.getFirstRequestLine());
        assertNull(e.getHttpVersion());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected HttpEvent getEventToTest(Long timestamp) throws Exception {
        return new HttpEvent(timestamp == null ? null : new TimestampImpl(timestamp));
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
