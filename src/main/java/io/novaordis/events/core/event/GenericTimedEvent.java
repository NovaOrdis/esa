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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class GenericTimedEvent extends GenericEvent implements TimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    //
    // the timestamp in milliseconds from the GMT epoch, not accounting for timezone and daylight saving offsets.
    //
    private Long timestamp;

    //
    // the timezone offset, in milliseconds, as specified by the source of the event (logs, for example). If
    // the timestamp was specified as "12/31/16 10:00:00 -0800" in the log, then the timezone offset is
    // -8 * 3600 * 1000 ms. Null if no timezone offset specified by the source of the event. We need this information
    // to produce timestamps similar to the original ones, when the processing is done in an arbitrary timezone.
    //
    private Integer timezoneOffsetMs;

    // Constructors ----------------------------------------------------------------------------------------------------

    public GenericTimedEvent() {
        this(null);
    }

    public GenericTimedEvent(Long timestamp) {
        this.timestamp = timestamp;
    }

    // TimedEvent implementation ---------------------------------------------------------------------------------------

    @Override
    public Long getTimestamp() {

        return timestamp;
    }

    @Override
    public void setTimestamp(Long timestamp) {

        this.timestamp = timestamp;
    }

    @Override
    public Integer getTimezoneOffsetMs() {

        return timezoneOffsetMs;
    }

    @Override
    public void setTimezoneOffsetMs(Integer i) {

        this.timezoneOffsetMs = i;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
