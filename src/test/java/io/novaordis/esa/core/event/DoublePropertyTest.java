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
public class DoublePropertyTest extends PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DoublePropertyTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void value() throws Exception {

        DoubleProperty fp = new DoubleProperty("test-name", 1.1d);

        assertEquals("test-name", fp.getName());
        assertEquals(1.1d, fp.getValue());
        assertEquals(1.1d, fp.getDouble().doubleValue(), 0.0001);
        assertEquals(Double.class, fp.getType());
    }

    @Test
    public void fromString_InvalidValue() throws Exception {

        DoubleProperty dp = new DoubleProperty("test");

        try {
            dp.fromString("not a float");
            fail("should have thrown Exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void externalizeValue_DoubleProperty() throws Exception {

        DoubleProperty dp = new DoubleProperty("test-name", 1.1d);
        assertEquals("1.1", dp.externalizeValue());
    }

    @Test
    public void externalizeType_DoubleProperty() throws Exception {

        DoubleProperty sp = new DoubleProperty("test-name", 1.1d);
        assertEquals("test-name", sp.externalizeType());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected DoubleProperty getPropertyToTest(String name) {
        return new DoubleProperty(name, 1.1d);
    }

    @Override
    protected Double getAppropriateValueForPropertyToTest() {

        return 1.1d;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
