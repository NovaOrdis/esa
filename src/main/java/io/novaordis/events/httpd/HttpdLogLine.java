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
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.StringProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A httpd log line.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogLine {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private long lineNumber;

    private Map<HttpdFormatString, Object> values;

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpdLogLine() {

        this.values = new HashMap<>();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    /**
     * @return the value corresponding to the specified format element or null if there is no corresponding value
     * in the log event ("-" in the httpd logs, or whether the log format does contain the given format element.
     */
    public Object getLogValue(HttpdFormatString e) {

        return values.get(e);
    }

    /**
     * @param value the value associated with the given HttpdFormatStrings in the log entry corresponding to this event. Can
     *              be null, and this has the semantics of "erasing" the old value, if any.
     *
     * @return the previous value, if any, or null
     *
     * @exception IllegalArgumentException if the value's type does not match the format element.
     */
    public Object setLogValue(HttpdFormatString e, Object value) {

        if (value == null) {

            //
            // this work fell for IGNORE, it is simply removed when it does not exist, meaning ... ignored
            //
            return values.remove(e);
        }

        if (!e.getType().equals(value.getClass())) {
            throw new IllegalArgumentException(
                    "type mismatch, " + value.getClass() + " \"" + value + "\" is not a valid type for " + e);
        }

        if (HttpdFormatStrings.TIMESTAMP.equals(e)) {

            // we convert timestamp from Date to Long
            Date timestamp = (Date)value;
            value = timestamp.getTime();
        }

        return values.put(e, value);
    }

    /**
     * @return the HttpdFormatStrings this httpd log line has values for.
     */
    public Set<HttpdFormatString> getFormatStrings() {

        return values.keySet();
    }

    public Long getTimestamp() {
        return (Long)getLogValue(HttpdFormatStrings.TIMESTAMP);
    }

    /**
     * @see HttpdFormatStrings#REMOTE_HOST
     *
     * @return may return null
     */
    public String getRemoteHost() {
        return (String) getLogValue(HttpdFormatStrings.REMOTE_HOST);
    }

    /**
     * @see HttpdFormatStrings#REMOTE_LOGNAME
     *
     * @return may return null
     */
    public String getRemoteLogname() {
        return (String) getLogValue(HttpdFormatStrings.REMOTE_LOGNAME);
    }

    public String getRemoteUser() {
        return (String) getLogValue(HttpdFormatStrings.REMOTE_USER);
    }

    public String getFirstRequestLine() {
        return (String) getLogValue(HttpdFormatStrings.FIRST_REQUEST_LINE);
    }

    public String getQueryString() {
        return (String) getLogValue(HttpdFormatStrings.QUERY_STRING);
    }

    /**
     * @return may return null.
     */
    public Integer getStatusCode() {
        return (Integer) getLogValue(HttpdFormatStrings.STATUS_CODE);
    }

    /**
     * @return may return null.
     */
    public Integer getOriginalRequestStatusCode() {
        return (Integer) getLogValue(HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE);
    }

    /**
     * @return may return null.
     */
    public Long getResponseEntityBodySize() {
        return (Long) getLogValue(HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE);
    }

    /**
     * @return may return null.
     */
    public String getThreadName() {
        return (String) getLogValue(HttpdFormatStrings.THREAD_NAME);
    }

    /**
     * @return may return null.
     */
    public Long getRequestProcessingTimeMs() {
        return (Long) getLogValue(HttpdFormatStrings.REQUEST_PROCESSING_TIME_MS);
    }

    /**
     * Converts this httpd log line into a HttpEvent. Since HttpEvents are timed events, it does not make sense to
     * convert a timestamp-less log line into a HTTP timed event, so in that case the method throws
     * IllegalStateException.
     *
     * @exception IllegalStateException if the timestamp information is missing from the underlying httpd log line.
     * @exception IllegalStateException if the first request line is invalid (bad HTTP method, bad HTTP version, etc.)
     */
    public HttpEvent toEvent() throws IllegalStateException {

        Long timestamp = getTimestamp();

        // we allow for non-timestamps events
//        if (timestamp == null) {
//            throw new IllegalStateException(
//                    "httpd log line " + this + " does not have timestamp information thus cannot be converted to a HTTP timed event");
//        }

        HttpEvent httpEvent = new HttpEvent(timestamp);

        if (lineNumber > 0) {
            httpEvent.setLongProperty(Event.LINE_NUMBER_PROPERTY_NAME, lineNumber);
        }

        Set<HttpdFormatString> httpdFormatStrings = getFormatStrings();

        for(HttpdFormatString fs: httpdFormatStrings) {

            if (HttpdFormatStrings.TIMESTAMP.equals(fs)) {
                // already handled
                continue;
            }

            if (HttpdFormatStrings.FIRST_REQUEST_LINE.equals(fs)) {

                String s = (String)getLogValue(HttpdFormatStrings.FIRST_REQUEST_LINE);
                if (s == null) {
                    continue;
                }

                String[] methodPathHtmlVersion = parseFirstRequestLine(s);
                httpEvent.setProperty(new StringProperty(HttpEvent.METHOD, methodPathHtmlVersion[0]));
                httpEvent.setProperty(new StringProperty(HttpEvent.REQUEST_URI, methodPathHtmlVersion[1]));
                httpEvent.setProperty(new StringProperty(HttpEvent.HTTP_VERSION, methodPathHtmlVersion[2]));
            }
            else {

                Object value = getLogValue(fs);
                Property p = fs.toProperty(value);
                if (p != null) {
                    httpEvent.setProperty(p);
                }
            }
        }

        return httpEvent;
    }

    @Override
    public String toString() {

        Long timestamp = getTimestamp();
        String ts = timestamp == null ? "-" : HttpdFormatString.TIMESTAMP_FORMAT.format(timestamp);
        String rls = getFirstRequestLine();
        rls = rls == null ? "-" : rls;
        Integer rsc = getOriginalRequestStatusCode();
        String rscs = rsc == null ? "-" : Integer.toString(rsc);
        return ts + ", " + rls + ", " + rscs;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Static Package protected ----------------------------------------------------------------------------------------

    /**
     * @return a String[3] array containing <b>valid</b> HTTP Method, the request path and a <b>valid</b> HTTP version
     * String. The result is guaranteed to contain 3 elements and have valid values.
     *
     * @exception IllegalArgumentException if the passed argument cannot produce the valid String[3].
     */
    static String[] parseFirstRequestLine(String firstRequestLine) throws IllegalArgumentException {

        if (firstRequestLine == null) {
            throw new IllegalArgumentException("null first request line");
        }

        String[] result = firstRequestLine.split(" +");

        if (result.length != 3) {
            throw new IllegalArgumentException(
                    "invalid first request line, more than three elements: \"" + firstRequestLine + "\"");
        }

        // verify method

        try {

            HTTPMethod.valueOf(result[0]);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("invalid first request line, unknown HTTP method: \"" + firstRequestLine + "\"", e);
        }

        //
        // TODO maybe some path validation in the future
        //

        if (!"HTTP/1.0".equals(result[2]) && !"HTTP/1.1".equals(result[2])) {
            throw new IllegalArgumentException("invalid first request line \"" + firstRequestLine + "\", unknown HTTP version \"" + result[2] + "\"") ;
        }

        return result;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
