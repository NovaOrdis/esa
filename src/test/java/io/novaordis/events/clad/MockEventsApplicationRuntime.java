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

package io.novaordis.events.clad;

import io.novaordis.events.core.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/21/16
 */
public class MockEventsApplicationRuntime extends EventsApplicationRuntime {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MockEventsApplicationRuntime.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private EventProcessor mockEventProcessor;

    // Constructors ----------------------------------------------------------------------------------------------------

    // EventsApplicationRuntime override -------------------------------------------------------------------------------

    @Override
    public void start() {
        log.info(this + " mock started");
    }

    @Override
    public EventProcessor getEventProcessor() {

        if (mockEventProcessor != null) {
            return mockEventProcessor;
        }

        return super.getEventProcessor();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setEventProcessor(EventProcessor ep) {
        this.mockEventProcessor = ep;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
