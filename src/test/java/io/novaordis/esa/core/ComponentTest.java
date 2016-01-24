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

package io.novaordis.esa.core;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public abstract class ComponentTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void nullName() throws Exception {

        Component c = getComponentToTest(null);

        assertNull(c.getName());

        c.setName("test");

        assertEquals("test", c.getName());
    }

    @Test
    public void name() throws Exception {

        Component c = getComponentToTest("test");

        assertEquals("test", c.getName());
    }

    @Test
    public void endOfStreamListeners() throws Exception {

        Component c = getComponentToTest("test");

        assertTrue(c.getEndOfStreamListeners().isEmpty());

        MockEndOfStreamListener meos = new MockEndOfStreamListener();

        c.addEndOfStreamListener(meos);

        List<EndOfStreamListener> endOfStreamListeners = c.getEndOfStreamListeners();

        assertEquals(1, endOfStreamListeners.size());
        assertEquals(meos, endOfStreamListeners.get(0));

        MockEndOfStreamListener meos2 = new MockEndOfStreamListener();

        c.addEndOfStreamListener(meos2);

        endOfStreamListeners = c.getEndOfStreamListeners();

        assertEquals(2, endOfStreamListeners.size());
        assertEquals(meos, endOfStreamListeners.get(0));
        assertEquals(meos2, endOfStreamListeners.get(1));

        c.clearEndOfStreamListeners();

        endOfStreamListeners = c.getEndOfStreamListeners();
        assertTrue(endOfStreamListeners.isEmpty());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract Component getComponentToTest(String name) throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
