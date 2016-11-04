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
import io.novaordis.utilities.time.TimeOffset;
import io.novaordis.utilities.time.Timestamp;

import java.text.DateFormat;
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

    // null means no "from" clause
    private Long from;
    private String uncalibratedFrom;

    // null means no "to" clause
    private Long to;
    private String uncalibratedTo;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @throws NoFiltersException if the configuration does not contains any declared filters.
     * @throws IllegalArgumentException if the configuration options are improperly configured or invalid.
     */
    private EventFilter(Configuration configuration) throws NoFiltersException {

        TimestampOption fromOption = (TimestampOption)configuration.getGlobalOption(new TimestampOption("from"));
        TimestampOption toOption = (TimestampOption)configuration.getGlobalOption(new TimestampOption("to"));

        if (fromOption == null && toOption == null) {
            throw new NoFiltersException();
        }

        if (fromOption != null) {

            if (fromOption.isRelative()) {

                //
                // calibrate later
                //

                uncalibratedFrom = fromOption.getValue();
            }
            else {

                DateFormat df = fromOption.getFullFormat();
                Date d;

                try {

                    d = df.parse(fromOption.getValue());
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("failed to parse the value of the \"from\" option into a date", e);
                }

                this.from = d.getTime();
            }
        }

        if (toOption != null) {

            if (toOption.isRelative()) {

                //
                // calibrate later
                //

                uncalibratedTo = toOption.getValue();

            }
            else {

                DateFormat df = toOption.getFullFormat();
                Date d;

                try {

                    d = df.parse(toOption.getValue());
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("failed to parse the value of the \"to\" option into a date", e);
                }

                this.to = d.getTime();
            }
        }
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

        String dayPortion = null;

        // the event UTC time adjusted for the local time offset.
        Long adjustedEventTime = null;

        if (e instanceof TimedEvent) {

            TimedEvent te = (TimedEvent)e;
            Timestamp ts = te.getTimestamp();
            dayPortion = ts.elementToString("MM/dd/yy");
            adjustedEventTime = ts.adjustTime(TimeOffset.getDefault());
        }

        //
        // if relative timestamp and not calibrated yet, do calibrate for timed events
        //

        if (uncalibratedFrom != null && adjustedEventTime != null) {

            //
            // not calibrated yet
            //

            from = TimestampOption.DEFAULT_FULL_FORMAT.parse(dayPortion + " " + uncalibratedFrom).getTime();
            uncalibratedFrom = null;
        }

        if (uncalibratedTo != null && adjustedEventTime != null) {

            //
            // not calibrated yet
            //

            to = TimestampOption.DEFAULT_FULL_FORMAT.parse(dayPortion + " " + uncalibratedTo).getTime();
            uncalibratedTo = null;
        }

        //
        // we're calibrated
        //

        if (from != null) {

            if (adjustedEventTime == null) {

                //
                // we have a "from" filter but not an event timestamp, the event does not match
                //
                return null;
            }

            if (adjustedEventTime < from) {
                //
                // we have a timestamp but falls ahead of the threshold
                //
                return null;
            }
        }

        if (to != null) {

            if (adjustedEventTime == null) {
                //
                // we have a "to" filter but not an event timestamp, the event does not match
                //
                return null;
            }

            if (adjustedEventTime > to) {
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
     * May return null if it was not configured or calibrated.
     */
    public Long getFromTimestampMs() {

        return from;
    }

    /**
     * May return null if it was not configured  or calibrated.
     */
    public Long getToTimestampMs() {

        return to;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * Package protected, for testing only.
     */
    boolean isCalibrated() {

        return uncalibratedFrom == null && uncalibratedTo == null;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
