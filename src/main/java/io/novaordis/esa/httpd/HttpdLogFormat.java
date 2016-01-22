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

import io.novaordis.esa.LogFormat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogFormat implements LogFormat {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final HttpdLogFormat COMMON = new HttpdLogFormat(
            HttpdFormatElement.REMOTE_HOST,
            HttpdFormatElement.REMOTE_LOGNAME,
            HttpdFormatElement.REMOTE_USER,
            HttpdFormatElement.OPENING_BRACKET,
            HttpdFormatElement.TIMESTAMP,
            HttpdFormatElement.CLOSING_BRACKET,
            HttpdFormatElement.DOUBLE_QUOTES,
            HttpdFormatElement.FIRST_REQUEST_LINE,
            HttpdFormatElement.DOUBLE_QUOTES);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<HttpdFormatElement> formatElements;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param formatElements duplicate format elements are acceptable. Quotes (HttpdFormatElement.DOUBLE_QUOTES and
     *                       HttpdFormatElement.SINGLE_QUOTE), if present, must always be balanced, or the constructor
     *                       will throw an exception.
     *
     * @exception IllegalArgumentException on unbalanced quotes
     */
    public HttpdLogFormat(HttpdFormatElement... formatElements) throws IllegalArgumentException {

        checkBalancedQuotes(formatElements);
        this.formatElements = Arrays.asList(formatElements);
    }

    // LogFormat implementation ----------------------------------------------------------------------------------------

    /**
     * @see LogFormat#getFormatElements()
     */
    @Override
    public List<HttpdFormatElement> getFormatElements() {
        return formatElements;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        String s = "";

        for(Iterator<HttpdFormatElement> i = formatElements.iterator(); i.hasNext(); ) {

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
    private static void checkBalancedQuotes(HttpdFormatElement[] elements) throws IllegalArgumentException {

        boolean openSingleQuote = false;
        boolean openDoubleQuote = false;

        for(HttpdFormatElement e: elements) {

            if (HttpdFormatElement.DOUBLE_QUOTES.equals(e)) {
                openDoubleQuote = !openDoubleQuote;
            }

            if (HttpdFormatElement.SINGLE_QUOTE.equals(e)) {
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
