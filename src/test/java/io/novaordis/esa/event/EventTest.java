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

package io.novaordis.esa.event;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @see Event
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public abstract class EventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(EventTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void emptyEvent() throws Exception {

        Event e = getEventToTest();
        assertNull(e.getTimestamp());
        assertEquals(0, e.getProperties().size());
        assertNull(e.getProperty("I-am-sure-there-is-no-such-property"));
        assertNull(e.getProperty(-1));
        assertNull(e.getProperty(0));
        assertNull(e.getProperty(1));
    }

    @Test
    public void cannotSetAtNegativeIndex() throws Exception {

        Event e = getEventToTest();

        MockProperty mp = new MockProperty("test");

        try {

            e.setProperty(-1, mp);
            fail("should have thrown exception");
        } catch (IllegalArgumentException iae) {
            log.info(iae.getMessage());
        }
    }

    @Test
    public void setAndGetProperties() throws Exception {

        Event e = getEventToTest();
        assertTrue(e.getProperties().isEmpty());

        MockProperty mp = new MockProperty("test");

        assertNull(e.setProperty(0, mp));

        assertEquals(mp, e.getProperty(0));
        assertEquals(mp, e.getProperty("test"));
    }

    @Test
    public void sparseList() throws Exception {

        Event e = getEventToTest();
        assertTrue(e.getProperties().isEmpty());

        MockProperty mp = new MockProperty("test");
        MockProperty mp2 = new MockProperty("test2");

        e.setProperty(0, mp);
        e.setProperty(2, mp2);

        assertEquals(mp, e.getProperty("test"));
        assertEquals(mp, e.getProperty(0));
        assertEquals(mp2, e.getProperty("test2"));
        assertEquals(mp2, e.getProperty(2));

        assertNull(e.getProperty(1));
        assertNull(e.getProperty(3));
    }

    @Test
    public void cannotHaveTwoPropertiesWithTheSameName() throws Exception {

        Event e = getEventToTest();

        MockProperty mp = new MockProperty("test");

        e.setProperty(0, mp);

        try {
            e.setProperty(1, mp);
            fail("should have thrown exception, cannot have two properties with the same name");
        }
        catch(IllegalArgumentException iae) {
            log.info(iae.getMessage());
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract Event getEventToTest() throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
