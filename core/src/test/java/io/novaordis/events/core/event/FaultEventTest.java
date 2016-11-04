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

import io.novaordis.events.extensions.bscenarios.BusinessScenarioException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class FaultEventTest extends EventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // constructors ----------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        FaultEvent e = new FaultEvent();
        assertNull(e.getMessage());
        assertNull(e.getCause());

        String s = e.toString();
        assertTrue(s.matches("FAULT \\(UNTYPED\\)\\[.+\\]"));
    }

    @Test
    public void constructor2() throws Exception {

        FaultEvent e = new FaultEvent("something");
        assertEquals("something", e.getMessage());
        assertNull(e.getCause());

        String s = e.toString();
        assertEquals("FAULT (UNTYPED): something", s);
    }

    @Test
    public void constructor3() throws Exception {

        Exception cause = new Exception("some message");
        FaultEvent e = new FaultEvent(cause);
        assertNull(e.getMessage());
        assertEquals(cause, e.getCause());

        String s = e.toString();
        assertEquals("FAULT (UNTYPED): Exception: some message", s);
    }

    @Test
    public void constructor4() throws Exception {

        Exception cause = new Exception("some message");
        FaultEvent e = new FaultEvent("something", cause);
        assertEquals("something", e.getMessage());
        assertEquals(cause, e.getCause());

        String s = e.toString();
        assertEquals("FAULT (UNTYPED): something, Exception: some message", s);
    }

    // getLineNumber() -------------------------------------------------------------------------------------------------

    @Test
    public void getLineNumber_Null() throws Exception {

        FaultEvent e = new FaultEvent();
        assertNull(e.getLineNumber());
    }

    @Test
    public void getLineNumber_FromBusinessScenarioException() throws Exception {

        long lineNumber = 7L;
        FaultEvent e = new FaultEvent(new BusinessScenarioException(lineNumber, "something"));
        assertEquals(7L, e.getLineNumber().longValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected FaultEvent getEventToTest() throws Exception {
        return new FaultEvent();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
