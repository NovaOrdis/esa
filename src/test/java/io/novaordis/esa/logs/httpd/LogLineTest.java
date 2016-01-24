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

package io.novaordis.esa.logs.httpd;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class LogLineTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(LogLineTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void noSuchFormatElement() throws Exception {

        LogLine e = new LogLine();

        //noinspection Convert2Lambda
        Object value = e.getLogValue(new MockFormatString("no-such-format-element"));

        assertNull(value);
    }

    @Test
    public void setValue_CorrectType() throws Exception {

        LogLine e = new LogLine();

        MockFormatString formatElement = new MockFormatString("LONG_MOCK", Long.class);

        Object old = e.setLogValue(formatElement, 1L);
        assertNull(old);

        assertEquals(1L, e.getLogValue(formatElement));

        Object old2 = e.setLogValue(formatElement, 2L);
        assertEquals(1L, old2);

        assertEquals(2L, e.getLogValue(formatElement));
    }

    @Test
    public void setValue_null() throws Exception {

        LogLine e = new LogLine();

        MockFormatString formatElement = new MockFormatString("LONG_MOCK", Long.class);

        Object old = e.setLogValue(formatElement, null);
        assertNull(old);

        Object old2 = e.setLogValue(formatElement, 1L);
        assertNull(old2);

        assertEquals(1L, e.getLogValue(formatElement));

        Object old3 = e.setLogValue(formatElement, null);
        assertEquals(1L, old3);

        assertNull(e.getLogValue(formatElement));
    }

    @Test
    public void setValue_IncorrectType() throws Exception {

        LogLine e = new LogLine();

        MockFormatString formatElement = new MockFormatString("LONG_MOCK", Long.class);

        try {
            e.setLogValue(formatElement, "this should cause failure");
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException iae) {
            log.info(iae.getMessage());
        }
    }

//    @Test
//    public void getValueCount() throws Exception {
//
//        LogLine e = new LogLine();
//
//        assertNull(e.timestamp);
//        assertEquals(0, e.getPropertyCount());
//
//        e.timestamp = new Date(1);
//        assertEquals(1, e.getPropertyCount());
//
//        MockFormatString mfe = new MockFormatString("LONG_MOCK", Long.class);
//        e.setLogValue(mfe, 1L);
//
//        assertEquals(2, e.getPropertyCount());
//    }
//
//    @Test
//    public void setAndGetTimestamp() throws Exception {
//
//        LogLine e = new LogLine();
//
//        assertNull(e.timestamp);
//
//        assertNull(e.timestamp = new Date(1L));
//
//        assertEquals(new Date(1L), e.timestamp);
//
//        assertEquals(new Date(1L), e.setTimestamp(new Date(2L)));
//        assertEquals(new Date(2L), e.getTimestamp());
//    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
