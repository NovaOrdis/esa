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

import io.novaordis.esa.EventBase;
import io.novaordis.esa.FormatElement;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogEvent extends EventBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Map<FormatElement, Object> values;

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpdLogEvent() {

        this.values = new HashMap<>();
    }

    // Event implementation --------------------------------------------------------------------------------------------

    @Override
    public int getPropertyCount() {

        return super.getPropertyCount() + values.size();
    }

    // Methods related to the fact that these events come from a HTTP log - this is where the httpd log format details
    // are important ---------------------------------------------------------------------------------------------------

    /**
     * @return the value corresponding to the specified format element or null if there is no corresponding value
     * in the log event ("-" in the httpd logs, or whether the log format does contain the given format element.
     */
    public Object getLogValue(FormatElement e) {

        return values.get(e);
    }

    /**
     * @param value the value associated with the given FormatElement in the log entry corresponding to this event. Can
     *              be null, and this has the semantics of "erasing" the old value, if any.
     *
     * @return the previous value, if any, or null
     *
     * @exception IllegalArgumentException if the value's type does not match the format element.
     */
    public Object setLogValue(FormatElement e, Object value) {

        if (value == null) {
            return values.remove(e);
        }

        if (!e.getType().equals(value.getClass())) {
            throw new IllegalArgumentException(
                    "type mismatch, " + value.getClass() + " \"" + value + "\" is not a valid type for " + e);
        }

        if (HttpdFormatElement.TIMESTAMP.equals(e)) {
            // the timestamp is stored in superclass
            Date oldTimestamp = getTimestamp();
            setTimestamp((Date)value);
            return oldTimestamp;
        }

        return values.put(e, value);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @see HttpdFormatElement#REMOTE_HOST
     *
     * @return may return null
     */
    public String getRemoteHost() {
        return (String) getLogValue(HttpdFormatElement.REMOTE_HOST);
    }

    /**
     * @see HttpdFormatElement#REMOTE_LOGNAME
     *
     * @return may return null
     */
    public String getRemoteLogname() {
        return (String) getLogValue(HttpdFormatElement.REMOTE_LOGNAME);
    }

    public String getRemoteUser() {
        return (String) getLogValue(HttpdFormatElement.REMOTE_USER);
    }

    public String getRequestLine() {
        return (String) getLogValue(HttpdFormatElement.FIRST_REQUEST_LINE);
    }

    /**
     * @return may return null.
     */
    public Integer getStatusCode() {
        return (Integer) getLogValue(HttpdFormatElement.STATUS_CODE);
    }

    /**
     * @return may return null.
     */
    public Integer getOriginalRequestStatusCode() {
        return (Integer) getLogValue(HttpdFormatElement.ORIGINAL_REQUEST_STATUS_CODE);
    }

    /**
     * @return may return null.
     */
    public Long getResponseEntityBodySize() {
        return (Long) getLogValue(HttpdFormatElement.RESPONSE_ENTITY_BODY_SIZE);
    }

    /**
     * @return may return null.
     */
    public String getThreadName() {
        return (String) getLogValue(HttpdFormatElement.THREAD_NAME);
    }

    /**
     * @return may return null.
     */
    public Long getRequestProcessingTimeMs() {
        return (Long) getLogValue(HttpdFormatElement.REQUEST_PROCESSING_TIME_MS);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
