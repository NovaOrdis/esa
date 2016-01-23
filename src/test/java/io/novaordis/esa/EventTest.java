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

package io.novaordis.esa;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public abstract class EventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void emptyEvent() throws Exception {

        Event e = getEventToTest();
        assertNull(e.getTimestamp());
        assertEquals(0, e.getPropertyCount());
        assertTrue(e.getPropertyNames().isEmpty());
        assertNull(e.getPropertyValue("I-am-sure-there-is-no-such-property"));
    }

    @Test
    public void settingAndGettingProperties() throws Exception {

        Event e = getEventToTest();
        assertEquals(0, e.getPropertyCount());

        Object o = e.setPropertyValue("s", "this is a string");
        assertNull(o);

        assertEquals("this is a string", e.getPropertyValue("s"));

        o = e.setPropertyValue("s", "this is another string");
        assertEquals("this is a string", o);

        assertEquals("this is another string", e.getPropertyValue("s"));
        o = e.setPropertyValue("s", null);

        assertEquals("this is another string", o);
        assertNull(e.getPropertyValue("s"));
    }


    @Test
    public void keyOrder() throws Exception {

        Event e = getEventToTest();
        assertEquals(0, e.getPropertyCount());

        e.setPropertyValue("s", "this is a string");
        e.setPropertyValue("i", 1L);

        List<String> names = e.getPropertyNames();

        assertEquals(2, names.size());
        assertEquals("i", names.get(0));
        assertEquals("s", names.get(1));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract Event getEventToTest() throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
