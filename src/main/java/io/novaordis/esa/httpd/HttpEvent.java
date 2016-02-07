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
import io.novaordis.esa.core.event.LongProperty;
import io.novaordis.esa.core.event.MapProperty;
import io.novaordis.esa.core.event.StringProperty;
import io.novaordis.esa.core.event.TimedEvent;
import io.novaordis.esa.core.event.TimedEventBase;

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
public class HttpEvent extends TimedEventBase implements TimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String METHOD = "method";
    public static final String PATH = "path";
    public static final String HTTP_VERSION = "http-version";
    public static final String ORIGINAL_REQUEST_STATUS_CODE = "original-request-status-code";
    public static final String STATUS_CODE = "status-code";
    public static final String THREAD_NAME = "thread-name";
    public static final String REMOTE_HOST = "remote-host";
    public static final String REMOTE_LOGNAME = "remote-logname";
    public static final String REMOTE_USER = "remote-user";
    public static final String RESPONSE_ENTITY_BODY_SIZE = "response-body-size";
    public static final String REQUEST_PROCESSING_TIME = "request-processing-time";
    public static final String QUERY = "query";
    public static final String REQUEST_HEADERS = "request-headers";
    public static final String RESPONSE_HEADERS = "response-headers";
    public static final String COOKIES = "cookies";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpEvent(Long timestamp) {
        super(timestamp);
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

    public String getPath() {
        return getString(PATH);
    }

    public String getHttpVersion() {
        return getString(HTTP_VERSION);
    }

    public String getFirstRequestLine() {

        String method = getMethod();
        String path = getPath();
        String version = getHttpVersion();

        if (method == null || path == null || version == null) {
            return null;
        }

        return method + " " + path + " " + version;
    }

    public Integer getStatusCode() {
        return getInteger(STATUS_CODE);
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

    public Long getRequestProcessingTimeMs() {
        return getLong(REQUEST_PROCESSING_TIME);
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
