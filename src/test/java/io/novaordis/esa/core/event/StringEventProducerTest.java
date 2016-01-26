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

package io.novaordis.esa.core.event;

import io.novaordis.esa.core.InputStreamConversionLogicTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class StringEventProducerTest extends InputStreamConversionLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void oneLine() throws Exception {

        StringEventProducer sep = getConversionLogicToTest();

        assertTrue(sep.getEvents().isEmpty());

        assertFalse(sep.process('h'));
        assertTrue(sep.getEvents().isEmpty());

        assertFalse(sep.process('e'));
        assertTrue(sep.getEvents().isEmpty());

        assertFalse(sep.process('l'));
        assertTrue(sep.getEvents().isEmpty());

        assertFalse(sep.process('l'));
        assertTrue(sep.getEvents().isEmpty());

        assertFalse(sep.process('o'));
        assertTrue(sep.getEvents().isEmpty());

        assertTrue(sep.process('\n'));

        List<Event> result = sep.getEvents();
        assertEquals(1, result.size());
        StringEvent se = (StringEvent)result.get(0);
        assertEquals("hello", se.get());

        assertTrue(sep.getEvents().isEmpty());
    }

    @Test
    public void emptyLine() throws Exception {

        StringEventProducer sep = getConversionLogicToTest();

        assertTrue(sep.getEvents().isEmpty());

        assertTrue(sep.process('\n'));
        List<Event> result = sep.getEvents();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        StringEvent se = (StringEvent)result.get(0);
        assertEquals("", se.get());

        assertTrue(sep.getEvents().isEmpty());

        assertTrue(sep.process('\n'));

        result = sep.getEvents();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        se = (StringEvent)result.get(0);
        assertEquals("", se.get());

        assertTrue(sep.getEvents().isEmpty());
    }

    @Test
    public void endOfStreamAfterSomeCharacters() throws Exception {

        StringEventProducer sep = getConversionLogicToTest();

        assertTrue(sep.getEvents().isEmpty());

        assertFalse(sep.process('h'));

        List<Event> result = sep.getEvents();
        assertTrue(result.isEmpty());

        assertTrue(sep.process(-1));

        result = sep.getEvents();
        assertEquals(2, result.size());
        StringEvent se = (StringEvent)result.get(0);
        assertEquals("h", se.get());
        EndOfStreamEvent eos = (EndOfStreamEvent)result.get(1);
        assertNotNull(eos);
    }

    @Test
    public void endOfStreamAfterAfterNewLine() throws Exception {

        StringEventProducer sep = getConversionLogicToTest();

        assertTrue(sep.getEvents().isEmpty());

        assertFalse(sep.process('h'));

        List<Event> result = sep.getEvents();
        assertTrue(result.isEmpty());

        assertTrue(sep.process('\n'));

        result = sep.getEvents();
        assertEquals(1, result.size());
        StringEvent se = (StringEvent)result.get(0);
        assertEquals("h", se.get());

        result = sep.getEvents();
        assertTrue(result.isEmpty());

        assertTrue(sep.process(-1));

        result = sep.getEvents();
        assertEquals(1, result.size());
        EndOfStreamEvent eos = (EndOfStreamEvent)result.get(0);
        assertNotNull(eos);
    }

    @Test
    public void endOfStreamRightAtTheBeginning() throws Exception {

        StringEventProducer sep = getConversionLogicToTest();

        assertTrue(sep.getEvents().isEmpty());

        assertTrue(sep.process(-1));
        List<Event> result = sep.getEvents();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        EndOfStreamEvent eos = (EndOfStreamEvent)result.get(0);
        assertNotNull(eos);

        assertTrue(sep.getEvents().isEmpty());
    }

//    @Test
//    public void cRAndLfAndCombinations() throws Exception {
//        fail("return here");
//    }
//
//    @Test
//    public void windows() throws Exception {
//        fail("return here");
//    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected StringEventProducer getConversionLogicToTest() throws Exception {

        return new StringEventProducer();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
