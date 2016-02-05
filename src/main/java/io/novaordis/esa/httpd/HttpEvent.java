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

import io.novaordis.esa.core.event.TimedEvent;
import io.novaordis.esa.core.event.TimedEventBase;

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
    public static final String QUERY_STRING = "query";
    public static final String REQUEST_HEADERS = "request-headers";
    public static final String RESPONSE_HEADERS = "response-headers";
    public static final String COOKIES = "cookies";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpEvent(long timestamp) {
        super(timestamp);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
