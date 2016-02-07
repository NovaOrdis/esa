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

import io.novaordis.esa.ParsingException;
import io.novaordis.esa.core.LineFormat;
import io.novaordis.esa.core.LineParser;
import io.novaordis.esa.core.event.Event;

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
     * @throws InvalidFormatStringException we determined that the format specification <b>is</b> a httpd log file
     * format but we find an incorrectly specified format string.
     */
    public HttpdLineParser(String format) throws IllegalArgumentException, InvalidFormatStringException {

        //
        // We attempt to build a list of HttpdFormatStrings from the given format string. If we succeed, it means
        // the format is valid. If not, it means the format is not valid.
        //

        try {
            lineFormat = new HttpdLogFormat(format);
        }
        catch (Exception e) {
            //
            // invalid httpd format
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
    public Event parseLine(String line) throws ParsingException {

        HttpdLogLine e = new HttpdLogLine();

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
            e.setLogValue(fe, o);

            // advance the cursor to the next non-blank character
            while(i < line.length() && line.charAt(i) == ' ') { i++; }
            cursor = i;
        }

        return e.toEvent();
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
