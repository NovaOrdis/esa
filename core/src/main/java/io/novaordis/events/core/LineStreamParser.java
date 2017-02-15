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
import io.novaordis.events.api.event.FaultEvent;
import io.novaordis.events.api.event.LineEvent;

/**
 * Logic wired into event processors that receive lines from their queues (in form of StringEvents) and parse them into
 * more semantically rich Events.
 *
 * The logic handles EndOfStreamEvents, FaultEvents, etc. as these are meaningless to the delegate LineParsers.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/5/16
 */
public class LineStreamParser extends ProcessingLogicBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private LineParser lineParser;

    // Constructors ----------------------------------------------------------------------------------------------------

    public LineStreamParser() {

        this(null);
    }

    public LineStreamParser(LineParser lineParser) {

        this.lineParser = lineParser;
    }

    // ProcessingLogicBase implementation ------------------------------------------------------------------------------

    @Override
    protected Event processInternal(Event e) {

        //
        // we relay FaultEvents
        //

        if (e instanceof FaultEvent) {
            return e;
        }

        //
        // we generate FaultEvents if we get an unknown event
        //

        if (!(e instanceof LineEvent)) {
            return new FaultEvent(this + " does not know how to handle " + e);
        }

        LineEvent le = (LineEvent)e;
        String line = le.get();
        long lineNumber = le.getLineNumber();

        if (lineParser == null) {

            //
            // not a Fault, but an invalid state, the pipeline was not assembled correctly, so bubble it up instead of
            // downstream
            //
            throw new IllegalStateException(this + " has a null line parser");
        }

        try {

            return lineParser.parseLine(lineNumber, line);
        }
        catch(Exception ex) {
            // parsing failure, propagate as FaultEvent
            return new FaultEvent(ex);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setLineParser(LineParser lineParser) {
        this.lineParser = lineParser;
    }

    public LineParser getLineParser() {
        return lineParser;
    }

    @Override
    public String toString() {

        return "LineStreamParser[" + lineParser + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
