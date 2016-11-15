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

import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.MapProperty;
import io.novaordis.events.core.event.StringProperty;
import io.novaordis.utilities.time.TimeOffset;
import io.novaordis.utilities.time.Timestamp;
import io.novaordis.utilities.time.TimestampImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Object value = e.getLogValue(new MockHttpdFormatString("no-such-format-element"));

        assertNull(value);
    }

    @Test
    public void setValue_CorrectType() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        MockHttpdFormatString formatElement = new MockHttpdFormatString("LONG_MOCK", Long.class);

        Object old = e.setLogValue(formatElement, 1L);
        assertNull(old);

        assertEquals(1L, e.getLogValue(formatElement));

        Object old2 = e.setLogValue(formatElement, 2L);
        assertEquals(1L, old2);

        assertEquals(2L, e.getLogValue(formatElement));

        Set<HttpdFormatString> httpdFormatStrings = e.getFormatStrings();
        assertEquals(1, httpdFormatStrings.size());
        assertEquals(formatElement, httpdFormatStrings.iterator().next());
    }

    @Test
    public void setValue_null() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        MockHttpdFormatString formatElement = new MockHttpdFormatString("LONG_MOCK", Long.class);

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

        MockHttpdFormatString formatElement = new MockHttpdFormatString("LONG_MOCK", Long.class);

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

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);

        assertEquals(1L, e.getTimestamp().getTime());

        Timestamp t2 = new TimestampImpl(2L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t2);

        assertEquals(2L, e.getTimestamp().getTime());

        Set<HttpdFormatString> httpdFormatStrings = e.getFormatStrings();
        assertEquals(1, httpdFormatStrings.size());
        assertEquals(HttpdFormatStrings.TIMESTAMP, httpdFormatStrings.iterator().next());
    }

    @Test
    public void fullLine() throws Exception {

        HttpdLogLine line = new HttpdLogLine();

        line.setLogValue(HttpdFormatStrings.TIMESTAMP, new TimestampImpl(1L, new TimeOffset("-0800")));
        line.setLogValue(HttpdFormatStrings.REMOTE_HOST, "test.remote.host");
        line.setLogValue(HttpdFormatStrings.REMOTE_LOGNAME, "test.remote.logname");
        line.setLogValue(HttpdFormatStrings.REMOTE_USER, "test.remote.user");
        line.setLogValue(HttpdFormatStrings.QUERY_STRING, "a=b&c=d");
        line.setLogValue(HttpdFormatStrings.FIRST_REQUEST_LINE, "GET /something HTTP/1.1");
        line.setLogValue(HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE, 200);
        line.setLogValue(HttpdFormatStrings.STATUS_CODE, 201);
        line.setLogValue(HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE, 1L);
        line.setLogValue(HttpdFormatStrings.THREAD_NAME, "test.thread.name");
        line.setLogValue(HttpdFormatStrings.REQUEST_PROCESSING_TIME_MS, 7L);
        line.setLogValue(HttpdFormatStrings.REQUEST_PROCESSING_TIME_S, 1.1d);
        line.setLogValue(HttpdFormatStrings.LOCAL_IP_ADDRESS, "test.local.address");
        line.setLogValue(HttpdFormatStrings.LOCAL_SERVER_NAME, "test.local.server.name");
        line.setLogValue(HttpdFormatStrings.BYTES_TRANSFERRED, 8L);

        Timestamp t = line.getTimestamp();
        assertEquals(1L, t.getTime());
        assertEquals(new TimeOffset("-0800"), t.getTimeOffset());

        assertEquals("test.remote.host", line.getRemoteHost());
        assertEquals("test.remote.logname", line.getRemoteLogname());
        assertEquals("test.remote.user", line.getRemoteUser());
        assertEquals("a=b&c=d", line.getQueryString());
        assertEquals("GET /something HTTP/1.1", line.getFirstRequestLine());
        assertEquals(200, line.getOriginalRequestStatusCode().intValue());
        assertEquals(201, line.getStatusCode().intValue());
        assertEquals(1L, line.getResponseEntityBodySize().longValue());
        assertEquals("test.thread.name", line.getThreadName());
        assertEquals(7L, line.getRequestProcessingTimeMs().longValue());
        assertEquals(1.1d, line.getRequestProcessingTimeSec().doubleValue(), 0.0001);
        assertEquals("test.local.address", line.getLocalIpAddress());
        assertEquals("test.local.server.name", line.getLocalServerName());
        assertEquals(8L, line.getBytesTransferred().longValue());

        Set<HttpdFormatString> httpdFormatStrings = line.getFormatStrings();
        assertEquals(15, httpdFormatStrings.size());
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.TIMESTAMP));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.REMOTE_HOST));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.REMOTE_LOGNAME));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.REMOTE_USER));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.QUERY_STRING));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.FIRST_REQUEST_LINE));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.STATUS_CODE));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.THREAD_NAME));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.REQUEST_PROCESSING_TIME_MS));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.REQUEST_PROCESSING_TIME_S));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.LOCAL_IP_ADDRESS));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.LOCAL_SERVER_NAME));
        assertTrue(httpdFormatStrings.contains(HttpdFormatStrings.BYTES_TRANSFERRED));
    }

    // setLineNumber() -------------------------------------------------------------------------------------------------

    @Test
    public void setLineNumber() throws Exception {

        HttpdLogLine e = new HttpdLogLine();
        assertEquals(0, e.getLineNumber());
        e.setLineNumber(7L);
        assertEquals(7L, e.getLineNumber());
    }

    // toEvent() -------------------------------------------------------------------------------------------------------

    @Test
    public void toEvent_NoTimestamp() throws Exception {

        HttpdLogLine logLine = new HttpdLogLine();
        assertNull(logLine.getTimestamp());
        HttpEvent event = logLine.toEvent();
        assertNull(event.getTime());
    }

    @Test
    public void toEvent_ValidTimestamp() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);

        HttpEvent event = e.toEvent();

        assertEquals(1L, event.getTime().longValue());
    }

    @Test
    public void toEvent_NoTimezoneOffset() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(HttpdFormatStrings.TIMESTAMP, new TimestampImpl(1L));

        HttpEvent event = e.toEvent();

        assertEquals(1L, event.getTime().longValue());
    }

    @Test
    public void toEvent_TimezoneOffsetPresent() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLogValue(HttpdFormatStrings.TIMESTAMP, new TimestampImpl(1L, new TimeOffset("-0800")));

        HttpEvent event = e.toEvent();

        assertEquals(1L, event.getTime().longValue());
        assertEquals(new TimeOffset("-0800"), event.getTimestamp().getTimeOffset());
    }

    @Test
    public void toEvent() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        e.setLineNumber(7L);

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);
        e.setLogValue(HttpdFormatStrings.FIRST_REQUEST_LINE, "PUT /test/ HTTP/1.1");
        e.setLogValue(HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE, 404);
        e.setLogValue(HttpdFormatStrings.THREAD_NAME, "some thread name XXX-100");

        HttpEvent event = e.toEvent();

        assertEquals(1L, event.getTime().longValue());
        assertEquals(HTTPMethod.PUT.name(), ((StringProperty)event.getProperty(HttpEvent.METHOD)).getString());
        assertEquals("/test/", ((StringProperty)event.getProperty(HttpEvent.REQUEST_URI)).getString());
        assertEquals("HTTP/1.1", ((StringProperty) event.getProperty(HttpEvent.HTTP_VERSION)).getString());
        assertEquals(404, ((IntegerProperty)event.
                getProperty(HttpEvent.ORIGINAL_REQUEST_STATUS_CODE)).getInteger().intValue());
        assertNull(event.getProperty(HttpEvent.STATUS_CODE));
        assertEquals("some thread name XXX-100", ((StringProperty) event.getProperty(HttpEvent.THREAD_NAME)).getString());

        assertEquals(7L, event.getLongProperty(Event.LINE_NUMBER_PROPERTY_NAME).getLong().longValue());
    }

    @Test
    public void toEvent_StatusCode() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);
        e.setLogValue(HttpdFormatStrings.STATUS_CODE, 200);

        HttpEvent event = e.toEvent();

        assertEquals(200, ((IntegerProperty) event.getProperty(HttpEvent.STATUS_CODE)).getInteger().intValue());
    }

    @Test
    public void toEvent_Both_StatusCode_OriginalRequestStatusCode() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);
        e.setLogValue(HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE, 301);
        e.setLogValue(HttpdFormatStrings.STATUS_CODE, 302);

        HttpEvent event = e.toEvent();

        assertEquals(301, ((IntegerProperty) event.
                getProperty(HttpEvent.ORIGINAL_REQUEST_STATUS_CODE)).getInteger().intValue());
        assertEquals(302, ((IntegerProperty) event.getProperty(HttpEvent.STATUS_CODE)).getInteger().intValue());
    }

    @Test
    public void toEvent_OneRequestHeader() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);
        e.setLogValue(new RequestHeaderHttpdFormatString("%{i,Test-Header}"), "header value");

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

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);
        e.setLogValue(new RequestHeaderHttpdFormatString("%{i,Test-Header}"), "header value");
        e.setLogValue(new RequestHeaderHttpdFormatString("%{i,Another-Test-Header}"), "header value 2");

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

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);
        e.setLogValue(new CookieHttpdFormatString("%{c,TestCookie}"), "test-cookie-value");

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

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);
        e.setLogValue(new CookieHttpdFormatString("%{c,TestCookie}"), "test-cookie-value");
        e.setLogValue(new CookieHttpdFormatString("%{c,AnotherTestCookie}"), "another-test-cookie-value");

        HttpEvent event = e.toEvent();

        MapProperty cookies = (MapProperty)event.getProperty(HttpEvent.COOKIES);
        assertEquals(HttpEvent.COOKIES, cookies.getName());
        assertEquals(Map.class, cookies.getType());
        Map map = cookies.getMap();

        assertEquals(2, map.size());
        assertEquals("test-cookie-value", map.get("TestCookie"));
        assertEquals("another-test-cookie-value", map.get("AnotherTestCookie"));
    }

    @Test
    public void toEvent_TwoElementRequestLine() throws Exception {

        HttpdLogLine e = new HttpdLogLine();

        Timestamp t = new TimestampImpl(1L);
        e.setLogValue(HttpdFormatStrings.TIMESTAMP, t);
        e.setLogValue(HttpdFormatStrings.FIRST_REQUEST_LINE, "GET /something");

        HttpEvent event = e.toEvent();

        String s = event.getFirstRequestLine();
        assertEquals("GET /something", s);

        s = event.getMethod();
        assertEquals("GET", s);

        s = event.getRequestUri();
        assertEquals("/something", s);

        assertNull(event.getHttpVersion());
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
    public void parseFirstRequestLine_OneElement() throws Exception {

        try {
            HttpdLogLine.parseFirstRequestLine("GET");
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void parseFirstRequestLine_TwoElements() throws Exception {

        //
        // actually, we saw "valid" first line requests in the logs made of only two elements, do we'll have to
        // accept those
        //

        HttpdLogLine.parseFirstRequestLine("GET /a");

        //
        // this is an actual value from logs
        //
        HttpdLogLine.parseFirstRequestLine("GET /server-status?auto");
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
