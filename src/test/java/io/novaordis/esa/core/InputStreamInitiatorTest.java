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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class InputStreamInitiatorTest extends InitiatorTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(InputStreamInitiatorTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    @Test
    public void conversionLogic() throws Exception {

        //
        // input stream initiators only accept input stream conversion logic
        //

        Initiator initiator = getComponentToTest("test");

        assertNull(initiator.getConversionLogic());

        MockConversionLogic conversionLogic = new MockConversionLogic();

        try {
            initiator.setConversionLogic(conversionLogic);
            fail("should throw IllegalArgumentException because we're feeding a non-InputStreamConversionLogic");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());

        }

        assertNull(initiator.getConversionLogic());

        InputStreamConversionLogic inputStreamConversionLogic = new MockInputStreamConversionLogic();

        initiator.setConversionLogic(inputStreamConversionLogic);

        assertEquals(inputStreamConversionLogic, initiator.getConversionLogic());
    }

    @Test
    public void toStringWithNoName() {

        InputStreamInitiator initiator = new InputStreamInitiator();

        String s = initiator.toString();

        log.info(s);

        assertTrue(s.matches("InputStreamInitiator\\[.*\\]"));
    }

    @Test
    public void inputStream() throws Exception {

        InputStreamInitiator initiator = getComponentToTest("test");

        assertNull(initiator.getInputStream());

        InputStream is = new ByteArrayInputStream(new byte[0]);

        initiator.setInputStream(is);

        assertEquals(is, initiator.getInputStream());
    }

    @Test
    public void insureReadyForStart() throws Exception {

        InputStreamInitiator initiator = getComponentToTest("test");

        try {
            initiator.insureReadyForStart();
            fail("should throw exception, initiator not ready for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        initiator.setInputStream(new ByteArrayInputStream(new byte[1]));

        try {
            initiator.insureReadyForStart();
            fail("should throw exception, initiator not ready for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        initiator.setConversionLogic(new MockInputStreamConversionLogic());

        try {
            initiator.insureReadyForStart();
            fail("should throw exception, initiator not ready for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        initiator.setOutputQueue(new ArrayBlockingQueue<>(1));

        initiator.insureReadyForStart();

        log.info("ok");
    }

    @Test
    public void closingInputStreamDoesNotReleaseABlockedThread_stopTimesOut() throws Exception {

        InputStreamInitiator inputStreamInitiator = new InputStreamInitiator();

        //
        // set a shorter timeout, we expect stop to timeout so we should exit fast
        //

        long testStopTimeout = 250L;

        inputStreamInitiator.setStopTimeoutMs(testStopTimeout);

        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);

        //
        // A piped input stream can be used to simulate a blocked read, but closing the piped input stream DOES NOT
        // cause the read to end with EOS, so this is a good test case
        //

        inputStreamInitiator.setInputStream(pis);

        inputStreamInitiator.setOutputQueue(new LinkedBlockingQueue<>());
        inputStreamInitiator.setConversionLogic(new MockInputStreamConversionLogic());

        inputStreamInitiator.start();

        //
        // wait for the component thread to block on an empty stream - iterated until its state is
        // Thread.State.TIMED_WAITING
        //

        Thread componentThread = inputStreamInitiator.getThread();

        long componentThreadBlockTimeout = 3001L;

        for(long t0 = System.currentTimeMillis(); !componentThread.getState().equals(Thread.State.TIMED_WAITING); ) {
            if (System.currentTimeMillis() - t0 > componentThreadBlockTimeout) {
                throw new TimeoutException("timed out after " + componentThreadBlockTimeout + " ms");
            }
        }

        assertEquals(Thread.State.TIMED_WAITING, componentThread.getState());

        //
        // The component thread is "blocked" in I/O. Attempt to stop the initiator by closing the thread. W know that
        // for a PipedInputStream close() does not send EOS so the component thread won't unblock
        //

        //
        // we expect to wait at least getStopTimeoutMs() and then stop() should return false.
        //

        long t0 = System.currentTimeMillis();
        boolean noTimeOut = inputStreamInitiator.stop();
        long t1 = System.currentTimeMillis();

        assertFalse(noTimeOut);
        assertTrue(t1 - t0 >= inputStreamInitiator.getStopTimeoutMs());

        //
        // make sure that the initiator was put in a state that makes it quit without doing anything else when
        // the I/O read operation finally unblocks (if ever). Any well written component will exit from everything
        // if stopped.
        //

        assertFalse(inputStreamInitiator.isActive());
        assertTrue(inputStreamInitiator.isStopped());
    }

    @Test
    public void testWhetherClosingTheInputStreamReleasesABlockedReadingThread_ClosingDoesRelease_stopWorks()
            throws Exception {

        fail("Return here");
    }

    @Test
    public void stopBlocksForever() throws Exception {
        fail("Return here");
    }

    @Test
    public void testConversionException()
            throws Exception {

        fail("Return here");
    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected InputStreamInitiator getComponentToTest(String name) {
        return new InputStreamInitiator(name);
    }

    @Override
    protected void configureForStart(Component c) throws Exception {

        if (!(c instanceof InputStreamInitiator)) {
            throw new Exception("not an InputStreamInitiator");
        }

        InputStreamInitiator inputStreamInitiator = (InputStreamInitiator)c;

        inputStreamInitiator.setInputStream(new ByteArrayInputStream(new byte[0]));
        inputStreamInitiator.setConversionLogic(new MockInputStreamConversionLogic());
        inputStreamInitiator.setOutputQueue(new ArrayBlockingQueue<>(1));
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
