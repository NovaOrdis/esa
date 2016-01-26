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

import io.novaordis.esa.core.event.Event;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class OutputStreamTerminatorTest extends TerminatorTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(OutputStreamTerminatorTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        BlockingQueue<Event> inputQueue = new ArrayBlockingQueue<>(1);
        OutputStreamConversionLogic mosc = new MockOutputStreamConversionLogic();
        OutputStream outputStream = new ByteArrayOutputStream();

        OutputStreamTerminator terminator = new OutputStreamTerminator("test", inputQueue, mosc, outputStream);

        assertEquals("test", terminator.getName());
        assertEquals(inputQueue, terminator.getInputQueue());
        assertEquals(mosc, terminator.getConversionLogic());
        assertEquals(outputStream, terminator.getOutputStream());
    }

    @Override
    @Test
    public void conversionLogic() throws Exception {

        //
        // output stream terminators only accept output stream conversion logic
        //

        Terminator terminator = getComponentToTest("test");

        assertNull(terminator.getConversionLogic());

        MockConversionLogic conversionLogic = new MockConversionLogic();

        try {
            terminator.setConversionLogic(conversionLogic);
            fail("should throw IllegalArgumentException because we're feeding a non-OutputStreamConversionLogic");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }

        assertNull(terminator.getConversionLogic());

        OutputStreamConversionLogic outputStreamConversionLogic = new MockOutputStreamConversionLogic();

        terminator.setConversionLogic(outputStreamConversionLogic);

        assertEquals(outputStreamConversionLogic, terminator.getConversionLogic());
    }

    @Test
    public void toStringWithNoName() {

        OutputStreamTerminator terminator = new OutputStreamTerminator();

        String s = terminator.toString();

        log.info(s);

        assertTrue(s.matches("OutputStreamTerminator\\[.*\\]"));
    }

    @Test
    public void outputStream() throws Exception {

        OutputStreamTerminator terminator = getComponentToTest("test");

        assertNull(terminator.getOutputStream());

        OutputStream os = new ByteArrayOutputStream();

        terminator.setOutputStream(os);

        assertEquals(os, terminator.getOutputStream());
    }

    @Test
    public void insureReadyForStart() throws Exception {

        OutputStreamTerminator terminator = getComponentToTest("test");

        try {
            terminator.insureReadyForStart();
            fail("should throw exception, terminator not ready for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        terminator.setInputQueue(new ArrayBlockingQueue<>(1));

        try {
            terminator.insureReadyForStart();
            fail("should throw exception, terminator not ready for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        terminator.setConversionLogic(new MockOutputStreamConversionLogic());

        try {
            terminator.insureReadyForStart();
            fail("should throw exception, terminator not ready for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        terminator.setOutputStream(new ByteArrayOutputStream());

        terminator.insureReadyForStart();

        log.info("ok");
    }

    // Package protected -----------------------------------------------------------------------------------------------

    @Override
    protected OutputStreamTerminator getComponentToTest(String name) throws Exception {
        return new OutputStreamTerminator(name);
    }

    @Override
    protected void configureForStart(Component c) throws Exception {

        if (!(c instanceof OutputStreamTerminator)) {
            throw new Exception("not an OutputStreamTerminator");
        }

        OutputStreamTerminator outputStreamTerminator = (OutputStreamTerminator)c;

        outputStreamTerminator.setInputQueue(new ArrayBlockingQueue<>(1));
        outputStreamTerminator.setConversionLogic(new MockOutputStreamConversionLogic());
        outputStreamTerminator.setOutputStream(new ByteArrayOutputStream());
    }

    @Override
    protected boolean willTimeoutOnStop() {
        return false;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
