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

import io.novaordis.esa.core.event.Property;
import io.novaordis.esa.logs.ParsingException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * An individual httpd log format string, as specified by
 * https://httpd.apache.org/docs/current/en/mod/mod_log_config.html#formats. For example "%h" is a httpd format string
 * that represents the remote client address for a httpd log.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public interface FormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    String TIMESTAMP_FORMAT_STRING = "dd/MMM/yyyy:HH:mm:ss Z";
    DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(TIMESTAMP_FORMAT_STRING);

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param s a multi-token format string - may contain multiple individual format strings
     * @throws ParsingException
     */
    static List<FormatString> fromString(String s) throws ParsingException {

        List<FormatString> result = new ArrayList<>();

        upper: for(StringTokenizer st = new StringTokenizer(s, " "); st.hasMoreTokens(); ) {

            String tok = st.nextToken();

            token: while(tok.length() > 0) {

                for(FormatString fs: FormatStrings.values()) {
                    if (tok.startsWith(fs.getLiteral())) {
                        result.add(fs);
                        tok = tok.substring(fs.getLiteral().length());
                        continue token;
                    }
                }

                // try known parameterized format strings
                ParameterizedFormatString pfs = ParameterizedFormatString.parameterizedFormatFromString(tok);
                if (pfs != null) {
                    result.add(pfs);
                    continue upper;
                }

                throw new ParsingException("unknown httpd format element '" + tok + "'");
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
     * Turns a string representation of the format element, as read from the log, into a typed value.
     *
     * @return a typed value, never null.
     *
     * @throws ParsingException if the string representation does not match the format element.
     */
    Object parse(String logStringRepresentation) throws ParsingException;

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
    FormatString getMatchingEnclosure();

    /**
     * @return may return null if conversion is not possible (the value is null, the FormatString - double quotes -
     * cannot generate a property, etc.)
     */
    Property toProperty(Object value);

}
