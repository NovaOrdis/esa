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

package io.novaordis.events.httpd.microparsers;

import io.novaordis.events.api.parser.ParsingException;
import io.novaordis.events.httpd.HttpdFormatString;
import io.novaordis.events.httpd.RequestHeaderHttpdFormatString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parser is useful for particular fields that contain spaces and that were written in the log without being
 * embedded by quotes. It applies field-specific heuristics to figure out the end of the field.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/30/16
 */
public class UrlParser {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String[] HEADER_NAMES = {
            "Referer"
    };

    private static final Logger log = LoggerFactory.getLogger(UrlParser.class);
    private static final boolean debug = log.isDebugEnabled();

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param startFrom the index of the first character of the field
     *
     * @param httpdFormatString - the HTTP format string we parse the string representation for. Used to generate
     *                          a friendlier user message. Null is acceptable, but hte error message will be less
     *                          descriptive.
     *
     * @throws ParsingException in case the content on the line does not make sense for this type of field.
     *
     * @return the index of the character immediately following the field. The character could be a space. If the
     * line ends, -1 is returned.
     *
     * @exception  ParsingException if no known pattern was identified
     */
    public static int identifyEnd(String line, int startFrom, HttpdFormatString httpdFormatString, Long lineNumber)
            throws ParsingException {

        String protocol;
        String host;
        int port = -1;
        String path;
        String query;

        if (line.charAt(startFrom) == '-') {

            return startFrom == line.length() - 1 ? -1 : startFrom + 1;
        }

        int i = line.indexOf("://", startFrom);

        if (i == -1) {

            String msg =
                    "'://' missing from " +
                            (httpdFormatString == null ? "" : httpdFormatString.getLiteral() + " ") +
                            "URL representation";
            throw new ParsingException(msg, lineNumber, startFrom);
        }

        protocol = line.substring(startFrom, i);
        if (debug) { log.debug("protocol: " + protocol); }

        i += 3;
        int j = i;
        char crt = 0;

        // walk host name
        while (j < line.length() && (crt = line.charAt(j)) != '/' && crt != ':' && crt != ' ') {
            j++;
        }

        host = line.substring(i, j);
        if (debug) { log.debug("host: " + host); }

        if (j >= line.length()) {
            // end of the line
            return -1;
        }

        i = j;

        if (crt == ':') {

            j = line.indexOf('/', i);
            String ports = j == -1 ? line.substring(i + 1) : line.substring(i + 1, j);

            try {
                port = Integer.parseInt(ports);
                log.debug("port: " + port);
            }
            catch(Exception e) {
                throw new ParsingException("invalid port value: \"" + ports + "\"", lineNumber, startFrom);
            }

            if (j == -1 || j >= line.length()) {
                // end of the line
                return -1;
            }

            i = j;
        }

        // walk path
        while (j < line.length() && (crt = line.charAt(j)) != '?' && crt != ' ') {
            j++;
        }

        path = line.substring(i, j);
        if (debug) { log.debug("path: " + path); }

        if (j >= line.length()) {
            // end of the line
            return -1;
        }

        if (crt == ' ') {
            return j;
        }

        //
        // there is a query
        //

        //
        // walk the query and allow for spaces
        //

        i = j;

        while(true) {

            j ++;

            if (j >= line.length()) {

                query = line.substring(i);
                if (debug) { log.debug("query: " + query); }

                return -1;
            }

            if (line.charAt(j) == ' ') {

                //
                // if there is a subsequent '&', skip to it, to allow for spaces, unless it belongs to a different URL
                //

                int nextQuerySeparator = line.indexOf('&', j);

                if (nextQuerySeparator == -1) {

                    // no more query separators, we're done
                    query = line.substring(i, j);
                    if (debug) { log.debug("query: " + query); }

                    return j;
                }

                //
                // heuristics, may not always work
                //

                int nextUrl = line.indexOf("://", j);
                int nextSemicolon = line.indexOf(";", j);

                if ((nextUrl != -1 && nextQuerySeparator > nextUrl) ||
                        (nextSemicolon != -1 && nextQuerySeparator > nextSemicolon)) {

                    //
                    // we're done
                    //

                    query = line.substring(i, j);
                    if (debug) { log.debug("query: " + query); }

                    return j;

                }
                else {

                    //
                    // skip to the next query separator
                    //
                    j = nextQuerySeparator;
                }
            }
        }
    }

    public static boolean isUrl(HttpdFormatString fs) {

        if (fs == null) {
            return false;
        }

        if (!(fs instanceof RequestHeaderHttpdFormatString)) {
            return false;
        }

        RequestHeaderHttpdFormatString rhfs = (RequestHeaderHttpdFormatString)fs;

        for(String s: HEADER_NAMES) {

            if (s.equalsIgnoreCase(rhfs.getHeaderName())) {
                return true;
            }
        }

        return false;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private UrlParser() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
