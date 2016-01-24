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

package io.novaordis.esa.processor;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/23/16
 */
public class SingleThreadedEventProcessorTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(SingleThreadedEventProcessor.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void inputQueueAndInputStreamAreMutuallyExclusive() throws Exception {

        SingleThreadedEventProcessor step = new SingleThreadedEventProcessor();

        step.setInput(new ArrayBlockingQueue<>(1));

        try {
            step.setInput(new ByteArrayInputStream(new byte[1]));
            fail("should have thrown exception, the input stream and the input queue are mutually exclusive");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void inputStreamAndInputQueueAreMutuallyExclusive() throws Exception {

        SingleThreadedEventProcessor step = new SingleThreadedEventProcessor();

        step.setInput(new ByteArrayInputStream(new byte[1]));

        try {
            step.setInput(new ArrayBlockingQueue<>(1));
            fail("should have thrown exception, the input stream and the input queue are mutually exclusive");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void outputQueueAndOutputStreamAreMutuallyExclusive() throws Exception {

        SingleThreadedEventProcessor step = new SingleThreadedEventProcessor();

        step.setOutput(new ArrayBlockingQueue<>(1));

        try {
            step.setOutput(new ByteArrayOutputStream());
            fail("should have thrown exception, the output stream and the output queue are mutually exclusive");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void outputStreamAndOutputQueueAreMutuallyExclusive() throws Exception {

        SingleThreadedEventProcessor step = new SingleThreadedEventProcessor();

        step.setOutput(new ByteArrayOutputStream());

        try {
            step.setOutput(new ArrayBlockingQueue<>(1));
            fail("should have thrown exception, the output stream and the output queue are mutually exclusive");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void redundantStartIsFrownedUpon() throws Exception {

        SingleThreadedEventProcessor step = new SingleThreadedEventProcessor();

        step.start();

        assertTrue(step.isRunning());

        try {
            step.start();
            fail("should have thrown exception, redundant start");
        }
        catch (IllegalStateException e) {
            log.info(e.getMessage());
        }

        assertTrue(step.isRunning());

        step.stop();

        assertFalse(step.isRunning());
    }

    @Test
    public void startDoesNotSucceedBecauseThereIsNoLogic() throws Exception {

        SingleThreadedEventProcessor step = new SingleThreadedEventProcessor();

        assertNull(step.getByteLogic());

        try {
            step.start();
            fail("should have thrown exception, no logic");
        }
        catch (IllegalStateException e) {
            log.info(e.getMessage());
        }
        assertFalse(step.isRunning());
    }

    @Test
    public void startDoesNotSucceedBecauseOfMissingInputAndLogicRequiresInput() throws Exception {

        SingleThreadedEventProcessor step = new SingleThreadedEventProcessor();

        assertNull(step.getInputQueue());
        assertNull(step.getInputStream());

        // the logic requires input
        step.setByteLogic(new MockByteLogic());
        fail("MAKE SURE THE LOGIC REQUIRES INPUT");

        try {
            step.start();
            fail("should have thrown exception, no inputs");
        }
        catch (IllegalStateException e) {
            log.info(e.getMessage());
        }
        assertFalse(step.isRunning());
    }

    @Test
    public void startDoesNotSucceedBecauseOfMissingOutputAndLogicRequiresOutput() throws Exception {

        SingleThreadedEventProcessor step = new SingleThreadedEventProcessor();

        assertNull(step.getOutputQueue());
        assertNull(step.getOutputStream());

        // the logic requires output
        step.setByteLogic(new MockByteLogic());
        fail("MAKE SURE THE LOGIC REQUIRES OUTPUT");

        try {
            step.start();
            fail("should have thrown exception, no outputs");
        }
        catch (IllegalStateException e) {
            log.info(e.getMessage());
        }
        assertFalse(step.isRunning());
    }

    @Test
    public void endOfInputStream() throws Exception {

        fail("RETURN HERE");
    }

    @Test
    public void endOfInputQueue() throws Exception {

        fail("RETURN HERE");
    }

    @Test
    public void threadInterruption() throws Exception {

        fail("Make sure that an interrupted thread does not kill the processor and it is logged as warning");
    }

    @Test
    public void inputStreamFault() throws Exception {

        fail("simulate an InputStream read() fault - what is the best way to deal with the outcome?");
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
