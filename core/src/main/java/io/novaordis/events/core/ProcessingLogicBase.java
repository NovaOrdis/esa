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

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.FaultEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles EndOfStream events, among other things
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public abstract class ProcessingLogicBase implements ProcessingLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean closed;
    private List<Event> eventBuffer;

    // Constructors ----------------------------------------------------------------------------------------------------

    protected ProcessingLogicBase() {

        this.closed = false;
        this.eventBuffer = new ArrayList<>();
    }

    // ProcessingLogic implementation ----------------------------------------------------------------------------------

    @Override
    public boolean process(Event e) throws ClosedException {

        if (closed) {
            throw new ClosedException(this + " is closed");
        }

        if (e instanceof EndOfStreamEvent) {
            closed = true;
            return false;
        }

        try {

            Event outputEvent = processInternal(e);

            //
            // protection against subclasses returning null here, only add if not null; a null coming from a
            // subclass at this point usually means "I am ignoring this event"
            //
            if (outputEvent != null) {
                eventBuffer.add(outputEvent);
            }
        }
        catch (IllegalStateException ise) {
            //
            // fail fast on illegal state exception, it means the pipeline was improperly configured and won't be
            // able to process anything
            //
            throw ise;
        }
        catch (Throwable t) {
            //
            // generate fault event
            //
            eventBuffer.add(new FaultEvent(t));
        }
        return !eventBuffer.isEmpty();
    }

    @Override
    public List<Event> getEvents() {

        if (eventBuffer.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = new ArrayList<>(eventBuffer);
        eventBuffer.clear();
        return events;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public boolean isClosed() {
        return closed;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * Gives subclasses access to the event buffer.
     */
    protected List<Event> getEventBuffer() {
        return eventBuffer;
    }

    /**
     * All useful implementations so far turn an input event into just one single output event. If we'll ever need
     * input event -> multiple output events conversion, we'll refactor. Guaranteed to never receive EndOfStreamEvent.
     *
     * See TODO n7k4S
     *
     * It is also possible that the invocation returns no Event, so this layer should graciously handle null.
     */
    protected abstract Event processInternal(Event e) throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
