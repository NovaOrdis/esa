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

package io.novaordis.events.core.event;

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.LineEvent;
import io.novaordis.events.core.ClosedException;
import io.novaordis.events.core.InputStreamConversionLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Breaks the input stream content into lines and send the LineEvents downstream.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class ByteToLineEventConverter implements InputStreamConversionLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean closed;
    private StringBuilder sb;
    private List<Event> buffer;

    // 1-based line numbering
    private long lineNumber;

    // Constructors ----------------------------------------------------------------------------------------------------

    public ByteToLineEventConverter() {

        this.closed = false;
        this.sb = new StringBuilder();
        this.buffer = new ArrayList<>();

        // text files start with line 1, not line 0
        this.lineNumber = 1;
    }

    // InputStreamConversionLogic implementation -----------------------------------------------------------------------

    @Override
    public boolean process(int b) throws ClosedException{

        if (closed) {
            throw new ClosedException(this + " is closed");
        }

        if (b < -1) {

            throw new IllegalArgumentException("input: " + b);
        }
        else if (b == -1) {

            //
            // end of stream
            //

            if (sb.length() == 0) {

                buffer.add(new EndOfStreamEvent());
            }
            else
            {
                buffer.add(new LineEvent(lineNumber ++, sb.toString()));
                buffer.add(new EndOfStreamEvent());
            }

            sb.setLength(0);
            closed = true;
        }
        else if (b == '\n') {

            buffer.add(new LineEvent(lineNumber ++, sb.toString()));
            sb.setLength(0);
        }
        else //noinspection StatementWithEmptyBody
            if (b == '\r') {

            //
            // ignore it, as we expect an immediately following '\n'.
            // TODO this implementation is incomplete, we don't handle situations like '\r' by itself, or '\r\r'.
            //
        }
        else if (b <= 255) {
            sb.append((char) b);
        }
        else {
            throw new IllegalArgumentException("input: " + b);
        }

        return !buffer.isEmpty();
    }

    @Override
    public List<Event> getEvents() {

        if (buffer.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> result = new ArrayList<>(buffer);
        buffer.clear();
        return result;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * For testing only.
     */
    StringBuilder getStringBuilder() {
        return sb;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
