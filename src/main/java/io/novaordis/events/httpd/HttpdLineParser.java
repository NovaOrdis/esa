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

import io.novaordis.events.ParsingException;
import io.novaordis.events.core.LineFormat;
import io.novaordis.events.core.LineParser;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.httpd.microparsers.CookieMicroParser;
import io.novaordis.events.httpd.microparsers.FirstRequestLineMicroParser;
import io.novaordis.events.httpd.microparsers.UserAgentMicroParser;

import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class HttpdLineParser implements LineParser {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private HttpdLogFormat lineFormat;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @throws IllegalArgumentException if the given format is not a valid httpd log file format
     * @throws CorruptedHttpdFormatStringException - this indicates that the format is partially correct and corrupted
     * by the introduction of a bad token we want to let the user know this. The message is human-readable.
     */
    public HttpdLineParser(String format) throws CorruptedHttpdFormatStringException, IllegalArgumentException {

        //
        // We attempt to build a list of HttpdFormatStrings from the given format string. If we succeed, it means
        // the format is valid. If not, it means the format is not valid.
        //

        try {

            lineFormat = new HttpdLogFormat(format);
        }
        //
        // let CorruptedHttpdFormatStringException bubble up
        //
        catch(ParsingException e) {

            //
            // this can't be a httpd log format, starts with a bad token right away
            //
            throw new IllegalArgumentException("invalid httpd log format \"" + format + "\"", e);
        }
    }

    public HttpdLineParser(HttpdLogFormat format) throws IllegalArgumentException {

        this.lineFormat = format;
    }

    public HttpdLineParser(FormatString... formatStrings) throws IllegalArgumentException {

        this.lineFormat = new HttpdLogFormat(formatStrings);
    }

    // LineParser implementation ---------------------------------------------------------------------------------------

    @Override
    public LineFormat getLineFormat() {

        return lineFormat;
    }

    @Override
    public Event parseLine(long lineNumber, String line) throws ParsingException {

        //
        // we ignore empty lines
        //
        if (line == null || line.trim().length() == 0) {
            return null;
        }

        HttpdLogLine logLine = new HttpdLogLine();

        logLine.setLineNumber(lineNumber);

        //
        // we don't perform a match against an aggregated format that would match the entire line because we also want
        // to handle incomplete lines. In the future, we will see if this is useful, and if not replace the current
        // parsing code with matching against the aggregated pattern (TODO: evaluate this later)
        //

        List<FormatString> formatStrings = lineFormat.getFormatStrings();

        char c;
        int cursor = 0;
        FormatString expectedRightEnclosure = null;

        for(FormatString crt : formatStrings) {

            if (expectedRightEnclosure != null) {

                //
                // we only accept just zero or one intermediary format element while waiting for an enclosure to close
                //

                if (crt.equals(expectedRightEnclosure)) {
                    // empty enclosure or consumed enclosure, advance the cursor and continue
                    while(line.charAt(cursor) == ' ') { cursor++; }
                    if ((c = line.charAt(cursor)) != crt.getLiteral().charAt(0)) {
                        throw new ParsingException("expecting " + crt + " but got " + c);
                    }
                    cursor++;
                    while(cursor < line.length() && line.charAt(cursor) == ' ') { cursor++; }
                    expectedRightEnclosure = null;
                    continue;
                }

                //
                // at this point, we'll parse the enclosed element below
                //
            }
            else if (crt.isLeftEnclosure()) {
                expectedRightEnclosure = crt.getMatchingEnclosure();

                // advance the cursor until the first non-blank character and make sure it's the expected left enclosure
                // format element

                while(line.charAt(cursor) == ' ') { cursor++; }
                if ((c = line.charAt(cursor)) != crt.getLiteral().charAt(0)) {
                    throw new ParsingException("expecting " + crt + " but got '" + c + "'");
                }
                cursor++;

                //
                // we're good - iterate until we find the matching right enclosure
                //

                continue;
            }

            Token token = nextToken(line, cursor, crt, expectedRightEnclosure);

            Object o = crt.parse(token.getValue(), lineNumber, cursor);
            logLine.setLogValue(crt, o);

            cursor = token.getCursor();
        }

        return logLine.toEvent();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public HttpdLogFormat getHttpdLogFormat() {
        return lineFormat;
    }

    @Override
    public String toString() {

        return "HttpdLineParser[format: " + lineFormat + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected static Token nextToken(String line, int cursor, FormatString crt, FormatString expectedRightEnclosure)
            throws ParsingException {

        //
        // the situation when the current token is enclosed by quotes is handled by the above layer, and we
        // need to align by handling with priority a non-null expectedRightEnclosure
        //

        int i;

        //
        // find the next closing element, the next pattern or the next gap
        //

        if (expectedRightEnclosure != null) {

            char closingChar = expectedRightEnclosure.getLiteral().charAt(0);
            i = line.indexOf(closingChar, cursor);

        }
        else if (FormatStrings.FIRST_REQUEST_LINE.equals(crt)) {

            i = FirstRequestLineMicroParser.identifyEnd(line, cursor);
        }
        else if (UserAgentMicroParser.isUserAgentRequestHeader(crt)) {

            i = UserAgentMicroParser.identifyEnd(line, cursor);
        }
        else if (CookieMicroParser.isCookieRequestHeader(crt)) {

            i = CookieMicroParser.identifyEnd(line, cursor);
        }
        else {

            //
            // the current token is ended by a space
            //

            i = line.indexOf(' ', cursor);
        }

        i = i == -1 ? line.length() : i;
        String value = line.substring(cursor, i);

        //
        // advance the cursor to the next non-blank character
        //
        while(i < line.length() && line.charAt(i) == ' ') { i++; }

        return new Token(i, value);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

    /**
     * Bundles together a string token and an int cursor value. Package protected for testing.
     */
    static class Token {

        private int cursor;
        private String value; // may contain spaces

        /**
         * @param value may contain spaces.
         */
        public Token(int cursor, String value) {
            this.cursor = cursor;
            this.value = value;
        }

        public int getCursor() {
            return cursor;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return cursor + ":" + value;
        }
    }
}
