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

package io.novaordis.esa.processor;

import io.novaordis.esa.event.Event;
import io.novaordis.esa.event.special.EndOfStreamEvent;
import io.novaordis.esa.event.special.StringEvent;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/23/16
 */
public class InputStreamToEventConvertorTest extends ByteLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void oneLine() throws Exception {

        InputStreamToEventConvertor istec = getByteLogicToTest();
        assertTrue(istec.process('h').isEmpty());
        assertTrue(istec.process('e').isEmpty());
        assertTrue(istec.process('l').isEmpty());
        assertTrue(istec.process('l').isEmpty());
        assertTrue(istec.process('o').isEmpty());

        List<Event> events = istec.process('\n');
        assertEquals(1, events.size());
        StringEvent se = (StringEvent)events.get(0);
        String s = se.get();
        assertEquals("hello", s);
    }

    @Test
    public void emptyLine() throws Exception {

        InputStreamToEventConvertor istec = getByteLogicToTest();

        List<Event> events = istec.process('\n');
        assertEquals(1, events.size());

        StringEvent se = (StringEvent)events.get(0);
        assertEquals("", se.get());

        events = istec.process('\n');
        assertEquals(1, events.size());
        assertEquals("", ((StringEvent)events.get(0)).get());
    }

    @Test
    public void endOfStreamAfterSomeCharacters() throws Exception {

        InputStreamToEventConvertor istec = getByteLogicToTest();

        assertTrue(istec.process('h').isEmpty());

        List<Event> events = istec.process(-1);
        assertEquals(2, events.size());
        StringEvent se = (StringEvent)events.get(0);
        assertEquals("h", se.get());
        assertTrue(events.get(1) instanceof EndOfStreamEvent);
    }

    @Test
    public void endOfStreamAfterNewLine() throws Exception {

        InputStreamToEventConvertor istec = getByteLogicToTest();

        assertTrue(istec.process('h').isEmpty());
        List<Event> events = istec.process('\n');
        assertEquals(1, events.size());
        assertEquals("h", ((StringEvent)events.get(0)).get());

        events = istec.process(-1);
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof EndOfStreamEvent);
    }


    @Test
    public void endOfStreamRightAtTheBeginning() throws Exception {

        InputStreamToEventConvertor istec = getByteLogicToTest();

        List<Event> events = istec.process(-1);
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof EndOfStreamEvent);
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
    protected InputStreamToEventConvertor getByteLogicToTest() throws Exception {

        return new InputStreamToEventConvertor();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
