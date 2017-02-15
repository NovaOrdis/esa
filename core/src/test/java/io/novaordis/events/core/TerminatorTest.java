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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public abstract class TerminatorTest extends ComponentTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void inputQueue() throws Exception {

        Terminator terminator = getComponentToTest("test");

        assertNull(terminator.getInputQueue());

        ArrayBlockingQueue<Event> inputQueue = new ArrayBlockingQueue<Event>(1);

        terminator.setInputQueue(inputQueue);

        assertEquals(inputQueue, terminator.getInputQueue());
    }

    @Test
    public void conversionLogic() throws Exception {

        Terminator terminator = getComponentToTest("test");

        assertNull(terminator.getConversionLogic());

        MockConversionLogic conversionLogic = new MockConversionLogic();

        terminator.setConversionLogic(conversionLogic);

        assertEquals(conversionLogic, terminator.getConversionLogic());
    }

    // disable() -------------------------------------------------------------------------------------------------------

    @Test
    public void disable() throws Exception {

        Terminator terminator = getComponentToTest("test");

        assertFalse(terminator.isActive());

        assertFalse(terminator.isDisabled());

        terminator.disable();

        assertTrue(terminator.isDisabled());

        //
        // make sure cannot be started after disabling
        //

        terminator.start();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected abstract Terminator getComponentToTest(String name) throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
