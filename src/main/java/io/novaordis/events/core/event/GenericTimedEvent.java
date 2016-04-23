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

    private Long timestamp;

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

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
