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
    public void happyPath() throws Exception {

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

//    @Test
//    public void oneLine() throws Exception {
//
//        InputStreamConverter istec = getByteLogicToTest();
//        assertTrue(istec.process('h').isEmpty());
//        assertTrue(istec.process('e').isEmpty());
//        assertTrue(istec.process('l').isEmpty());
//        assertTrue(istec.process('l').isEmpty());
//        assertTrue(istec.process('o').isEmpty());
//
//        List<OldEvent> events = istec.process('\n');
//        assertEquals(1, events.size());
//        StringOldEvent se = (StringOldEvent)events.get(0);
//        String s = se.get();
//        assertEquals("hello", s);
//    }
//
//    @Test
//    public void emptyLine() throws Exception {
//
//        InputStreamConverter istec = getByteLogicToTest();
//
//        List<OldEvent> events = istec.process('\n');
//        assertEquals(1, events.size());
//
//        StringOldEvent se = (StringOldEvent)events.get(0);
//        assertEquals("", se.get());
//
//        events = istec.process('\n');
//        assertEquals(1, events.size());
//        assertEquals("", ((StringOldEvent)events.get(0)).get());
//    }
//
//    @Test
//    public void endOfStreamAfterSomeCharacters() throws Exception {
//
//        InputStreamConverter istec = getByteLogicToTest();
//
//        assertTrue(istec.process('h').isEmpty());
//
//        List<OldEvent> events = istec.process(-1);
//        assertEquals(2, events.size());
//        StringOldEvent se = (StringOldEvent)events.get(0);
//        assertEquals("h", se.get());
//        assertTrue(events.get(1) instanceof EndOfStreamOldEvent);
//    }
//
//    @Test
//    public void endOfStreamAfterNewLine() throws Exception {
//
//        InputStreamConverter istec = getByteLogicToTest();
//
//        assertTrue(istec.process('h').isEmpty());
//        List<OldEvent> events = istec.process('\n');
//        assertEquals(1, events.size());
//        assertEquals("h", ((StringOldEvent)events.get(0)).get());
//
//        events = istec.process(-1);
//        assertEquals(1, events.size());
//        assertTrue(events.get(0) instanceof EndOfStreamOldEvent);
//    }
//
//
//    @Test
//    public void endOfStreamRightAtTheBeginning() throws Exception {
//
//        InputStreamConverter istec = getByteLogicToTest();
//
//        List<OldEvent> events = istec.process(-1);
//        assertEquals(1, events.size());
//        assertTrue(events.get(0) instanceof EndOfStreamOldEvent);
//    }
//
////    @Test
////    public void cRAndLfAndCombinations() throws Exception {
////        fail("return here");
////    }
////
////    @Test
////    public void windows() throws Exception {
////        fail("return here");
////    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected StringEventProducer getConversionLogicToTest() throws Exception {

        return new StringEventProducer();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
