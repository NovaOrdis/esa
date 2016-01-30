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

package io.novaordis.esa.logs.httpd;

import io.novaordis.esa.logs.ParsingException;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public enum FormatStrings implements FormatString {

    //
    // a double quote, if present, must always be balanced, otherwise the format will throw an exception when
    // constructed
    //
    DOUBLE_QUOTES("\"", Character.class),

    //
    // a single quote, if present, must always be balanced, otherwise the format will throw an exception when
    // constructed
    //
    SINGLE_QUOTE("'", Character.class),

    OPENING_BRACKET("[", Character.class),
    CLOSING_BRACKET("]", Character.class),

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
    TIMESTAMP("%t", Date.class, new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z")),

    //
    // First line of request. Note that the first line is enclosed in quotes, you must explicitly specify the
    // DOUBLE_QUOTES or SINGLE_QUOTE format element.
    //
    FIRST_REQUEST_LINE("%r", String.class),

    //
    // The status code of the original request (whether was internally redirected or not). Stored as Integer.
    //
    ORIGINAL_REQUEST_STATUS_CODE("%s", Integer.class),

    //
    // The status code of the final request (whether was internally redirected or not). Stored as Integer.
    //
    STATUS_CODE("%>s", Integer.class),

    //
    // Response entity body size. Stored as Long.
    //
    RESPONSE_ENTITY_BODY_SIZE("%b", Long.class),

    //
    // The name of the thread processing the request.
    //
    // Note that this is actually the WildFly convention, not Apache httpd convention (Apache httpd logs "bytes
    // received, including request and headers" for %I)
    //
    THREAD_NAME("%I", String.class),

    //
    // The time taken to serve the request. WildFly logs the time in milliseconds for %D, while Apache httpd logs the
    // time in microseconds for the same %D.
    //
    REQUEST_PROCESSING_TIME_MS("%D", Long.class);


    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String literal;
    private Class type;
    private Format format;

    // Constructors ----------------------------------------------------------------------------------------------------

    FormatStrings(String literal, Class type) {

        this(literal, type, null);
    }

    /**
     * @param format an optional Format (can be null) which specifies the string representation format.
     */
    FormatStrings(String literal, Class type, Format format) {

        this.literal = literal;
        this.type = type;
        this.format = format;
    }

    // FormatStrings implementation ------------------------------------------------------------------------------------

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

        if (Integer.class.equals(type)) {

            try {
                return new Integer(logStringRepresentation);
            }
            catch(Exception e) {
                throw new ParsingException(
                        this + " string representation \"" + logStringRepresentation + "\" is not a valid integer", e);
            }
        }

        if (Long.class.equals(type)) {
            try {
                return new Long(logStringRepresentation);
            }
            catch(Exception e) {
                throw new ParsingException(
                        this + " string representation \"" + logStringRepresentation + "\" is not a valid long", e);
            }
        }

        throw new RuntimeException(this + ": parse() for " + getType() + " NOT IMPLEMENTED YET");
    }

    @Override
    public Class getType() {

        return type;
    }

    @Override
    public boolean isLeftEnclosure() {

        return DOUBLE_QUOTES.equals(this) || SINGLE_QUOTE.equals(this) || OPENING_BRACKET.equals(this);
    }

    @Override
    public boolean isRightEnclosure() {

        return DOUBLE_QUOTES.equals(this) || SINGLE_QUOTE.equals(this) || CLOSING_BRACKET.equals(this);
    }

    @Override
    public FormatString getMatchingEnclosure() {

        if (DOUBLE_QUOTES.equals(this)) {
            return DOUBLE_QUOTES;
        }
        else if (SINGLE_QUOTE.equals(this)) {
            return SINGLE_QUOTE;
        }
        else if (OPENING_BRACKET.equals(this)) {
            return CLOSING_BRACKET;
        }
        else if (CLOSING_BRACKET.equals(this)) {
            return OPENING_BRACKET;
        }
        else {
            return null;
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return super.toString() + " " + (DOUBLE_QUOTES.equals(this) ? "\\" : "") + getLiteral();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
