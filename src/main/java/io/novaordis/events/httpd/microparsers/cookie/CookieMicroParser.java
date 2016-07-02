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

package io.novaordis.events.httpd.microparsers.cookie;

import io.novaordis.events.ParsingException;
import io.novaordis.events.httpd.FormatString;
import io.novaordis.events.httpd.ParameterizedFormatString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A micro-parser is useful for particular fields that contain spaces and that were written in the log without being
 * embedded by quotes. It applies field-specific heuristics to figure out the end of the field.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/30/16
 */
public class CookieMicroParser {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param startFrom the index of the first character of the field.
     * @param lineNumber null is acceptable. If not null, will be used for error reporting.

     * @throws ParsingException in case the content on the line does not make sense for this type of field.
     *
     * @return the index of the character immediately following the field. The character could be a space. If the
     * line ends, -1 is returned.
     *
     * @exception  ParsingException if no known pattern was identified
     */
    public static int identifyEnd(String line, int startFrom, Long lineNumber) throws ParsingException {

        //
        // easy way out, no value
        //

        if ("- ".equals(line.substring(startFrom, startFrom + 2))) {

            return startFrom + 1;
        }

        List<Cookie> cookies = new ArrayList<>();

        //noinspection UnnecessaryLocalVariable
        int cookieFragmentStart = startFrom;
        String cookieLogRepresentation;
        boolean moreCookies = true;

        while(moreCookies) {

            int cookieFragmentEnd = line.indexOf(';', cookieFragmentStart);

            if (cookieFragmentEnd == -1) {

                //
                // no more semi-colons, we're dealing with the last cookie in the series, so we need to look for
                // a separator other than the semi-colon. One of those separators is space
                //
                cookieFragmentEnd = identifyEndOfTheCookieSeries(line, cookieFragmentStart, lineNumber);
                if (cookieFragmentEnd == -1) {
                    //
                    // end of the string
                    //
                    cookieFragmentEnd = line.length();
                }
                moreCookies = false;
            }

            cookieLogRepresentation = line.substring(cookieFragmentStart, cookieFragmentEnd);

            //
            // check whether we're not in between two different semicolon-separated cookie series
            //

            int boundary = identifyBoundaryBetweenSeries(cookieLogRepresentation);

            if (boundary != -1) {

                //
                // boundary identified
                //
                cookieLogRepresentation = line.substring(cookieFragmentStart, cookieFragmentStart + boundary);
                moreCookies = false;
            }


            Cookie c = new Cookie(cookieLogRepresentation, lineNumber);
            cookies.add(c);
            cookieFragmentStart = cookieFragmentEnd + 1;
        }

        int nextTokenStartIndex = startFrom;

        for(Iterator<Cookie> i = cookies.iterator(); i.hasNext(); ) {

            nextTokenStartIndex += i.next().getLiteral().length();

            if (i.hasNext()) {

                //
                // account for semi-colon separator
                //
                nextTokenStartIndex += 1;
            }
        }

        if (nextTokenStartIndex >= line.length()) {

            //
            // end of the string
            //
            nextTokenStartIndex = -1;

        }

        return nextTokenStartIndex;
    }

    public static boolean isCookieRequestHeader(FormatString fs) {

        if (fs == null) {
            return false;
        }

        if (!(fs instanceof ParameterizedFormatString)) {
            return false;
        }

        ParameterizedFormatString pfs = (ParameterizedFormatString)fs;
        String parameterName = pfs.getParameter();
        return "Cookie".equalsIgnoreCase(parameterName) || "Set-Cookie".equalsIgnoreCase(parameterName);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private CookieMicroParser() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * Invoked at the end of a cookie series, when no more semicolons are present, and we need to apply heuristics
     * to figure out where the last cookie representation ends.
     *
     * @return index in the string that designates the first position of the next token. If we reach the end of the
     * string, we return -1.
     *
     * @exception ParsingException if the string does not start with a cookie
     */
    static int identifyEndOfTheCookieSeries(String s, int from, Long lineNumber) throws ParsingException {

        //
        // heuristics
        //

        //
        // first space after the first equal sign
        //

        int equalsIndex = s.indexOf('=', from);

        if (equalsIndex == -1) {

            //
            // no cookie here
            //

            throw new ParsingException("no cookie detected", lineNumber, from);
        }

        //
        // if no space, means we reached the end of the string and we return -1
        //
        return s.indexOf(' ', equalsIndex);
    }

    /**
     * @return -1 if no boundary is identified
     */
    static int identifyBoundaryBetweenSeries(String s) {

        //
        // two cookies representation separated by a space
        //

        int left = s.indexOf('=');
        int right = s.lastIndexOf('=');

        if (left != -1 && right != -1 && left != right) {

            s = s.substring(left, right);
            int space = s.indexOf(' ');

            if (space == -1) {
                return -1;
            }

            return left + space;
        }

        return -1;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
