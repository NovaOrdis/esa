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

package io.novaordis.events.core;

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.parser.ParsingException;
import io.novaordis.events.core.event.MockEvent;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class MockLineParser implements LineParser {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String VALID_LINE = "MockLineParser valid line";
    public static final String INVALID_LINE = "MockLineParser invalid line";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // LineParser implementation ---------------------------------------------------------------------------------------

    @Override
    public LineFormat getLineFormat() {
        throw new RuntimeException("getLineFormat() NOT YET IMPLEMENTED");
    }

    @Override
    public Event parseLine(long lineNumber, String line) throws ParsingException {

        if (VALID_LINE.equals(line)) {

            return new MockEvent();

        }
        else if (INVALID_LINE.equals(line)) {

            throw new ParsingException("invalid line: " + line);
        }
        else {
            throw new RuntimeException("we don't know how to parse \"" + line + "\"");
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return a line that would be parsed correctly and generate a MockEvent
     */
    public String getValidLine() {
        return VALID_LINE;
    }

    /**
     * @return a line that would cause a parsing failure
     */
    public String getInvalidLine() {
        return INVALID_LINE;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
