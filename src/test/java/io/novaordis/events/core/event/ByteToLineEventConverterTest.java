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

import io.novaordis.events.core.InputStreamConversionLogic;
import io.novaordis.events.core.InputStreamConversionLogicTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/24/16
 */
public class ByteToLineEventConverterTest extends InputStreamConversionLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void process() throws Exception {

        ByteToLineEventConverter c = getConversionLogicToTest();

        assertFalse(c.process('t'));
        assertTrue(c.getEvents().isEmpty());

        assertFalse(c.process('e'));
        assertTrue(c.getEvents().isEmpty());

        assertFalse(c.process('s'));
        assertTrue(c.getEvents().isEmpty());

        assertFalse(c.process('t'));
        assertTrue(c.getEvents().isEmpty());

        assertTrue(c.process('\n'));

        List<Event> events = c.getEvents();
        assertEquals(1, events.size());
        assertTrue(c.getEvents().isEmpty());

        LineEvent le = (LineEvent)events.get(0);
        assertEquals("test", le.get());
        assertEquals(1, le.getLineNumber());

        assertFalse(c.process('t'));
        assertTrue(c.getEvents().isEmpty());

        assertFalse(c.process('e'));
        assertTrue(c.getEvents().isEmpty());

        assertFalse(c.process('s'));
        assertTrue(c.getEvents().isEmpty());

        assertFalse(c.process('t'));
        assertTrue(c.getEvents().isEmpty());

        assertFalse(c.process('2'));
        assertTrue(c.getEvents().isEmpty());

        assertTrue(c.process('\n'));

        events = c.getEvents();
        assertEquals(1, events.size());
        assertTrue(c.getEvents().isEmpty());

        le = (LineEvent)events.get(0);
        assertEquals("test2", le.get());
        assertEquals(2, le.getLineNumber());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    @Override
    protected ByteToLineEventConverter getConversionLogicToTest() throws Exception {
        return new ByteToLineEventConverter();
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
