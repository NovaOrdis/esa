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

package io.novaordis.esa.core;

import io.novaordis.esa.core.event.Event;

import java.util.List;

/**
 * Logic wired into event processors that receive lines from their queue (in the form of StringEvents) and parse them
 * into more semantically rich Events.
 *
 * The logic handles EndOfStreamEvents, FaultEvents, etc. as these are meaningless to the delegate LineParsers.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/5/16
 */
public class LineStreamParser implements ProcessingLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private LineParser lineParser;

    // Constructors ----------------------------------------------------------------------------------------------------

    // ProcessingLogic implementation ----------------------------------------------------------------------------------

    @Override
    public boolean process(Event e) throws ClosedException {
        throw new RuntimeException("process() NOT YET IMPLEMENTED");
    }

    @Override
    public List<Event> getEvents() {
        throw new RuntimeException("getEvents() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
