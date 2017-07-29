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

import io.novaordis.events.api.event.Event;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public abstract class InitiatorTest extends ComponentTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void conversionLogic() throws Exception {

        Initiator initiator = getComponentToTest("test");

        assertNull(initiator.getConversionLogic());

        MockConversionLogic conversionLogic = new MockConversionLogic();

        initiator.setConversionLogic(conversionLogic);

        assertEquals(conversionLogic, initiator.getConversionLogic());
    }

    @Test
    public void outputQueue() throws Exception {

        Initiator initiator = getComponentToTest("test");

        assertNull(initiator.getOutputQueue());

        ArrayBlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);

        BlockingQueue<Event> installedQueue = initiator.setOutputQueue(queue);

        assertEquals(queue, installedQueue);
        assertEquals(queue, initiator.getOutputQueue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected abstract Initiator getComponentToTest(String name) throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
