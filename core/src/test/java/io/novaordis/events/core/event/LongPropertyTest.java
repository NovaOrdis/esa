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
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class LongPropertyTest extends PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(LongPropertyTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void value() throws Exception {

        LongProperty sp = new LongProperty("test-name", 1L);

        assertEquals("test-name", sp.getName());
        assertEquals(1L, sp.getValue());
        assertEquals(1L, sp.getLong().longValue());
        assertEquals(Long.class, sp.getType());
    }

    @Test
    public void fromString_InvalidValue() throws Exception {

        LongProperty lp = new LongProperty("test");

        try {
            lp.fromString("not a long");
            fail("should have thrown Exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void externalizeValue_LongProperty() throws Exception {

        LongProperty lp = new LongProperty("test-name", 1L);
        assertEquals("1", lp.externalizeValue());
    }

    @Test
    public void externalizeType_LongProperty() throws Exception {

        LongProperty lp = new LongProperty("test-name", 1L);
        assertEquals("test-name", lp.externalizeType());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected LongProperty getPropertyToTest(String name) {
        return new LongProperty(name, 1L);
    }

    @Override
    protected Long getAppropriateValueForPropertyToTest() {

        return 1L;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
