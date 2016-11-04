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

import java.text.Format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public abstract class PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void name() throws Exception {

        Property p = getPropertyToTest("test");
        assertEquals("test", p.getName());
    }

    @Test
    public void fromString() throws Exception {

        Property p = getPropertyToTest("test");
        Object value = getAppropriateValueForPropertyToTest();

        String valueAsString;

        Format format = p.getFormat();

        if (format != null) {
            valueAsString = format.format(value);
        }
        else {
            valueAsString = value.toString();
        }

        Property p2 = p.fromString(valueAsString);

        assertNotEquals(p2, p);

        assertEquals(p.getName(), p2.getName());
        assertEquals(p.getType(), p2.getType());
        assertEquals(p.getMeasureUnit(), p2.getMeasureUnit());

        Object p2Value = p2.getValue();
        assertEquals(value, p2Value);
    }

    // externalizeValue() ----------------------------------------------------------------------------------------------

    @Test
    public void externalizeValue() throws Exception {

        Property p = getPropertyToTest("test");
        p.setValue(null);
        assertNull(p.getValue());
        String s = p.externalizeValue();
        assertNull(s);
    }

    // externalizeType() -----------------------------------------------------------------------------------------------

    @Test
    public void externalizeType() throws Exception {

        Property p = getPropertyToTest("hj46hHT3");
        String s = p.externalizeType();
        assertNotNull(s);
        assertTrue(s.contains("hj46hHT3"));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract Property getPropertyToTest(String name);

    protected abstract Object getAppropriateValueForPropertyToTest();

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
