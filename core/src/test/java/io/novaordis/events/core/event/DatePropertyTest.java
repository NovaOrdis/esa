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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class DatePropertyTest extends PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DatePropertyTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void value() throws Exception {

        DateProperty dp = new DateProperty("test-name", new Date(1L));

        assertEquals("test-name", dp.getName());
        assertEquals(new Date(1L), dp.getValue());
        assertEquals(1L, dp.getDate().getTime());
        assertEquals(Date.class, dp.getType());
    }

    @Test
    public void fromString_InvalidValue() throws Exception {

        DateProperty dp = new DateProperty("test");

        try {
            dp.fromString("not a date");
            fail("should have thrown Exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void externalizeValue_DateProperty() throws Exception {

        long value = 1010101L;
        DateProperty dp = new DateProperty("test-name", new Date(value));

        String expected = DateProperty.DEFAULT_DATE_FORMAT.format(value);
        String externalized = dp.externalizeValue();

        assertEquals(expected, externalized);
    }

    @Test
    public void externalizeType_DateProperty() throws Exception {

        long value = 1010101L;
        DateProperty dp = new DateProperty("test-name", new Date(value));

        assertEquals("test-name", dp.externalizeType());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected DateProperty getPropertyToTest(String name) {
        return new DateProperty(name, new Date(100000L));
    }

    @Override
    protected Date getAppropriateValueForPropertyToTest() {

        return new Date(100000L);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
