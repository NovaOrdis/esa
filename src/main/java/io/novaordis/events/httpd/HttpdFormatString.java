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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;

import io.novaordis.events.api.event.Property;
import io.novaordis.utilities.parsing.ParsingException;

/**
 * An individual httpd log format string, as specified by
 * https://httpd.apache.org/docs/current/en/mod/mod_log_config.html#formats. For example "%h" is a httpd format string
 * that represents the remote client address for a httpd log.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public interface HttpdFormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    Logger log = org.slf4j.LoggerFactory.getLogger(HttpdFormatString.class);

    String TIMESTAMP_FORMAT_STRING = "dd/MMM/yyyy:HH:mm:ss Z";
    DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(TIMESTAMP_FORMAT_STRING);

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param s a multi-token format string - may contain multiple individual format strings
     *
     * @throws ParsingException if an unknown httpd format element is encountered right away.
     * @throws CorruptedHttpdFormatStringException if the format string is a partially correct httpd format string but
     *  an unknown element is encountered. The invalid token is mentioned in the human-readable error message.
     */
    static List<HttpdFormatString> fromString(String s) throws CorruptedHttpdFormatStringException, ParsingException {

        log.debug("attempting to produce a HttpdFormatString list from \"" + s + "\"");

        List<HttpdFormatString> result = new ArrayList<>();

        for(StringTokenizer st = new StringTokenizer(s, " "); st.hasMoreTokens(); ) {

            String tok = st.nextToken();

            token: while(tok.length() > 0) {

                for(HttpdFormatString fs: HttpdFormatStrings.values()) {
                    if (tok.startsWith(fs.getLiteral())) {
                        result.add(fs);
                        tok = tok.substring(fs.getLiteral().length());
                        continue token;
                    }
                }

                // try known parameterized format strings
                ParameterizedHttpdFormatString pfs = ParameterizedHttpdFormatString.parameterizedFormatFromString(tok);
                if (pfs != null) {
                    result.add(pfs);
                    tok = tok.substring(pfs.getLiteral().length());
                    continue;
                }

                if (result.isEmpty()) {

                    throw new ParsingException("unknown httpd format element '" + tok + "'");
                }
                else {
                    //
                    // this is a corrupted httpd format string, help the user and give as many details we can
                    //
                    throw new CorruptedHttpdFormatStringException("invalid httpd log format token \"" + tok + "\"");
                }
            }
        }

        return result;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the literal, as it appears in the format specification. Example: %h.
     */
    String getLiteral();

    /**
     * Turns a string representation of the format element, as read from the log, into a typed value. The only valid
     * "no value" representation is "-". A string comprising of spaces, or an empty string should trigger a
     * ParsingException.
     *
     * @param lineNumber the current line number. Null is acceptable, if the line number is not available.
     * @param positionInLine the position in line. Null is acceptable, if the position is not available.
     *
     *
     * @return a typed value, never null.
     *
     * @throws ParsingException if the string representation does not match the format element.
     */
    Object parse(String logStringRepresentation, Long lineNumber, Integer positionInLine) throws ParsingException;

    /**
     * @return the type of the values maintained for this format element.
     */
    Class getType();

    /**
     * @return true if this element is used to enclose at the left a string (possibly containing multiple spaces).
     * Examples: left bracket, double quotes, single quote.
     */
    boolean isLeftEnclosure();

    /**
     * @return true if this element is used to enclose at the right a string (possibly containing multiple spaces).
     * Examples: right bracket, double quotes, single quote.
     */
    boolean isRightEnclosure();

    /**
     * If this element is an enclosure, the method returns corresponding matching enclosure (right brace if we
     * are a left brace, left brace if we are a right brace, double quotes if we are double quotes, single quote if
     * we are a single quote. Return null if this element is not an enclosure.
     */
    HttpdFormatString getMatchingEnclosure();

    /**
     * @return may return null if conversion is not possible (the value is null, the HttpdFormatString - double quotes -
     * cannot generate a property, etc.)
     */
    Property toProperty(Object value);

}
