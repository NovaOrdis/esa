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

import io.novaordis.clad.option.TimestampOption;
import io.novaordis.events.clad.MockConfiguration;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.MockEvent;
import io.novaordis.events.core.event.MockTimedEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/5/16
 */
public class EventFilterTest extends ProcessingLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(EventFilterTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // buildInstance() -------------------------------------------------------------------------------------------------

    @Test
    public void buildInstance_NoAppropriateOptions() throws Exception {

        EventFilter e = EventFilter.buildInstance(new MockConfiguration());
        assertNull(e);
        log.debug(".");
    }

    @Test
    public void buildInstance() throws Exception {

        String timestamp = "07/07/16 00:00:00";
        String aDayBefore = "07/06/16 00:00:00";

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption(null, "from", timestamp));
        EventFilter e = EventFilter.buildInstance(mc);
        assertNotNull(e);

        Event me = new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse(aDayBefore).getTime());

        assertNull(e.processInternal(me));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected EventFilter getProcessingLogicToTest() throws Exception {

        return new EventFilter();
    }

    @Override
    protected Event getInputEventRelevantToProcessingLogic() throws Exception {

        return new MockEvent();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
