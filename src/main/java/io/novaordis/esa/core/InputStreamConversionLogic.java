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
 * The pluggable event generation logic that turns bytes arriving over an input stream into one or more input events.
 *
 * IMPORTANT: the conversion logic will be always invoked in a single-threaded context.
 *
 * The main concern of the conversion logic is to single-threaded loop and convert bytes into events. The threading
 * and interaction with queues is the responsibility of the owner Initiator.
 *
 * @see Initiator
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public interface InputStreamConversionLogic extends ConversionLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Processes bytes arrived from the input stream. The conversion logic expects all bytes (including -1
     * (end-of-stream)) to be handed over for conversion.
     *
     * The conversion logic <b>is NOT required</b> to send an EndOfStreamEvent event as the last event in the stream
     * when -1 (end-of-stream) is received. If it does so, the EndOfStreamEvent will be gracefully handled by the
     * enclosing component.
     *
     * @return true if one or more Events are available for retrieval, and false if no event is available for retrieval.
     * If the returned value is true, the events can be retrieved and at the same time removed with getEvents(), which
     * is guaranteed to return a non-empty list. The return value has advisory value only, no harm will come from
     * invoking getEvents() if process() returned false, however the result of the getEvents() invocation will be an
     * empty list. Presumably one could invoke process() multiple time without consulting the return value and only
     * belatedly invoke getEvents() - that would probably work assuming the conversion logic instance has sufficient
     * memory at its disposal to temporarily store the events.
     *
     * Note that any exception thrown by this method will cause the component thread to exit, effectively rendering
     * the component inoperable. The recommended method to deal with recoverable processing faults at this level is to
     * generate specific fault events, not to throw exceptions.
     *
     * @see InputStreamConversionLogic#getEvents()
     */
    boolean process(int b);

    /**
     * Retrieves and at the same time removes from the instance any available Events.
     *
     * If no events are available, the method will return an empty list, but never null.
     */
    List<Event> getEvents();

}
