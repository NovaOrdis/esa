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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
        String valueAsString = value.toString();

        Property p2 = p.fromString(valueAsString);

        assertNotEquals(p2, p);

        assertEquals(value, p2.getValue());
        assertEquals(p.getName(), p2.getName());
        assertEquals(p.getType(), p2.getType());
        assertEquals(p.getMeasureUnit(), p2.getMeasureUnit());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract Property getPropertyToTest(String name);

    protected abstract Object getAppropriateValueForPropertyToTest();

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
