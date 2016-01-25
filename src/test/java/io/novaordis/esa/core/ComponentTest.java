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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public abstract class ComponentTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ComponentTest.class);

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

    // start/stop() ----------------------------------------------------------------------------------------------------

    @Test
    public void startAndStop() throws Exception {

        Component c = getComponentToTest("test");

        assertFalse(c.isActive());

        try {

            c.start();
            fail("should have thrown IllegalArgumentException because the component is not properly configured for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        assertFalse(c.isActive());

        //
        // add an EndOfStreamListener to see if stop() clears them
        //

        c.addEndOfStreamListener(new MockEndOfStreamListener());


        configureForStart(c);

        c.start();

        assertTrue(c.isActive());
        assertEquals(1, c.getEndOfStreamListeners().size());

        //
        // test start idempotence
        //

        c.start();

        assertTrue(c.isActive());

        boolean timedOut = c.stop();

        log.info("this component implementation did " + (timedOut ? "" : "NOT ") + "timed out on stop()");

        //
        // stop() may or may not timeout depending on the underlying implementation (some can be shut down gracefully
        // and some can't) so it does not make sense to test state.
        //

        assertFalse(c.isActive());

        //
        // test stop idempotence
        //

        c.stop();

        assertFalse(c.isActive());

        assertTrue(c.isStopped());

        //
        // normal stop clears resources (listeners, etc.)
        //

        assertTrue(c.getEndOfStreamListeners().isEmpty());
    }

    @Test
    public void setAndSetStopTimeoutMs() throws Exception {

        Component c = getComponentToTest("test");

        assertEquals(Component.DEFAULT_STOP_TIMEOUT_MS, c.getStopTimeoutMs());

        c.setStopTimeoutMs(Component.DEFAULT_STOP_TIMEOUT_MS + 1L);

        assertEquals(Component.DEFAULT_STOP_TIMEOUT_MS + 1L, c.getStopTimeoutMs());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract Component getComponentToTest(String name) throws Exception;

    protected abstract void configureForStart(Component c) throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
