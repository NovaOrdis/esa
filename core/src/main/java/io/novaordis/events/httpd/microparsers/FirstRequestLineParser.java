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

import io.novaordis.events.ParsingException;

/**
 * A parser is useful for particular fields that contain spaces and that were written in the log without being
 * embedded by quotes. It applies field-specific heuristics to figure out the end of the field.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/30/16
 */
public class FirstRequestLineParser {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param startFrom the index of the first character of the field
     * @throws ParsingException in case the content on the line does not make sense for this type of field.
     *
     * @return the index of the character immediately following the field. The character could be a space. If the
     * line ends, -1 is returned.
     */
    public static int identifyEnd(String line, int startFrom) throws ParsingException {

        //
        // no quotes, and the first line has multiple spaces
        //

        int methodPathGap = line.indexOf(' ', startFrom);

        if (methodPathGap == -1) {
            throw new ParsingException(
                    "expected space between HTTP method and path for the first request line not found, log line: \"" +
                            line + "\"");
        }

        int pathHttpVersionGap = line.indexOf(' ',  methodPathGap + 1);

        if (pathHttpVersionGap == -1) {
            throw new ParsingException(
                    "expected space between path and HTTP version for the first request line not found, log line: \"" +
                            line + "\"");
        }

        //
        // if the line ends, we return -1
        //
        return line.indexOf(' ', pathHttpVersionGap + 1);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private FirstRequestLineParser() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
