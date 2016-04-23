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

import io.novaordis.events.core.event.EndOfStreamEvent;
import io.novaordis.events.core.event.Event;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public abstract class InputStreamConversionLogicTest extends ConversionLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(InputStreamConversionLogic.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // process() -------------------------------------------------------------------------------------------------------

    @Test
    public void process_EndOfStream() throws Exception {

        InputStreamConversionLogic c = getConversionLogicToTest();

        // we do generate EOS
        assertTrue(c.process(-1));

        List<Event> events = c.getEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof EndOfStreamEvent);

        assertTrue(c.getEvents().isEmpty());

        try {

            c.process(1);
            fail("should throw exception as the conversion logic is supposed to be closed()");
        }
        catch(ClosedException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void process_NegativeValue() throws Exception {

        InputStreamConversionLogic c = getConversionLogicToTest();

        try {
            c.process(-10);
            fail("should throw IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }

        assertTrue(c.getEvents().isEmpty());
    }

    @Test
    public void process_IllegallyLargeValue() throws Exception {

        InputStreamConversionLogic c = getConversionLogicToTest();

        try {
            c.process(256);
            fail("should throw IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }

        assertTrue(c.getEvents().isEmpty());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected abstract InputStreamConversionLogic getConversionLogicToTest() throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
