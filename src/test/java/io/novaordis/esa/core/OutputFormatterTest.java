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

package io.novaordis.esa.core;

import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.FaultEvent;
import io.novaordis.esa.core.event.MockEvent;
import io.novaordis.esa.core.event.MockProperty;
import io.novaordis.esa.core.event.MockTimedEvent;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/2/16
 */
public class OutputFormatterTest extends OutputStreamConversionLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // process() -------------------------------------------------------------------------------------------------------

    @Test
    public void process_WeDumpTheFaultOnFaultEvent() throws Exception {

        OutputFormatter c = getConversionLogicToTest();

        Event event = new FaultEvent("test message", new RuntimeException("SYNTHETIC"));
        assertTrue(c.process(event));

        byte[] content = c.getBytes();
        String s = new String(content);

        //
        // TODO we may want to consider to send the fault events to stderr
        //

        assertEquals(event.toString() + "\n", s);
    }

    @Test
    public void process_RegularUntimedEvent() throws Exception {

        OutputFormatter c = getConversionLogicToTest();

        MockEvent me = new MockEvent();

        // priority inverse to the name order
        me.setProperty(new MockProperty("A", "A value", 3));
        me.setProperty(new MockProperty("B", "B value", 2));
        me.setProperty(new MockProperty("C", "C value", 1));

        assertTrue(c.process(me));

        byte[] content = c.getBytes();
        String s = new String(content);

        assertEquals("C value, B value, A value\n", s);
    }

    @Test
    public void process_RegularTimedEvent() throws Exception {

        OutputFormatter c = getConversionLogicToTest();

        Date d = new SimpleDateFormat("MM/yy/dd HH:mm:ss").parse("01/16/01 01:01:01");

        MockTimedEvent me = new MockTimedEvent(d.getTime());

        // priority inverse to the name order
        me.setProperty(new MockProperty("A", "A value", 3));
        me.setProperty(new MockProperty("B", "B value", 2));
        me.setProperty(new MockProperty("C", "C value", 1));

        assertTrue(c.process(me));

        byte[] content = c.getBytes();
        String s = new String(content);

        String expected = OutputFormatter.DEFAULT_TIMESTAMP_FORMAT.format(d) + ", C value, B value, A value\n";
        assertEquals(expected, s);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected OutputFormatter getConversionLogicToTest() throws Exception {

        return new OutputFormatter();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
