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

package io.novaordis.events.sampling;

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.core.ProcessingLogicTest;
import org.junit.Test;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/7/16
 */
public class SamplerTest extends ProcessingLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    @Test
    public void processAndGetEvents() throws Exception {

        //
        // noop, because a sampler may need multiple input events to produce an output event.
        //
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected Sampler getProcessingLogicToTest() throws Exception {
        return new Sampler(1000L, "test");
    }

    @Override
    protected Event getInputEventRelevantToProcessingLogic() throws Exception {

        GenericTimedEvent gte = new GenericTimedEvent(1000L);
        gte.setProperty(new IntegerProperty("test", 1));
        return gte;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
