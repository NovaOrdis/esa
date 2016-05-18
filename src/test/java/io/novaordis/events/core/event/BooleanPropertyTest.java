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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class BooleanPropertyTest extends PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BooleanPropertyTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void value() throws Exception {

        BooleanProperty bp = new BooleanProperty("test-name", true);

        assertEquals("test-name", bp.getName());
        assertTrue((Boolean)bp.getValue());
        assertTrue(bp.getBoolean());
        assertEquals(Boolean.class, bp.getType());
    }

    @Test
    public void fromString_InvalidValue() throws Exception {

        BooleanProperty bp = new BooleanProperty("test");

        try {
            bp.fromString("not a boolean");
            fail("should have thrown Exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void externalizeValue_BooleanProperty() throws Exception {

        BooleanProperty bp = new BooleanProperty("test-name", true);
        assertEquals("true", bp.externalizeValue());
    }

    @Test
    public void externalizeType_BooleanProperty() throws Exception {

        BooleanProperty bp = new BooleanProperty("test-name", true);
        assertEquals("test-name", bp.externalizeType());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected BooleanProperty getPropertyToTest(String name) {
        return new BooleanProperty(name, true);
    }

    @Override
    protected Boolean getAppropriateValueForPropertyToTest() {

        return true;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
