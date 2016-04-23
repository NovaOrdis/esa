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
import io.novaordis.events.core.event.MockEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    public void constructor() throws Exception {

        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ConversionLogic cl = new MockInputStreamConversionLogic();
        BlockingQueue<Event> outputQueue = new ArrayBlockingQueue<>(1);

        InputStreamInitiator inputStreamInitiator = new InputStreamInitiator("test", bais, cl, outputQueue);

        assertEquals("test", inputStreamInitiator.getName());
        assertEquals(bais, inputStreamInitiator.getInputStream());
        assertEquals(cl, inputStreamInitiator.getConversionLogic());
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
    public void closingInputStreamDoesNotReleaseBlockedThread_stopTimesOut() throws Exception {

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
    public void closingInputStreamDoesReleaseBlockedThread_stopExitsGracefully() throws Exception {

        InputStreamInitiator inputStreamInitiator = new InputStreamInitiator();

        //
        // set a longer timeout, we expect stop to not timeout so it does not matter
        //

        inputStreamInitiator.setStopTimeoutMs(5000L);

        MockInputStreamThatSendsEndOfStreamOnClose mis = new MockInputStreamThatSendsEndOfStreamOnClose();

        inputStreamInitiator.setInputStream(mis);
        MockInputStreamConversionLogic conversionLogic = new MockInputStreamConversionLogic();
        inputStreamInitiator.setConversionLogic(conversionLogic);
        inputStreamInitiator.setOutputQueue(new LinkedBlockingQueue<>());

        inputStreamInitiator.start();

        mis.getEnteringReadLatch().await();

        //
        // at this point the component thread is blocked in read()
        //

        Thread.sleep(10);

        log.info("closing the component which in turn will close the stream ...");

        boolean graceful = inputStreamInitiator.stop();

        assertTrue(graceful);

        //
        // make sure InputStream.close() was invoked
        //
        assertTrue(mis.wasCloseInvoked());

        //
        // wait until the end-of-stream propagates to the conversion logic
        //

        long conversionLogicTimeout = 5000L;

        boolean didNotTimeOut = conversionLogic.waitForEndOfStreamMs(conversionLogicTimeout);

        if (didNotTimeOut) {
            log.info("end of stream reached conversion logic");
        }
        else {
            fail("we timed out (" + conversionLogicTimeout + " ms) waiting for the end of stream to reach the conversion logic");
        }

        //
        // wait until we get an EndOfStream event on the output queue
        //

        BlockingQueue<Event> outputQueue = inputStreamInitiator.getOutputQueue();

        long outputQueueTimeout = 5000L;

        for(;;) {

            Event e = outputQueue.poll(outputQueueTimeout, TimeUnit.MILLISECONDS);

            if (e == null) {

                fail("no event arrived on the queue for more than " + outputQueueTimeout + " ms");
            }

            if (e instanceof EndOfStreamEvent) {
                //
                // we're good
                //
                log.info("EndOfStreamEvent received");
                break;
            }
            else {
                //
                // discard and return to polling
                //
                log.debug("" + e);
            }
        }


        log.info("all EOS notifications and events received");

        //
        // make sure the component is in the correct "stopped" state
        //

        assertFalse(inputStreamInitiator.isActive());
        assertTrue(inputStreamInitiator.isStopped());
    }

    @Test
    public void conversionLogicExceptionHandling() throws Exception {

        //
        // we install a conversion logic that wraps each byte into MockEvent unless it gets 'x', in which case it
        // throws a synthetic runtime exception. We send 'a' and then 'x' and we should get just one event and a
        // closed component
        //

        final AtomicBoolean closeMethodInvoked = new AtomicBoolean(false);

        InputStream is = new InputStream() {

            private byte[] bytes = new byte[] { 'a', 'x' };
            private int cursor = 0;
            private boolean closed;

            @Override
            public int read() throws IOException {

                if (closed) {
                    throw new IOException("this stream is closed");
                }

                if (cursor == bytes.length) {
                    closed = true;
                    return -1;
                }

                return bytes[cursor ++];
            }

            @Override
            public void close() {
                closeMethodInvoked.set(true);
            }
        };

        assertFalse(closeMethodInvoked.get());

        InputStreamInitiator inputStreamInitiator = getComponentToTest("test");
        inputStreamInitiator.setInputStream(is);
        inputStreamInitiator.setConversionLogic(new InputStreamConversionLogic() {

            private Event e;

            @Override
            public boolean process(int b) {

                if (b == 'a') {

                    e = new MockEvent((char)b);
                    return true;
                }
                else {
                    throw new RuntimeException("SYNTHETIC");
                }
            }

            @Override
            public List<Event> getEvents() {

                if (e == null) {
                    return Collections.emptyList();
                }
                else {
                    Event e2 = e;
                    e = null;
                    return Collections.singletonList(e2);
                }
            }

            @Override
            public boolean isClosed() {
                throw new RuntimeException("isClosed() NOT YET IMPLEMENTED");
            }
        });
        inputStreamInitiator.setOutputQueue(new LinkedBlockingQueue<>());

        inputStreamInitiator.start();

        //
        // we just busy poll for the component to stop
        //
        for(long timeout = 1000L, t0 = System.currentTimeMillis();;) {
            if (inputStreamInitiator.isStopped()) { break; }
            Thread.sleep(10);
            if (System.currentTimeMillis() - t0 > timeout) { fail("polled more than " + timeout + " ms"); }
        }

        assertTrue(inputStreamInitiator.isStopped());

        BlockingQueue<Event> oq = inputStreamInitiator.getOutputQueue();

        assertTrue(oq.size() == 2);

        MockEvent me = (MockEvent)oq.take();
        assertEquals('a', me.getPayload());

        EndOfStreamEvent eose = (EndOfStreamEvent)oq.take();
        assertNotNull(eose);
    }

    @Test
    public void conversionLogicPlacesAnEndOfStreamEventUponReceivingTheEndOfStream() throws Exception {

        InputStreamInitiator isi = new InputStreamInitiator("test");
        isi.setInputStream(new ByteArrayInputStream(new byte[] { 'a' }));
        isi.setConversionLogic(new InputStreamConversionLogic() {
            List<Event> events = new ArrayList<>();
            private boolean closed;
            @Override
            public boolean process(int b) {

                if (closed) {
                    throw new IllegalStateException("we are closed");
                }

                if (b == -1) {
                    //
                    // we DO emit an EndOfStreamEvent
                    //
                    events.add(new EndOfStreamEvent());
                    closed = true;
                }
                else {
                    events.add(new MockEvent());
                }

                return true;
            }

            @Override
            public List<Event> getEvents() {
                List<Event> result = new ArrayList<Event>(events);
                events.clear();
                return result;
            }

            @Override
            public boolean isClosed() {
                throw new RuntimeException("isClosed() NOT YET IMPLEMENTED");
            }
        });
        isi.setOutputQueue(new LinkedBlockingQueue<>());

        isi.start();

        //
        // we just busy poll for the component to stop
        //
        for(long timeout = 1000L, t0 = System.currentTimeMillis();;) {
            if (isi.isStopped()) { break; }
            Thread.sleep(10);
            if (System.currentTimeMillis() - t0 > timeout) { fail("polled more than " + timeout + " ms"); }
        }

        assertTrue(isi.isStopped());
        assertEquals(2, isi.getOutputQueue().size());
        MockEvent e = (MockEvent)isi.getOutputQueue().take();
        assertNotNull(e);
        EndOfStreamEvent e2 = (EndOfStreamEvent)isi.getOutputQueue().take();
        assertNotNull(e2);
    }

    @Test
    public void conversionLogicDoesNotPlaceAnEndOfStreamEventUponReceivingTheEndOfStream() throws Exception {

        InputStreamInitiator isi = new InputStreamInitiator("test");
        isi.setInputStream(new ByteArrayInputStream(new byte[] { 'a' }));
        isi.setConversionLogic(new InputStreamConversionLogic() {
            List<Event> events = new ArrayList<>();
            private boolean closed;
            @Override
            public boolean process(int b) {

                if (closed) {
                    throw new IllegalStateException("we are closed");
                }

                if (b == -1) {
                    //
                    // we DO NOT (!) emit an EndOfStreamEvent
                    //
                    closed = true;
                    return false;
                }
                else {
                    events.add(new MockEvent());
                }

                return true;
            }

            @Override
            public List<Event> getEvents() {
                List<Event> result = new ArrayList<Event>(events);
                events.clear();
                return result;
            }

            @Override
            public boolean isClosed() {
                throw new RuntimeException("isClosed() NOT YET IMPLEMENTED");
            }
        });
        isi.setOutputQueue(new LinkedBlockingQueue<>());

        isi.start();

        //
        // we just busy poll for the component to stop
        //
        for(long timeout = 1000L, t0 = System.currentTimeMillis();;) {
            if (isi.isStopped()) { break; }
            Thread.sleep(10);
            if (System.currentTimeMillis() - t0 > timeout) { fail("polled more than " + timeout + " ms"); }
        }

        assertTrue(isi.isStopped());
        assertEquals(2, isi.getOutputQueue().size());
        MockEvent e = (MockEvent)isi.getOutputQueue().take();
        assertNotNull(e);
        EndOfStreamEvent e2 = (EndOfStreamEvent)isi.getOutputQueue().take();
        assertNotNull(e2);
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

        //
        // we provide an input stream that blocks forever to avoid start() tests race conditions
        //

        InputStream blockingInputStream = new PipedInputStream(new PipedOutputStream());
        inputStreamInitiator.setInputStream(blockingInputStream);
        inputStreamInitiator.setConversionLogic(new MockInputStreamConversionLogic());
        inputStreamInitiator.setOutputQueue(new ArrayBlockingQueue<>(1));
    }

    @Override
    protected boolean willTimeoutOnStop() {

        return true;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
