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

import io.novaordis.esa.FormatElement;
import io.novaordis.esa.ParsingException;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public enum HttpdFormatElement implements FormatElement {

    //
    // Remote host name or IP address. Will log the IP address if HostnameLookups is set to Off, which is the default.
    //
    REMOTE_HOST("%h", String.class),

    //
    // Remote logname from identd (if supplied)
    //
    REMOTE_LOGNAME("%l", String.class),

    //
    // Remote user if the request was authenticated. May be bogus if return status (%s) is 401 (unauthorized).
    //
    REMOTE_USER("%u", String.class),

    //
    // Time the request was received, in the format [18/Sep/2011:19:18:28 -0400]. The last number indicates the timezone
    // offset from GMT
    //
    TIMESTAMP("%t", Date.class, new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z"))

    //
    //  \"%r\" %>s %b"
    //

    ;

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String literal;
    private Class type;
    private Format format;

    // Constructors ----------------------------------------------------------------------------------------------------

    private HttpdFormatElement(String literal, Class type) {

        this(literal, type, null);
    }

    /**
     * @param format an optional Format (can be null) which specifies the string representation format.
     */
    private HttpdFormatElement(String literal, Class type, Format format) {

        this.literal = literal;
        this.type = type;
        this.format = format;
    }

    // FormatElement implementation ------------------------------------------------------------------------------------

    @Override
    public String getLiteral() {
        return literal;
    }

    @Override
    public Object parse(String logStringRepresentation) throws ParsingException {

        if ("-".equals(logStringRepresentation)) {
            return null;
        }

        if (String.class.equals(type)) {
            return logStringRepresentation;
        }

        if (Date.class.equals(type)) {

            // use the default timestamp format
            if (format == null) {
                throw new IllegalStateException(
                        this + " incorrectly configured, it must have a non-null SimpleDateFormat instance");
            }

            try {
                return format.parseObject(logStringRepresentation);
            }
            catch(ParseException e) {
                throw new ParsingException(
                        this + " string representation \"" + logStringRepresentation +
                                "\" does not match the expected format " + format, e);
            }
        }

        // 10/Oct/2016:13:55:36 -0700

        throw new RuntimeException("parse() for " + getType() + " NOT IMPLEMENTED YET");
    }

    @Override
    public Class getType() {

        return type;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return super.toString() + " " + getLiteral();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
