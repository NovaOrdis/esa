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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class FloatPropertyTest extends PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(FloatPropertyTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void value() throws Exception {

        FloatProperty fp = new FloatProperty("test-name", 1.1f);

        assertEquals("test-name", fp.getName());
        assertEquals(1.1f, fp.getValue());
        assertEquals(1.1f, fp.getFloat().floatValue(), 0.0001);
        assertEquals(Float.class, fp.getType());
    }

    @Test
    public void fromString_InvalidValue() throws Exception {

        FloatProperty fp = new FloatProperty("test");

        try {
            fp.fromString("not a float");
            fail("should have thrown Exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void externalizeValue_FloatProperty() throws Exception {

        FloatProperty fp = new FloatProperty("test-name", 1.1f);
        assertEquals("1.1", fp.externalizeValue());
    }

    @Test
    public void externalizeType_FloatProperty() throws Exception {

        FloatProperty fp = new FloatProperty("test-name", 1.1f);
        assertEquals("test-name", fp.externalizeType());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected FloatProperty getPropertyToTest(String name) {
        return new FloatProperty(name, 1.1f);
    }

    @Override
    protected Float getAppropriateValueForPropertyToTest() {

        return 1.1f;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
