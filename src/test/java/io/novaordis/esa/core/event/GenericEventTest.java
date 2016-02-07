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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class GenericEventTest extends EventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void verifySetPropertyRemembersOrder() throws Exception {

        GenericEvent ge = getEventToTest();

        List<Property> props = ge.getPropertyList();

        assertEquals(0, props.size());

        assertNull(ge.setProperty(new StringProperty("X", "val1")));
        assertNull(ge.setProperty(new StringProperty("I", "val2")));
        assertNull(ge.setProperty(new StringProperty("A", "val3")));

        props = ge.getPropertyList();

        assertEquals(3, props.size());
        assertEquals("X", props.get(0).getName());
        assertEquals("val1", props.get(0).getValue());
        assertEquals("I", props.get(1).getName());
        assertEquals("val2", props.get(1).getValue());
        assertEquals("A", props.get(2).getName());
        assertEquals("val3", props.get(2).getValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected GenericEvent getEventToTest() throws Exception {
        return new GenericEvent();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
