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
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.MapProperty;
import io.novaordis.events.core.event.StringProperty;
import io.novaordis.events.core.event.TimedEvent;
import io.novaordis.events.core.event.GenericTimedEvent;
import io.novaordis.events.extensions.bscenarios.BusinessScenario;
import io.novaordis.utilities.timestamp.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A HTTP request/response as processed by a web server.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class HttpEvent extends GenericTimedEvent implements TimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String METHOD = "method";
    public static final String REQUEST_URI = "request-uri";
    public static final String HTTP_VERSION = "http-version";
    public static final String ORIGINAL_REQUEST_STATUS_CODE = "original-request-status-code";
    public static final String STATUS_CODE = "status-code";
    public static final String THREAD_NAME = "thread-name";
    public static final String REMOTE_HOST = "remote-host";
    public static final String REMOTE_LOGNAME = "remote-logname";
    public static final String REMOTE_USER = "remote-user";
    public static final String RESPONSE_ENTITY_BODY_SIZE = "response-body-size";
    public static final String REQUEST_DURATION = "request-duration"; // note that it could be expressed in ms or secs
    public static final String QUERY = "query";
    public static final String REQUEST_HEADERS = "request-headers";
    public static final String RESPONSE_HEADERS = "response-headers";
    public static final String COOKIES = "cookies";
    public static final String LOCAL_IP_ADDRESS = "local-ip-address";
    public static final String LOCAL_SERVER_NAME = "local-server-name";
    public static final String BYTES_TRANSFERRED = "bytes-transferred";

    public static final String JSESSIONID_COOKIE_KEY = "JSESSIONID";

    // Static ----------------------------------------------------------------------------------------------------------

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(HttpdFormatString.TIMESTAMP_FORMAT_STRING);

    private static String formatTimestamp(long timestamp) {

        //
        // we cannot concurrently access a Format instance
        //
        synchronized (TIMESTAMP_FORMAT) {
            return TIMESTAMP_FORMAT.format(timestamp);
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @see Timestamp
     */
    public HttpEvent(Timestamp timestamp) {
        super(timestamp);
    }

    /**
     * @see Timestamp
     */
    public HttpEvent(Timestamp timestamp, long duration) {
        super(timestamp);
        setRequestDuration(duration);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getRemoteHost() {
        return getString(REMOTE_HOST);
    }

    public String getRemoteLogname() {
        return getString(REMOTE_LOGNAME);
    }

    public String getRemoteUser() {
        return getString(REMOTE_USER);
    }

    public String getMethod() {
        return getString(METHOD);
    }

    public String getRequestUri() {
        return getString(REQUEST_URI);
    }

    public void setRequestUri(String requestUri) {
        setStringProperty(REQUEST_URI, requestUri);
    }

    public String getHttpVersion() {
        return getString(HTTP_VERSION);
    }

    public String getFirstRequestLine() {

        String method = getMethod();
        String path = getRequestUri();
        String version = getHttpVersion();

        if (method == null || path == null || version == null) {
            return null;
        }

        return method + " " + path + " " + version;
    }

    public Integer getStatusCode() {
        return getInteger(STATUS_CODE);
    }

    /**
     * Overwrites the existing value, if any.
     *
     * @exception IllegalArgumentException on invalid status code (smaller than 200, or larger than 599).
     */
    public void setStatusCode(int statusCode) {

        if (statusCode < 200 || statusCode >= 600) {
            throw new IllegalArgumentException("invalid status code " + statusCode);
        }
        setIntegerProperty(STATUS_CODE, statusCode);
    }

    public Integer getOriginalRequestStatusCode() {
        return getInteger(ORIGINAL_REQUEST_STATUS_CODE);
    }

    public Long getResponseEntityBodySize() {
        return getLong(RESPONSE_ENTITY_BODY_SIZE);
    }

    public String getThreadName() {
        return getString(THREAD_NAME);
    }

    public String getQueryString() {

        MapProperty p = getMapProperty(QUERY);

        if (p == null) {
            return null;
        }

        String s = "";

        Map<String, Object> m = p.getMap();
        List<String> keys = new ArrayList<>(m.keySet());
        Collections.sort(keys);

        for(Iterator<String> ki = keys.iterator(); ki.hasNext(); ) {
            String key = ki.next();
            s += key + "=" + m.get(key);
            if (ki.hasNext()) {
                s += "&";
            }
        }

        return s;
    }

    public Long getRequestDuration() {
        return getLong(REQUEST_DURATION);
    }

    public void setRequestDuration(long duration) {
        setLongProperty(REQUEST_DURATION, duration);
    }

    /**
     * @return null if no such cookie is present
     */
    public String getCookie(String cookieName) {

        MapProperty cookies = (MapProperty)getProperty(HttpEvent.COOKIES);

        if (cookies == null) {
            return null;
        }

        Map<String, Object> map = cookies.getMap();
        if (map == null) {
            return null;
        }

        return (String)map.get(cookieName);
    }

    /**
     * Overwrites the previous cookie with the same name, if exists.
     */
    public void setCookie(String cookieName, String cookieValue) {

        MapProperty mp = (MapProperty)getProperty(HttpEvent.COOKIES);

        if (mp == null) {
            mp = new MapProperty(HttpEvent.COOKIES);
            setProperty(mp);
        }

        mp.getMap().put(cookieName, cookieValue);
    }

    /**
     * @return null if no such header is present. Returns the empty string "" if the header is present but has an empty
     * body.
     */
    public String getRequestHeader(String headerName) {

        MapProperty requestHeaders = (MapProperty)getProperty(HttpEvent.REQUEST_HEADERS);

        if (requestHeaders == null) {
            return null;
        }

        Map<String, Object> map = requestHeaders.getMap();
        if (map == null) {
            return null;
        }

        return (String)map.get(headerName);
    }

    /**
     * Overwrites the previous header with the same name, if exists.
     *
     * @param headerValue if null, it means we want to add the request header with an empty body, it will show as
     *                    Some-Header-Name: in the request. The corresponding getRequestHeader() will return an empty
     *                    string.
     */
    public void setRequestHeader(String headerName, String headerValue) {

        MapProperty mp = (MapProperty)getProperty(HttpEvent.REQUEST_HEADERS);

        if (mp == null) {
            mp = new MapProperty(HttpEvent.REQUEST_HEADERS);
            setProperty(mp);
        }

        if (headerValue == null) {
            headerValue = "";
        }

        mp.getMap().put(headerName, headerValue);
    }

    /**
     * Sets a request header an empty body, it will show as Some-Header-Name: in the request. The corresponding
     * getRequestHeader() will return an empty string.
     */
    public void setRequestHeader(String headerName) {

        setRequestHeader(headerName, null);
    }

    public long getLineNumber() {

        LongProperty p = getLongProperty(Event.LINE_NUMBER_PROPERTY_NAME);
        Long v = p == null ? null : p.getLong();
        return v == null ? 0L : v;
    }

    public void setLineNumber(long lineNumber) {
        setLongProperty(Event.LINE_NUMBER_PROPERTY_NAME, lineNumber);
    }

    /**
     * @return the value corresponding to the request header 'Business-Scenario-Request-Sequence-ID'. May be null
     * if the header is not present.
     */
    public String getRequestSequenceId() {

        return getRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME);
    }

    public void setRequestSequenceId(String s) {

        setRequestHeader(BusinessScenario.BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME, s);
    }

    /**
     * @return the value corresponding to the request header 'Business-Scenario-Iteration-ID'. May be null
     * if the header is not present.
     */
    public String getIterationId() {

        return getRequestHeader(BusinessScenario.BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME);
    }

    @Override
    public String toString() {

        Long timestamp = getTimestampGMT();
        String s = (timestamp == null ? "N/A" : formatTimestamp(timestamp));
        s += " " + getMethod() + " " + getRequestUri();
        s += "(line=" + getLineNumber();
        String jSessionId = getCookie(JSESSIONID_COOKIE_KEY);
        if (jSessionId != null) {

            s += ", JSESSIONID=" + jSessionId;
        }
        s += ")";

        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private String getString(String propertyName) {

        StringProperty p = getStringProperty(propertyName);

        if (p == null) {
            return null;
        }

        return p.getString();
    }

    private Integer getInteger(String propertyName) {

        IntegerProperty p = getIntegerProperty(propertyName);
        if (p == null) {
            return null;
        }
        return p.getInteger();
    }

    private Long getLong(String propertyName) {

        LongProperty p = getLongProperty(propertyName);
        if (p == null) {
            return null;
        }
        return p.getLong();
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
