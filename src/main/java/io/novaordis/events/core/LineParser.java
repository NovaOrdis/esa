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
import io.novaordis.utilities.parsing.ParsingException;

/**
 * A line parser gets a line (as String) and turns it into an Event or a null (which mean that the parser willingly
 * and legally ignores the line).
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/5/16
 */
@Deprecated
/**
 * Does not address the following cases:
 *
 * 1. Multiple lines make an event (GG logs)
 * 2. Multiple events on the same line (also GC logs).
 */
public interface LineParser {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return if a parser returns null it means that it willingly ignores that line.
     */
    Event parseLine(long lineNumber, String line) throws ParsingException;

    LineFormat getLineFormat();

}
