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

import io.novaordis.clad.UserErrorException;
import io.novaordis.esa.EventBase;
import io.novaordis.esa.FormatElement;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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

    /**
     * @see io.novaordis.esa.Event#getValue(FormatElement)
     */
    @Override
    public Object getValue(FormatElement e) {

        return values.get(e);
    }

    /**
     * @see io.novaordis.esa.Event#setValue(FormatElement, Object)
     */
    @Override
    public Object setValue(FormatElement e, Object value) {

        if (value == null) {
            return values.remove(e);
        }

        if (!e.getType().equals(value.getClass())) {
            throw new IllegalArgumentException(
                    "type mismatch, " + value.getClass() + " \"" + value + "\" is not a valid type for " + e);
        }

        return values.put(e, value);
    }

    @Override
    public int getValueCount() {

        return super.getValueCount() + values.size();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @see HttpdFormatElement#REMOTE_HOST
     *
     * @return may return null
     */
    public String getRemoteHost() {
        return (String)getValue(HttpdFormatElement.REMOTE_HOST);
    }

    /**
     * @see HttpdFormatElement#REMOTE_LOGNAME
     *
     * @return may return null
     */
    public String getRemoteLogname() {
        return (String)getValue(HttpdFormatElement.REMOTE_LOGNAME);
    }

    public String getRemoteUser() {
        throw new RuntimeException("getRemoteUser() NOT YET IMPLEMENTED");
    }

    public String getRequestLine() {
        throw new RuntimeException("getRequestLine() NOT YET IMPLEMENTED");
    }

    public int getStatusCode() {
        throw new RuntimeException("getStatusCode() NOT YET IMPLEMENTED");
    }

    public long getResponseEntityBodySize() {
        throw new RuntimeException("getResponseEntityBodySize() NOT YET IMPLEMENTED");
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
