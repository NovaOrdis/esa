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

package io.novaordis.esa.httpd;

import io.novaordis.esa.Event;
import io.novaordis.esa.EventTest;
import io.novaordis.esa.MockFormatElement;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogEventTest extends EventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLogEventTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void noSuchFormatElement() throws Exception {

        HttpdLogEvent e = getEventToTest();

        //noinspection Convert2Lambda
        Object value = e.getValue(new MockFormatElement("no-such-format-element"));

        assertNull(value);
    }

    @Test
    public void setValue_CorrectType() throws Exception {

        HttpdLogEvent e = new HttpdLogEvent();

        MockFormatElement formatElement = new MockFormatElement("LONG_MOCK", Long.class);

        Object old = e.setValue(formatElement, 1L);
        assertNull(old);

        assertEquals(1L, e.getValue(formatElement));

        Object old2 = e.setValue(formatElement, 2L);
        assertEquals(1L, old2);

        assertEquals(2L, e.getValue(formatElement));
    }

    @Test
    public void setValue_null() throws Exception {

        HttpdLogEvent e = new HttpdLogEvent();

        MockFormatElement formatElement = new MockFormatElement("LONG_MOCK", Long.class);

        Object old = e.setValue(formatElement, null);
        assertNull(old);

        Object old2 = e.setValue(formatElement, 1L);
        assertNull(old2);

        assertEquals(1L, e.getValue(formatElement));

        Object old3 = e.setValue(formatElement, null);
        assertEquals(1L, old3);

        assertNull(e.getValue(formatElement));
    }

    @Test
    public void setValue_IncorrectType() throws Exception {

        HttpdLogEvent e = new HttpdLogEvent();

        MockFormatElement formatElement = new MockFormatElement("LONG_MOCK", Long.class);

        try {
            e.setValue(formatElement, "this should cause failure");
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException iae) {
            log.info(iae.getMessage());
        }
    }

    @Test
    public void getValueCount() throws Exception {

        HttpdLogEvent e = new HttpdLogEvent();

        assertNull(e.getTimestamp());
        assertEquals(0, e.getValueCount());

        e.setTimestamp(new Date(1));
        assertEquals(1, e.getValueCount());

        MockFormatElement mfe = new MockFormatElement("LONG_MOCK", Long.class);
        e.setValue(mfe, 1L);

        assertEquals(2, e.getValueCount());
    }

    @Test
    public void setAndGetTimestamp() throws Exception {

        HttpdLogEvent e = new HttpdLogEvent();

        assertNull(e.getTimestamp());

        assertNull(e.setTimestamp(new Date(1L)));

        assertEquals(new Date(1L), e.getTimestamp());

        assertEquals(new Date(1L), e.setTimestamp(new Date(2L)));
        assertEquals(new Date(2L), e.getTimestamp());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected HttpdLogEvent getEventToTest() throws Exception {

        return new HttpdLogEvent();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
