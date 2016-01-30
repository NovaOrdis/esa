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

import io.novaordis.esa.logs.ParsingException;

import java.util.List;

/**
 * A log parser instance is configured with a specific log format and is capable of parsing log lines producing HttpdLogLine
 * instances.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public class HttpdLogParser {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private LogFormat logFormat;

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpdLogParser(LogFormat logFormat) {

        this.logFormat = logFormat;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public LogFormat getLogFormat() {
        return logFormat;
    }

    public HttpdLogLine parse(String line) throws ParsingException {

        HttpdLogLine e = new HttpdLogLine();

        //
        // we don't perform a match against am aggregated format that would match the entire line because
        // we also want to handle incomplete lines. In the future, we will see if this is useful, and if not
        // replace the current parsing code with matching against the aggregated pattern (TODO: evaluate this later)
        //

        List<FormatString> fes = logFormat.getFormatStrings();

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

        return e;
    }

    @Override
    public String toString() {

        return "format: " + getLogFormat();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}