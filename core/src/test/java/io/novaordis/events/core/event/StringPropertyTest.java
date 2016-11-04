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

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class StringPropertyTest extends PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(StringPropertyTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void value() throws Exception {

        StringProperty sp = new StringProperty("test-name", "test-value");

        assertEquals("test-name", sp.getName());
        assertEquals("test-value", sp.getValue());
        assertEquals("test-value", sp.getString());
        assertEquals(String.class, sp.getType());

        log.debug(".");
    }

    @Test
    public void externalizeValue_StringProperty() throws Exception {

        StringProperty sp = new StringProperty("test-name", "test-value");
        assertEquals("test-value", sp.externalizeValue());
    }

    @Test
    public void externalizeType_StringProperty() throws Exception {

        StringProperty sp = new StringProperty("test-name", "test-value");
        assertEquals("test-name", sp.externalizeType());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected StringProperty getPropertyToTest(String name) {
        return new StringProperty(name, "test-value");
    }

    @Override
    protected String getAppropriateValueForPropertyToTest() {
        return "test";
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
