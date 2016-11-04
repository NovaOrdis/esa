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

package io.novaordis.events.core;

import io.novaordis.events.core.event.EndOfStreamEvent;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.FaultEvent;
import io.novaordis.events.core.event.MockEvent;
import io.novaordis.events.core.event.MockProperty;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public abstract class OutputStreamConversionLogicTest extends ConversionLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(InputStreamConversionLogic.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // process() -------------------------------------------------------------------------------------------------------

    @Test
    public void process_EndOfStreamEvent() throws Exception {

        OutputStreamConversionLogic c = getConversionLogicToTest();

        Event event = new EndOfStreamEvent();
        assertTrue(c.process(event));

        // null means we want the enclosing component to close the output stream.
        assertNull(c.getBytes());

        try {

            event = new MockEvent();
            c.process(event);
            fail("should throw exception as the conversion logic is supposed to be closed()");
        }
        catch(ClosedException e) {
            log.info(e.getMessage());
        }

        assertNull(c.getBytes());
    }

    @Test
    public void process_MultipleUncollectedEventsFollowedByEndOfStream() throws Exception {

        OutputStreamConversionLogic c = getConversionLogicToTest();

        Event event = new MockEvent();
        event.setProperty(new MockProperty("mock-property"));
        assertTrue(c.process(event));

        //
        // we do not collect the bytes so we do have uncollected bytes when EndOfStreamEvent arrives
        //

        event = new EndOfStreamEvent();
        assertTrue(c.process(event));

        byte[] bytes = c.getBytes();

        //
        // we don't get null (we can't, we need to push the leftover bytes on the output stream), but that is
        // fine because the owner Terminator knows we got EndOfStreamEvent, so it'll close the stream.
        //

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);


        try {

            event = new MockEvent();
            c.process(event);
            fail("should throw exception as the conversion logic is supposed to be closed()");
        }
        catch(ClosedException e) {
            log.info(e.getMessage());
        }

        assertNull(c.getBytes());
    }

    @Test
    public void process_FaultEvent() throws Exception {

        OutputStreamConversionLogic c = getConversionLogicToTest();

        Event event = new FaultEvent("test message", new RuntimeException("SYNTHETIC"));
        assertTrue(c.process(event));

        byte[] content = c.getBytes();
        assertNotNull(content);
        assertTrue(content.length > 0);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected abstract OutputStreamConversionLogic getConversionLogicToTest() throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
