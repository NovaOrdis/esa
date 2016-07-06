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

import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.TimestampOption;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.TimedEvent;

import java.util.Date;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/5/16
 */
public class EventFilter extends ProcessingLogicBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * The class is its own factory. We don't use the constructor because there are situation when the filter cannot
     * be built based on configuration.
     *
     * @return an EventFilter instance if configuration contains the right options, or null if no related options
     * are found.
     *
     * @throws IllegalArgumentException if the configuration options are improperly configured or invalid.
     */
    public static EventFilter buildInstance(Configuration configuration) {

        try {

            return new EventFilter(configuration);
        }
        catch(NoFiltersException e) {
            // that's fine
            return null;
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private long from;
    private boolean fromRelative;
    private long to;
    private boolean toRelative;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @throws NoFiltersException if the configuration does not contains any declared filters.
     * @throws IllegalArgumentException if the configuration options are improperly configured or invalid.
     */
    private EventFilter(Configuration configuration) throws NoFiltersException {

        TimestampOption from = (TimestampOption)configuration.getGlobalOption(new TimestampOption("from"));
        TimestampOption to = (TimestampOption)configuration.getGlobalOption(new TimestampOption("to"));

        if (from == null && to == null) {
            throw new NoFiltersException();
        }

        if (from == null) {

            this.from = -1L;
        }
        else {

            Date d = from.getValue();

            if (d == null) {
                throw new IllegalArgumentException("\"from\" option contains a null timestamp");
            }

            this.from = d.getTime();
            this.fromRelative = from.isRelative();
        }

        if (to == null) {

            this.to = -1L;
        }
        else {

            Date d = to.getValue();

            if (d == null) {
                throw new IllegalArgumentException("\"to\" option contains a null timestamp");
            }

            this.to = d.getTime();
            this.toRelative = to.isRelative();
        }
    }

    // ProcessingLogicBase overrides -----------------------------------------------------------------------------------

    @Override
    protected Event processInternal(Event e) throws Exception {

        Long timestamp = null;

        if (e instanceof TimedEvent) {
            TimedEvent te = (TimedEvent)e;
            timestamp = te.getTimestamp();
        }

        if (from > -1) {

            if (timestamp == null) {
                //
                // we have a "from" filter but not an event timestamp, the event does not match
                //
                return null;
            }

            if (timestamp < from) {
                //
                // we have a timestamp but falls ahead of the threshold
                //
                return null;
            }
        }

        if (to > -1) {

            if (timestamp == null) {
                //
                // we have a "to" filter but not an event timestamp, the event does not match
                //
                return null;
            }

            if (timestamp > to) {
                //
                // we have a timestamp but falls after the threshold
                //
                return null;
            }
        }

        return e;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * May return -1 if it was not configured.
     */
    public long getFromTimestampMs() {

        return from;
    }

    /**
     * May return -1 if it was not configured.
     */
    public long getToTimestampMs() {

        return to;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
