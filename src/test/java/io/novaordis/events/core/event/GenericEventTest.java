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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void setStringProperty() throws Exception {

        GenericEvent ge = getEventToTest();

        assertNull(ge.getStringProperty("test-property"));

        ge.setStringProperty("test-property", "value1");
        assertEquals("value1", ge.getStringProperty("test-property").getValue());

        List<Property> pl = ge.getPropertyList();
        assertEquals(1, pl.size());
        StringProperty sp = (StringProperty)pl.get(0);
        assertEquals("test-property", sp.getName());
        assertEquals("value1", sp.getValue());

        ge.setStringProperty("test-property", "value2");
        assertEquals("value2", ge.getStringProperty("test-property").getValue());

        List<Property> pl2 = ge.getPropertyList();
        assertEquals(1, pl2.size());
        StringProperty sp2 = (StringProperty)pl2.get(0);
        assertEquals("test-property", sp2.getName());
        assertEquals("value2", sp2.getValue());
    }

    @Test
    public void setLongProperty() throws Exception {

        GenericEvent ge = getEventToTest();

        assertNull(ge.getLongProperty("test-property"));

        ge.setLongProperty("test-property", 7L);
        assertEquals(7L, ge.getLongProperty("test-property").getValue());

        List<Property> pl = ge.getPropertyList();
        assertEquals(1, pl.size());
        LongProperty p = (LongProperty)pl.get(0);
        assertEquals("test-property", p.getName());
        assertEquals(7L, p.getValue());

        ge.setLongProperty("test-property", 8L);
        assertEquals(8L, ge.getLongProperty("test-property").getValue());

        List<Property> pl2 = ge.getPropertyList();
        assertEquals(1, pl2.size());
        LongProperty p2 = (LongProperty)pl2.get(0);
        assertEquals("test-property", p2.getName());
        assertEquals(8L, p2.getValue());
    }

    // removeStringProperty() ------------------------------------------------------------------------------------------

    @Test
    public void removeStringProperty() throws Exception {

        GenericEvent ge = getEventToTest();

        ge.setStringProperty("test-name", "test-value");
        assertEquals("test-value", ge.getStringProperty("test-name").getString());

        ge.removeStringProperty("test-name");
        assertNull(ge.getStringProperty("test-name"));

        ge.removeStringProperty("test-name");
    }

    @Test
    public void setStringProperty_Null() throws Exception {

        GenericEvent ge = getEventToTest();

        ge.setStringProperty("test-name", "test-value");
        assertEquals("test-value", ge.getStringProperty("test-name").getString());

        ge.setStringProperty("test-name", null);
        assertNull(ge.getStringProperty("test-name"));

        ge.setStringProperty("test-name", null);
    }

    // removeLongProperty() --------------------------------------------------------------------------------------------

    @Test
    public void removeLongProperty() throws Exception {

        GenericEvent ge = getEventToTest();

        ge.setLongProperty("test-name", 1L);
        assertEquals(1L, ge.getLongProperty("test-name").getLong().longValue());

        ge.removeLongProperty("test-name");
        assertNull(ge.getLongProperty("test-name"));

        ge.removeLongProperty("test-name");
    }

    // removeIntegerProperty() ------------------------------------------------------------------------------------------

    @Test
    public void removeIntegerProperty() throws Exception {

        GenericEvent ge = getEventToTest();

        ge.setIntegerProperty("test-name", 1);
        assertEquals(1, ge.getIntegerProperty("test-name").getInteger().intValue());

        ge.removeIntegerProperty("test-name");
        assertNull(ge.getIntegerProperty("test-name"));

        ge.removeIntegerProperty("test-name");
    }

    // removeBooleanProperty() ------------------------------------------------------------------------------------------

    @Test
    public void removeBooleanProperty() throws Exception {

        GenericEvent ge = getEventToTest();

        ge.setBooleanProperty("test-name", true);
        assertTrue(ge.getBooleanProperty("test-name").getBoolean());

        ge.removeBooleanProperty("test-name");
        assertNull(ge.getBooleanProperty("test-name"));

        ge.removeBooleanProperty("test-name");
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
