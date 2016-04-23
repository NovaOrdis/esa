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

import io.novaordis.events.core.LineFormat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the log format specification - is a list of format strings.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogFormat implements LineFormat {

    // Constants -------------------------------------------------------------------------------------------------------

    // %r %l %u [%t] "%r" %>s %b

    public static final HttpdLogFormat COMMON = new HttpdLogFormat(
            FormatStrings.REMOTE_HOST,
            FormatStrings.REMOTE_LOGNAME,
            FormatStrings.REMOTE_USER,
            FormatStrings.OPENING_BRACKET,
            FormatStrings.TIMESTAMP,
            FormatStrings.CLOSING_BRACKET,
            FormatStrings.DOUBLE_QUOTES,
            FormatStrings.FIRST_REQUEST_LINE,
            FormatStrings.DOUBLE_QUOTES,
            FormatStrings.STATUS_CODE,
            FormatStrings.RESPONSE_ENTITY_BODY_SIZE);

    // this is a pattern used for performance, it contains thread names and request durations and it does not log
    // remote logname, which is useless anyway. For WildFly, this is the pattern:
    //
    // "%I" %h %u [%t] "%r" %s %b %D
    //
    // "&quot;%I&quot; %h %l %u [%t] &quot;%r&quot; &quot;%q&quot; %s %b %D"
    //
    // "thread name" remote-host remote-user [timestamp] "first request line" status-code response-body-size request-duration-ms
    public static final HttpdLogFormat PERFORMANCE_ANALYSIS = new HttpdLogFormat(
            FormatStrings.DOUBLE_QUOTES,
            FormatStrings.THREAD_NAME,
            FormatStrings.DOUBLE_QUOTES,
            FormatStrings.REMOTE_HOST,
            FormatStrings.REMOTE_USER,
            FormatStrings.OPENING_BRACKET,
            FormatStrings.TIMESTAMP,
            FormatStrings.CLOSING_BRACKET,
            FormatStrings.DOUBLE_QUOTES,
            FormatStrings.FIRST_REQUEST_LINE,
            FormatStrings.DOUBLE_QUOTES,
            FormatStrings.DOUBLE_QUOTES,
            FormatStrings.QUERY_STRING,
            FormatStrings.DOUBLE_QUOTES,
            FormatStrings.ORIGINAL_REQUEST_STATUS_CODE,
            FormatStrings.RESPONSE_ENTITY_BODY_SIZE,
            FormatStrings.REQUEST_PROCESSING_TIME_MS);

    // Static ----------------------------------------------------------------------------------------------------------

    public static String replaceSpecialHTMLCharacters(String s) {

        return s.replaceAll("&quot;", "\"");
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<FormatString> formatStrings;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param formatStrings duplicate format elements are acceptable. Quotes (FormatStrings.DOUBLE_QUOTES and
     *                       FormatStrings.SINGLE_QUOTE), if present, must always be balanced, or the constructor
     *                       will throw an exception.
     *
     * @exception IllegalArgumentException on unbalanced quotes
     */
    public HttpdLogFormat(FormatString... formatStrings) throws IllegalArgumentException {

        checkBalancedQuotes(formatStrings);
        this.formatStrings = Arrays.asList(formatStrings);
    }

    /**
     * HTML special characters in format specification are handled correctly, they are replaced with the equivalent
     * characters.
     */
    public HttpdLogFormat(String formatSpecification) throws Exception {

        if (formatSpecification == null) {
            throw new IllegalArgumentException("null format specification");
        }

        formatSpecification = HttpdLogFormat.replaceSpecialHTMLCharacters(formatSpecification);
        this.formatStrings = FormatString.fromString(formatSpecification);
    }

    /**
     * @return the list of format elements, in order. For performance reasons, the implementations could return the
     * underlying storage. It may or may be not mutable, depending on the implementation's decision. It's probably
     * safest to assume it's mutable - but consult the implementation's documentation for more details.
     */
    public List<FormatString> getFormatStrings() {
        return formatStrings;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        String s = "";

        for(Iterator<FormatString> i = formatStrings.iterator(); i.hasNext(); ) {

            s += i.next().getLiteral();
            if (i.hasNext()) {
                s += " ";
            }
        }

        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * @throws IllegalArgumentException on unbalanced quotes.
     */
    private static void checkBalancedQuotes(FormatString[] elements) throws IllegalArgumentException {

        boolean openSingleQuote = false;
        boolean openDoubleQuote = false;

        for(FormatString e: elements) {

            if (FormatStrings.DOUBLE_QUOTES.equals(e)) {
                openDoubleQuote = !openDoubleQuote;
            }

            if (FormatStrings.SINGLE_QUOTE.equals(e)) {
                openSingleQuote = !openSingleQuote;
            }
        }

        if (openDoubleQuote) {
            throw new IllegalArgumentException("unbalanced double quotes");
        }

        if (openSingleQuote) {
            throw new IllegalArgumentException("unbalanced single quote");
        }
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
