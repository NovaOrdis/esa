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
 * The events that match the filters contained by this instance are are returned unchanged by processInternal(),
 * otherwise processInternal() returns null.
 *
 * If at least one of the timestamp filters (from or to) are relative, the EventFilter instance calibrates itself
 * based on the values of the relative timestamp filters and the events.
 *
 * @see TimestampOption#isRelative()
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/5/16
 */
public class EventFilter extends ProcessingLogicBase {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final long MILLISECONDS_IN_A_DAY = 24L * 60 * 60 * 1000L;

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
    private long to;

    // in case of relative timestamps, this value is initialized during calibration
    private long relativeOffsetDays;

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
        }

        relativeOffsetDays = -1;
    }

    // ProcessingLogicBase overrides -----------------------------------------------------------------------------------

    /**
     * If relative timestamp filters are used, the first timed event sent into the instance via this method calibrates
     * the filter.
     *
     * @exception IllegalStateException - "fail fast" exception, interrupts the pipeline processing and fail right
     * away - a fault is not produced in this case.
     */
    @Override
    protected Event processInternal(Event e) throws Exception {

        Long timestamp = null;

        if (e instanceof TimedEvent) {
            TimedEvent te = (TimedEvent)e;
            timestamp = te.getTimestampGMT();
        }

        //
        // if not calibrated, do calibrate
        //
        if (from > -1 && from < MILLISECONDS_IN_A_DAY && timestamp != null) {

            if (relativeOffsetDays > 0) {
                //
                // redundant calibration
                //
                throw new IllegalStateException("the filter was already calibrated");
            }

            relativeOffsetDays = timestamp / MILLISECONDS_IN_A_DAY;
            from = relativeOffsetDays * MILLISECONDS_IN_A_DAY + from;
        }

        if (to > -1 && to < MILLISECONDS_IN_A_DAY && timestamp != null) {

            //
            // not calibrated yet
            //
            if (relativeOffsetDays < 0) {
                relativeOffsetDays = timestamp / MILLISECONDS_IN_A_DAY;
            }

            to = relativeOffsetDays * MILLISECONDS_IN_A_DAY + to;
        }

        //
        // we're calibrated
        //

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
