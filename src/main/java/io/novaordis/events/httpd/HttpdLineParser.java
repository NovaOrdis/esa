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

        HttpdLogLine logLine = new HttpdLogLine();

        logLine.setLineNumber(lineNumber);

        //
        // we don't perform a match against an aggregated format that would match the entire line because we also want
        // to handle incomplete lines. In the future, we will see if this is useful, and if not replace the current
        // parsing code with matching against the aggregated pattern (TODO: evaluate this later)
        //

        List<FormatString> fes = lineFormat.getFormatStrings();

        char c;
        int cursor = 0;
        FormatString expectedRightEnclosure = null;

        for(FormatString fe: fes) {

            if (expectedRightEnclosure != null) {

                //
                // we only accept just zero or one intermediary format element while waiting for an enclosure to close
                //

                if (fe.equals(expectedRightEnclosure)) {
                    // empty enclosure or consumed enclosure, advance the cursor and continue
                    while(line.charAt(cursor) == ' ') { cursor++; }
                    if ((c = line.charAt(cursor)) != fe.getLiteral().charAt(0)) {
                        throw new ParsingException("expecting " + fe + " but got " + c);
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
            else if (fe.isLeftEnclosure()) {
                expectedRightEnclosure = fe.getMatchingEnclosure();

                // advance the cursor until the first non-blank character and make sure it's the expected left enclosure
                // format element

                while(line.charAt(cursor) == ' ') { cursor++; }
                if ((c = line.charAt(cursor)) != fe.getLiteral().charAt(0)) {
                    throw new ParsingException("expecting " + fe + " but got '" + c + "'");
                }
                cursor++;

                //
                // we're good - iterate until we find the matching right enclosure
                //

                continue;
            }

            char closingChar = expectedRightEnclosure != null ? expectedRightEnclosure.getLiteral().charAt(0) : ' ';

            // find the next closing element or the next gap

            int i = line.indexOf(closingChar, cursor);
            i = i == -1 ? line.length() : i;
            String value = line.substring(cursor, i);
            Object o = fe.parse(value);
            logLine.setLogValue(fe, o);

            // advance the cursor to the next non-blank character
            while(i < line.length() && line.charAt(i) == ' ') { i++; }
            cursor = i;
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

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
