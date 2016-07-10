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
import io.novaordis.events.httpd.HttpdFormatString;
import io.novaordis.events.httpd.RequestHeaderHttpdFormatString;
import io.novaordis.events.httpd.ResponseHeaderHttpdFormatString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A micro-parser is useful for particular fields that contain spaces and that were written in the log without being
 * embedded by quotes. It applies field-specific heuristics to figure out the end of the field.
 *
 *
 * Cases that are not handled (if they keep appearing, they will):
 *
 * 1. Cookie fragments are separated by commas, not semicolons.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/30/16
 */
public class CookieParser {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param startFrom the index of the first character of the field.
     * @param lineNumber null is acceptable. If not null, will be used for error reporting.
     * @param httpdFormatString - the HTTP format string we parse the string representation for. Must not be null.

     * @throws ParsingException in case the content on the line does not make sense for this type of field.
     *
     * @return the index of the character immediately following the field. The character could be a space. If the
     * line ends, -1 is returned.
     *
     * @exception  ParsingException if no known pattern was identified
     */
    public static int identifyEnd(String line, int startFrom, HttpdFormatString httpdFormatString, Long lineNumber)
            throws ParsingException {

        if (httpdFormatString == null) {
            throw new IllegalArgumentException("null httpd format string");
        }

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
                cookieFragmentEnd = identifyEndOfTheCookieSeries(
                        line, cookieFragmentStart, httpdFormatString, lineNumber);

                if (cookieFragmentEnd == -1) {
                    //
                    // end of the string
                    //
                    cookieFragmentEnd = line.length();
                }

                if (cookieFragmentEnd == cookieFragmentStart) {
                    //
                    // the segment does not contain any more cookie externalization
                    //
                    break;
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

            //
            // There are situations when the Cookie input header is not quoted, does not end with a non-semicolon space
            // and it is immediately followed by the Set-Cookie output header, so we can't say where one ends and the
            // other begins. We must we apply heuristics by looking for Domain/Path and we allocate that to the output
            // Set-Cookie. So don't add the cookie just identified to the cookie list, first check if there's no
            // Domain/Path pattern coming
            //

            if (isCookieRequestHeader(httpdFormatString) &&
                    doesDomainSpecificationFollow(line, cookieFragmentStart + cookieLogRepresentation.length())) {

                //
                // the cookie we just identified belongs to the next fragment, so don't add it to the list
                //
                break;
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

        //
        // walk past semicolons, if any
        //

        while(nextTokenStartIndex < line.length() && line.charAt(nextTokenStartIndex) == ';') {

            nextTokenStartIndex ++;
        }

        if (nextTokenStartIndex >= line.length()) {

            //
            // end of the string
            //
            nextTokenStartIndex = -1;

        }

        return nextTokenStartIndex;
    }

    public static boolean isCookieHeader(HttpdFormatString fs) {

        return isCookieRequestHeader(fs) || isCookieResponseHeader(fs);
    }

    public static boolean isCookieRequestHeader(HttpdFormatString fs) {

        if (fs == null) {
            return false;
        }

        if (!(fs instanceof RequestHeaderHttpdFormatString)) {
            return false;
        }

        RequestHeaderHttpdFormatString requestHeader = (RequestHeaderHttpdFormatString)fs;
        String parameterName = requestHeader.getParameter();
        return "Cookie".equalsIgnoreCase(parameterName);
    }

    public static boolean isCookieResponseHeader(HttpdFormatString fs) {

        if (fs == null) {
            return false;
        }

        if (!(fs instanceof ResponseHeaderHttpdFormatString)) {
            return false;
        }

        ResponseHeaderHttpdFormatString responseHeader = (ResponseHeaderHttpdFormatString)fs;
        String parameterName = responseHeader.getParameter();
        return "Set-Cookie".equalsIgnoreCase(parameterName);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private CookieParser() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * Invoked at the end of a cookie series, when no more semicolons are present, and we need to apply heuristics
     * to figure out where the last cookie representation ends.
     *
     * @param httpdFormatString - the HTTP format string we parse the string representation for. Used to generate
     *                          a friendlier user message. Null is acceptable, but hte error message will be less
     *                          descriptive.
     *
     * @return index in the string that designates the first position of the next token. If we reach the end of the
     * string, we return -1.
     *
     * @exception ParsingException if the string does not start with a cookie
     */
    static int identifyEndOfTheCookieSeries(String s, int from, HttpdFormatString httpdFormatString, Long lineNumber)
            throws ParsingException {

        //
        // heuristics
        //

        //
        // if the first character that follows after spaces is not the beginning of a word, this is the end of the
        // cookie series
        //

        int i = from;

        while(i < s.length() && s.charAt(i) == ' ') {
            i ++;
        }

        if (i >= s.length()) {
            return -1;
        }

        char c = s.charAt(i);

        if (c != '_' && (c < 'A' || c > 'Z') && (c < 'a' || c > 'z')) {
            //
            // not the beginning of a word
            //
            return from;
        }

        //
        // first space after the first equal sign indicates the last cookie in the series, or
        //

        int equalsIndex = s.indexOf('=', from);

        if (equalsIndex == -1) {

            //
            // no cookie here
            //

            String message = httpdFormatString == null ? "header value" : httpdFormatString.getLiteral();
            message += " missing";

            throw new ParsingException(message, lineNumber, from);
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

    private static boolean doesDomainSpecificationFollow(String line, int startFrom) {

        //
        // walk past semicolons and spaces
        //
        int i = startFrom;
        while (i < line.length() && (line.charAt(i) == ';' || line.charAt(i) == ' ')) {
            i++;
        }

        return i < line.length() && line.substring(i).startsWith("Domain=");
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
