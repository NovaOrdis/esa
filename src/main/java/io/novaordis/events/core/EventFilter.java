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
     */
    public static EventFilter buildInstance(Configuration configuration) {

        EventFilter eventFilter = null;

        TimestampOption from = (TimestampOption)configuration.getGlobalOption(new TimestampOption("from"));
        TimestampOption to = (TimestampOption)configuration.getGlobalOption(new TimestampOption("to"));

        if (from != null || to != null) {

            eventFilter = new EventFilter();
        }

        return eventFilter;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // ProcessingLogicBase overrides -----------------------------------------------------------------------------------

    @Override
    protected Event processInternal(Event e) throws Exception {
        throw new RuntimeException("processInternal() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
