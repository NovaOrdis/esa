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

import io.novaordis.esa.event.OldEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/23/16
 */
public class OldEventProcessor {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(OldEventProcessor.class);
    private static final boolean debug = log.isDebugEnabled();

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String name;

    private ByteOldLogic byteLogic;
    private EventOldLogic eventLogic;

    //
    // the input stream and the input queue are mutually exclusive - we can't have both
    //

    private InputStream inputStream;

    private BlockingQueue<OldEvent> inputQueue;

    //
    // the output stream and the output queue are mutually exclusive - we can't have both
    //

    private OutputStream outputStream;

    private BlockingQueue<OldEvent> outputQueue;

    private Thread thread;

    // Constructors ----------------------------------------------------------------------------------------------------

    public OldEventProcessor() {
        this(null);
    }

    public OldEventProcessor(String name) {
        this.name = name;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setInput(BlockingQueue<OldEvent> inputQueue) {

        this.inputQueue = inputQueue;
    }

    /**
     * @return may return null
     */
    public BlockingQueue<OldEvent> getInputQueue() {
        return inputQueue;
    }

    public void setInput(InputStream inputStream) {

        this.inputStream = inputStream;
    }

    /**
     * @return may return null
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    public void setOutput(BlockingQueue<OldEvent> outputQueue) {

        this.outputQueue = outputQueue;
    }

    /**
     * @return may return null
     */
    public BlockingQueue<OldEvent> getOutputQueue() {
        return outputQueue;
    }

    public void setOutput(OutputStream outputStream) {

        this.outputStream = outputStream;
    }

    /**
     * @return may return null
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setByteLogic(ByteOldLogic logic) {

        this.byteLogic = logic;
    }

    public ByteOldLogic getByteLogic() {
        return byteLogic;
    }

    public void setEventLogic(EventOldLogic logic) {

        this.eventLogic = logic;
    }

    public EventOldLogic getEventLogic() {
        return eventLogic;
    }

    /**
     * @exception IllegalStateException if the processor is not ready yet (missing input when the logic requires input
     * pipes, missing output when the logic requires output pipes) or it was already started.
     */
    public synchronized void start() {

        if (thread != null) {
            throw new IllegalStateException(this + " already started");
        }

        String threadName = name != null ?
                name + " Thread" : "Event Processor " + Integer.toHexString(System.identityHashCode(this)) + " Thread";

        thread = new Thread(new Executor(), threadName);
        thread.start();

        log.debug(this + " started");
    }

    public void stop() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public boolean isRunning() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void waitForEndOfStream() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    @Override
    public String toString() {

        return name == null ? Integer.toHexString(System.identityHashCode(this)) : name;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

    private class Executor implements Runnable {

        public void run() {

            for(;;) {

                try {

                    //
                    // read bytes/event from input, apply logic an write the resulting event to output
                    //

                    if (inputStream != null) {

                        int b;

                        try {
                            b = inputStream.read();
                        }
                        catch (Exception e) {
                            throw new RuntimeException("NOT YET IMPLEMENTED");
                        }

                        List<OldEvent> outputEvents = byteLogic.process(b);

                        if (!outputEvents.isEmpty()) {

                            // attempt to put in queue and block until space becomes available
                            for(OldEvent e: outputEvents) {

                                outputQueue.put(e);

                                if (debug) { log.debug(OldEventProcessor.this + " wrote event to the output queue"); }
                            }
                        }
                    }
                    else
                    {
                        //
                        // we expect events on the input queue
                        //

                        OldEvent event = inputQueue.take();

                        if (debug) { log.debug(OldEventProcessor.this + " read event from the input queue"); }

                        List<OldEvent> outputEvents = eventLogic.process(event);

                        if (!outputEvents.isEmpty()) {

                            // attempt to put in queue and block until space becomes 217
                            for(OldEvent e: outputEvents) {

                                outputQueue.put(e);

                                if (debug) { log.debug(OldEventProcessor.this + " wrote event to the output queue"); }
                            }
                        }
                    }
                }
                catch (InterruptedException e) {

                    //
                    // this is an abnormal condition, we don't interrupt threads, the only way we stop the machinery
                    // is to send an EndOfStreamEvent. Log and start over.
                    //

                    log.warn("a processor thread was interrupted", e);

                    //
                    // from here we go back into the loop
                    //
                }
            }
        }
    }

}
