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

import io.novaordis.events.core.event.MeasureUnit;
import io.novaordis.events.core.event.MemoryMeasureUnit;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.PropertyFactory;
import io.novaordis.events.core.event.TimeMeasureUnit;
import io.novaordis.events.ParsingException;

import java.text.Format;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public enum HttpdFormatStrings implements HttpdFormatString {

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
    REMOTE_HOST("%h", String.class, HttpEvent.REMOTE_HOST),

    //
    // Remote logname from identd (if supplied)
    //
    REMOTE_LOGNAME("%l", String.class, HttpEvent.REMOTE_LOGNAME),

    //
    // Remote user if the request was authenticated. May be bogus if return status (%s) is 401 (unauthorized).
    //
    REMOTE_USER("%u", String.class, HttpEvent.REMOTE_USER),

    //
    // Time the request was received, in the format [18/Sep/2011:19:18:28 -0400]. The last number indicates the timezone
    // offset from GMT
    //
    TIMESTAMP("%t", Date.class, TIMESTAMP_FORMAT, TIMESTAMP_FORMAT_STRING),

    //
    // The query string, excluding the '?' character. Usually enclosed in quotes.
    //
    QUERY_STRING("%q", String.class, HttpEvent.QUERY, Map.class),

    //
    // First line of request. Note that the first line may be optionally enclosed in quotes, where FIRST_REQUEST_LINE
    // format string is is bordered by DOUBLE_QUOTES or SINGLE_QUOTE elements, or not. The parser will know how to
    // handle both situations.
    //
    FIRST_REQUEST_LINE("%r", String.class),

    //
    // The status code of the original request (whether was internally redirected or not). Stored as Integer.
    //
    ORIGINAL_REQUEST_STATUS_CODE("%s", Integer.class, HttpEvent.ORIGINAL_REQUEST_STATUS_CODE),

    //
    // The status code of the final request (whether was internally redirected or not). Stored as Integer.
    //
    STATUS_CODE("%>s", Integer.class, HttpEvent.STATUS_CODE),

    //
    // Response entity body size. Does not include headers. Stored as Long.
    //
    RESPONSE_ENTITY_BODY_SIZE("%b", Long.class, HttpEvent.RESPONSE_ENTITY_BODY_SIZE, MemoryMeasureUnit.BYTE),

    //
    // The name of the thread processing the request.
    //
    // Note that this is actually the WildFly convention, not Apache httpd convention (Apache httpd logs "bytes
    // received, including request and headers" for %I)
    //
    THREAD_NAME("%I", String.class, HttpEvent.THREAD_NAME),

    //
    // The time taken to serve the request. WildFly logs the time in milliseconds for %D, while Apache httpd logs the
    // time in microseconds for the same %D.
    //
    REQUEST_PROCESSING_TIME_MS("%D", Long.class, HttpEvent.REQUEST_DURATION, TimeMeasureUnit.MILLISECOND),

    //
    // The time taken to serve the request, in seconds.
    //
    REQUEST_PROCESSING_TIME_S("%T", Double.class, HttpEvent.REQUEST_DURATION, TimeMeasureUnit.SECOND),

    //
    // Local IP address
    //
    LOCAL_IP_ADDRESS("%A", String.class, HttpEvent.LOCAL_IP_ADDRESS),

    //
    // Local server name
    //
    LOCAL_SERVER_NAME("%v", String.class, HttpEvent.LOCAL_SERVER_NAME),

    //
    // Bytes transferred (received and sent), including request and headers. Stored as Long.
    //
    BYTES_TRANSFERRED("%S", Long.class, HttpEvent.BYTES_TRANSFERRED, MemoryMeasureUnit.BYTE);

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String literal;
    private Class type;
    private Format format;
    private String formatStringRepresentation; // if present, makes the info and error messages more user friendly
                                               // if not present, format.toString() should be used.

    // the corresponding HttpEvent property name
    private String propertyName;

    // the corresponding HttpEvent property tye
    private Class propertyType;

    private MeasureUnit measureUnit;

    // Constructors ----------------------------------------------------------------------------------------------------

    HttpdFormatStrings(String literal, Class type) {

        this(literal, type, null, null, null, null);
    }

    /**
     * @param format an optional Format (can be null) which specifies the string representation format.
     * @param formatStringRepresentation - if present, makes the info and error messages more user friendly, if not
     *                                   present, format.toString() should be used. null is therefore acceptable.
     */
    HttpdFormatStrings(String literal, Class type, Format format, String formatStringRepresentation) {

        this(literal, type, format, formatStringRepresentation, null);
    }

    HttpdFormatStrings(String literal, Class type, String propertyName) {

        this(literal, type, null, null, propertyName);
    }

    HttpdFormatStrings(String literal, Class type, String propertyName, Class propertyType) {

        this(literal, type, null, null, propertyName, propertyType, null);
    }

    /**
     * Using this constructor implies that the HttpdFormatString type coincides with the corresponding property type.
     *
     * @param formatStringRepresentation - if present, makes the info and error messages more user friendly, if not
     *                                   present, format.toString() should be used. null is therefore acceptable.
     */
    HttpdFormatStrings(String literal, Class type, Format format, String formatStringRepresentation, String propertyName) {

        this(literal, type, format, formatStringRepresentation, propertyName, type);
    }

    /**
     * Using this constructor implies that the HttpdFormatString type coincides with the corresponding property type.
     */
    HttpdFormatStrings(String literal, Class type, String propertyName, MeasureUnit mu) {

        this(literal, type, null, null, propertyName, type, mu);
    }

    /**
     * @param formatStringRepresentation - if present, makes the info and error messages more user friendly, if not
     *                                   present, format.toString() should be used. null is therefore acceptable.
     */
    HttpdFormatStrings(String literal, Class type, Format format, String formatStringRepresentation, String propertyName,
                       Class propertyType) {

        this(literal, type, format, formatStringRepresentation, propertyName, propertyType, null);
    }

    /**
     * @param formatStringRepresentation - if present, makes the info and error messages more user friendly, if not
     *                                   present, format.toString() should be used. null is therefore acceptable.
     */
    HttpdFormatStrings(String literal, Class type, Format format, String formatStringRepresentation, String propertyName,
                       Class propertyType, MeasureUnit mu) {

        this.literal = literal;
        this.type = type;
        this.format = format;
        this.formatStringRepresentation = formatStringRepresentation;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.measureUnit = mu;
    }

    // HttpdFormatStrings implementation ------------------------------------------------------------------------------------

    @Override
    public String getLiteral() {
        return literal;
    }

    @Override
    public Object parse(String logStringRepresentation, Long lineNumber, Integer positionInLine)
            throws ParsingException {

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
                                "\" does not match the expected format " +
                                (formatStringRepresentation != null ?
                                        "\"" + formatStringRepresentation + "\"" :
                                        format.toString()), e, lineNumber, positionInLine);
            }
        }

        if (Integer.class.equals(type)) {

            try {
                return new Integer(logStringRepresentation);
            }
            catch(Exception e) {
                throw new ParsingException(
                        this + " string representation \"" + logStringRepresentation + "\" is not a valid integer", e,
                        lineNumber, positionInLine);
            }
        }

        if (Long.class.equals(type)) {

            try {
                return new Long(logStringRepresentation);
            }
            catch(Exception e) {
                throw new ParsingException(
                        this + " string representation \"" + logStringRepresentation + "\" is not a valid long", e,
                        lineNumber, positionInLine);
            }
        }

        if (Double.class.equals(type)) {

            try {
                return new Double(logStringRepresentation);
            }
            catch(Exception e) {
                throw new ParsingException(
                        this + " string representation \"" + logStringRepresentation + "\" is not a valid double", e,
                        lineNumber, positionInLine);
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
    public HttpdFormatString getMatchingEnclosure() {

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

    @Override
    public Property toProperty(Object value) {

        if (propertyName == null) {
            return null;
        }

        if (QUERY_STRING.equals(this)) {

            // TODO if I run into more than one of these cases, parameterize the conversion logic as a lambda

            String queryString = (String)value;
            Map<String, String> map = new HashMap<>();
            value = map;
            String[] attrs = queryString.split("&");
            for(String s: attrs) {
                int i = s.indexOf('=');
                if (i != -1) {
                    map.put(s.substring(0, i), s.substring(i + 1));
                }
                else {
                    map.put(s, null);
                }
            }

        }

        return PropertyFactory.createInstance(propertyName, propertyType, value, measureUnit);
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
