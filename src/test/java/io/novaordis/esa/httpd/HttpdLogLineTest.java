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

package io.novaordis.esa.httpd;

import io.novaordis.esa.core.event.IntegerProperty;
import io.novaordis.esa.core.event.MapProperty;
import io.novaordis.esa.core.event.StringProperty;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogLineTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLogLineTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void noSuchFormatElement() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        //noinspection Convert2Lambda
        Object value = e.getLogValue(new MockFormatString("no-such-format-element"));

        assertNull(value);
    }

    @Test
    public void setValue_CorrectType() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        MockFormatString formatElement = new MockFormatString("LONG_MOCK", Long.class);

        Object old = e.setLogValue(formatElement, 1L);
        assertNull(old);

        assertEquals(1L, e.getLogValue(formatElement));

        Object old2 = e.setLogValue(formatElement, 2L);
        assertEquals(1L, old2);

        assertEquals(2L, e.getLogValue(formatElement));

        Set<FormatString> formatStrings = e.getFormatStrings();
        assertEquals(1, formatStrings.size());
        assertEquals(formatElement, formatStrings.iterator().next());
    }

    @Test
    public void setValue_null() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        MockFormatString formatElement = new MockFormatString("LONG_MOCK", Long.class);

        Object old = e.setLogValue(formatElement, null);
        assertNull(old);

        Object old2 = e.setLogValue(formatElement, 1L);
        assertNull(old2);

        assertEquals(1L, e.getLogValue(formatElement));

        Object old3 = e.setLogValue(formatElement, null);
        assertEquals(1L, old3);

        assertNull(e.getLogValue(formatElement));
    }

    @Test
    public void setValue_IncorrectType() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        MockFormatString formatElement = new MockFormatString("LONG_MOCK", Long.class);

        try {
            e.setLogValue(formatElement, "this should cause failure");
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException iae) {
            log.info(iae.getMessage());
        }
    }

    @Test
    public void setAndGetTimestamp() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        assertNull(e.getTimestamp());

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(1L));

        assertEquals(1L, e.getTimestamp().longValue());

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(2L));

        assertEquals(2L, e.getTimestamp().longValue());

        Set<FormatString> formatStrings = e.getFormatStrings();
        assertEquals(1, formatStrings.size());
        assertEquals(FormatStrings.TIMESTAMP, formatStrings.iterator().next());
    }

    // toEvent() -------------------------------------------------------------------------------------------------------

    @Test
    public void toEvent_NoTimestamp() throws Exception {

        HttpdLogLine logLine = new HttpdLogLine();
        assertNull(logLine.getTimestamp());
        HttpEvent event = logLine.toEvent();
        assertNull(event.getTimestamp());
    }

    @Test
    public void toEvent_ValidTimestamp() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(1L));

        HttpEvent event = e.toEvent();

        assertEquals(1L, event.getTimestamp().longValue());
    }

    @Test
    public void toEvent() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(1L));
        e.setLogValue(FormatStrings.FIRST_REQUEST_LINE, "PUT /test/ HTTP/1.1");
        e.setLogValue(FormatStrings.ORIGINAL_REQUEST_STATUS_CODE, 404);
        e.setLogValue(FormatStrings.THREAD_NAME, "some thread name XXX-100");

        HttpEvent event = e.toEvent();

        assertEquals(1L, event.getTimestamp().longValue());
        assertEquals(HTTPMethod.PUT.name(), ((StringProperty)event.getProperty(HttpEvent.METHOD)).getString());
        assertEquals("/test/", ((StringProperty)event.getProperty(HttpEvent.PATH)).getString());
        assertEquals("HTTP/1.1", ((StringProperty) event.getProperty(HttpEvent.HTTP_VERSION)).getString());
        assertEquals(404, ((IntegerProperty)event.
                getProperty(HttpEvent.ORIGINAL_REQUEST_STATUS_CODE)).getInteger().intValue());
        assertNull(event.getProperty(HttpEvent.STATUS_CODE));
        assertEquals("some thread name XXX-100", ((StringProperty)event.getProperty(HttpEvent.THREAD_NAME)).getString());

        // "default task-1" 127.0.0.1 - "Module=CentricIdentityProvider&Operation=Login&LoginID=Administrator&Password=centric8" 74 27
    }

    @Test
    public void toEvent_StatusCode() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(1L));
        e.setLogValue(FormatStrings.STATUS_CODE, 200);

        HttpEvent event = e.toEvent();

        assertEquals(200, ((IntegerProperty) event.getProperty(HttpEvent.STATUS_CODE)).getInteger().intValue());
    }

    @Test
    public void toEvent_Both_StatusCode_OriginalRequestStatusCode() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(1L));
        e.setLogValue(FormatStrings.ORIGINAL_REQUEST_STATUS_CODE, 301);
        e.setLogValue(FormatStrings.STATUS_CODE, 302);

        HttpEvent event = e.toEvent();

        assertEquals(301, ((IntegerProperty) event.
                getProperty(HttpEvent.ORIGINAL_REQUEST_STATUS_CODE)).getInteger().intValue());
        assertEquals(302, ((IntegerProperty) event.getProperty(HttpEvent.STATUS_CODE)).getInteger().intValue());
    }

    @Test
    public void toEvent_OneRequestHeader() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(1L));
        e.setLogValue(new RequestHeaderFormatString("%{i,Test-Header}"), "header value");

        HttpEvent event = e.toEvent();

        MapProperty headers = (MapProperty)event.getProperty(HttpEvent.REQUEST_HEADERS);
        assertEquals(HttpEvent.REQUEST_HEADERS, headers.getName());
        assertEquals(Map.class, headers.getType());
        Map map = headers.getMap();

        assertEquals(1, map.size());
        assertEquals("header value", map.get("Test-Header"));
    }

    @Test
    public void toEvent_TwoRequestHeaders() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(1L));
        e.setLogValue(new RequestHeaderFormatString("%{i,Test-Header}"), "header value");
        e.setLogValue(new RequestHeaderFormatString("%{i,Another-Test-Header}"), "header value 2");

        HttpEvent event = e.toEvent();

        MapProperty headers = (MapProperty)event.getProperty(HttpEvent.REQUEST_HEADERS);
        assertEquals(HttpEvent.REQUEST_HEADERS, headers.getName());
        assertEquals(Map.class, headers.getType());
        Map map = headers.getMap();

        assertEquals(2, map.size());
        assertEquals("header value", map.get("Test-Header"));
        assertEquals("header value 2", map.get("Another-Test-Header"));
    }

    @Test
    public void toEvent_OneCookie() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(1L));
        e.setLogValue(new CookieFormatString("%{c,TestCookie}"), "test-cookie-value");

        HttpEvent event = e.toEvent();

        MapProperty cookies = (MapProperty)event.getProperty(HttpEvent.COOKIES);
        assertEquals(HttpEvent.COOKIES, cookies.getName());
        assertEquals(Map.class, cookies.getType());
        Map map = cookies.getMap();

        assertEquals(1, map.size());
        assertEquals("test-cookie-value", map.get("TestCookie"));
    }

    @Test
    public void toEvent_TwoCookies() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(FormatStrings.TIMESTAMP, new Date(1L));
        e.setLogValue(new CookieFormatString("%{c,TestCookie}"), "test-cookie-value");
        e.setLogValue(new CookieFormatString("%{c,AnotherTestCookie}"), "another-test-cookie-value");

        HttpEvent event = e.toEvent();

        MapProperty cookies = (MapProperty)event.getProperty(HttpEvent.COOKIES);
        assertEquals(HttpEvent.COOKIES, cookies.getName());
        assertEquals(Map.class, cookies.getType());
        Map map = cookies.getMap();

        assertEquals(2, map.size());
        assertEquals("test-cookie-value", map.get("TestCookie"));
        assertEquals("another-test-cookie-value", map.get("AnotherTestCookie"));
    }

    // parseFirstRequestLine() -----------------------------------------------------------------------------------------

    @Test
    public void parseFirstRequestLine_Null() throws Exception {

        try {
            HttpdLogLine.parseFirstRequestLine(null);
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void parseFirstRequestLine_TwoElements() throws Exception {

        try {
            HttpdLogLine.parseFirstRequestLine("GET /a");
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void parseFirstRequestLine_FourElements() throws Exception {

        try {
            HttpdLogLine.parseFirstRequestLine("GET /a HTTP/1.1 something");
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void parseFirstRequestLine_InvalidHttpMethod() throws Exception {

        try {
            HttpdLogLine.parseFirstRequestLine("nosuchmethod /test HTTP/1.1");
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void parseFirstRequestLine_InvalidHttpVersion() throws Exception {

        try {
            HttpdLogLine.parseFirstRequestLine("POST /test HTTP/2.2");
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void parseFirstRequestLine() throws Exception {

        String[] s = HttpdLogLine.parseFirstRequestLine("GET /test/index.html HTTP/1.1");
        assertEquals(3, s.length);
        assertEquals(HTTPMethod.GET.name(), s[0]);
        assertEquals("/test/index.html", s[1]);
        assertEquals("HTTP/1.1", s[2]);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
