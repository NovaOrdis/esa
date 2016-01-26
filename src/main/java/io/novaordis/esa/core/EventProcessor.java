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

import io.novaordis.esa.core.event.EndOfStreamEvent;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.ShutdownEvent;
import io.novaordis.esa.core.impl.ComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A single-threaded processor of an event stream - it reads events from its input queue, passes them to the pluggable
 * single-threaded logic and write the resulted output events to the output queue.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class EventProcessor extends ComponentBase implements Component {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final int DEFAULT_SHUTDOWN_INITIATION_TIMEOUT_MS = 100;

    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> inputQueue;

    private ProcessingLogic logic;

    private BlockingQueue<Event> outputQueue;

    // this is another way of saying "stopped" - we have a "stopped" variable in the super class and we don't want
    // those to clash
    private volatile boolean subStopped;

    // Constructors ----------------------------------------------------------------------------------------------------

    public EventProcessor() {
        this(null, null, null, null);
    }

    public EventProcessor(String name) {
        this(name, null, null, null);
    }

    public EventProcessor(String name, BlockingQueue<Event> inputQueue,
                          ProcessingLogic processingLogic, BlockingQueue<Event> outputQueue) {

        super(name);
        this.inputQueue = inputQueue;
        this.logic = processingLogic;
        this.outputQueue = outputQueue;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public BlockingQueue<Event> getInputQueue() {

        return inputQueue;
    }

    public void setInputQueue(BlockingQueue<Event> inputQueue) {

        this.inputQueue = inputQueue;
    }

    public BlockingQueue<Event> getOutputQueue() {

        return outputQueue;
    }

    public void setOutputQueue(BlockingQueue<Event> outputQueue) {

        this.outputQueue = outputQueue;
    }

    public void setProcessingLogic(ProcessingLogic logic) {

        this.logic = logic;
    }

    public ProcessingLogic getProcessingLogic() {

        return logic;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    protected void insureReadyForStart() throws IllegalStateException {

        //
        // we need the input queue, the processing logic and the output queue in place
        //

        if (inputQueue == null) {
            throw new IllegalStateException(this + " not properly configured, it is missing its input queue");
        }

        if (logic == null) {
            throw new IllegalStateException(this + " not properly configured, it is missing its processing logic");
        }

        if (outputQueue == null) {
            throw new IllegalStateException(this + " not properly configured, it is missing its output queue");
        }
    }

    @Override
    protected Runnable getRunnable() {

        //noinspection Convert2Lambda
        return new Runnable() {

            @Override
            public void run() {

                try {

                    boolean eos = false;
                    boolean shutdown = false;
                    boolean processingLogicIssuedEoSEvent = false;

                    for(; !subStopped; ) {

                        try {

                            Event ie = inputQueue.take();

                            if (subStopped) {

                                //
                                // if we have been decommissioned after we entered the blocking read, drop everything
                                // on the floor and exit
                                //
                                return;
                            }

                            if (ie instanceof EndOfStreamEvent) {

                                log.debug(this + " received EndOfStream event");
                                eos = true;
                            }

                            if (ie instanceof ShutdownEvent) {

                                log.debug(this + " received Shutdown event");
                                shutdown = true;
                            }


                            logic.process(ie);

                            List<Event> events = logic.getEvents();

                            for(Event oe : events) {

                                outputQueue.put(oe);

                                if (oe instanceof EndOfStreamEvent) {
                                    processingLogicIssuedEoSEvent = true;
                                }
                            }

                            if (eos || shutdown) {

                                log.debug(this + (eos ? " reached the end of stream" : " received a shutdown event") + " and it is now stopping ...");

                                if (!processingLogicIssuedEoSEvent) {
                                    //
                                    // the processing logic did not issue an EndOfStreamEvent, we do it ourselves
                                    //
                                    outputQueue.put(new EndOfStreamEvent());
                                }

                                //
                                // at this point we voluntarily stop, and since we are not blocked in take()and we don't
                                // care what comes on the input stream, there's no point in waiting on the stop latch
                                // after attempting to stop - release it in advance, since the finally block, where we
                                // normally release the latch, will execute only after stop() invocation
                                releaseTheStopLatch();
                                stop();
                                break;
                            }
                        }
                        catch(Throwable t) {

                            //
                            // any exception thrown by the conversion logic will be handled as irrecoverable - we
                            // release the resources, we put the component in a stopped state and exit. The
                            // recommended method to deal with recoverable processing faults in the processing logic
                            // is to generate specific fault events, not to throw exceptions.
                            //

                            log.error(EventProcessor.this + " failed and it will irrecoverably shut down", t);

                            //
                            // we let downstream know that no more events will come from us - if we can
                            //
                            boolean endOfStreamSent = outputQueue.offer(new EndOfStreamEvent());

                            if (!endOfStreamSent) {
                                log.error(EventProcessor.this + " attempted to sent and EndOfStream event but the output queue did not accept it");
                            }

                            //
                            // cleanup
                            //
                            stopSuperclass();
                        }
                    }
                }
                finally {

                    //
                    // no matter how we exit the processing loop, release the stop latch
                    //
                    releaseTheStopLatch();
                }
            }

        };
    }

    /**
     * @see ComponentBase#initiateShutdown()
     */
    @Override
    protected boolean initiateShutdown() {

        try {

            //
            // note that we inject a shutdown even in the input queue just in case the queue is empty and
            // the component thread is blocked reading the queue. If the queue contains a large number of events
            // waiting in line to be processing, injecting Shutdown won't help too much, but we also flag the thread
            // to exit, so we will be fine
            //
            log.debug(this + " injecting Shutdown event into the input queue " + inputQueue);

            boolean success = inputQueue.
                    offer(new ShutdownEvent(), DEFAULT_SHUTDOWN_INITIATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            if (success) {
                log.debug(this + " successfully injected Shutdown event into the input queue");
            }
            else {
                log.debug(this + " timed out while attempting to inject Shutdown event into the input queue");
            }

            return success;
        }
        catch (Exception e) {

            log.error(this + " failed to close the input stream");
            return false;
        }
    }

    @Override
    protected void stopSubclass() {

        log.debug(this + " clearing state");

        //
        // this puts the component in an unoperable state even if the read unblocks after shutdown
        //

        this.subStopped = true;

        //
        // do not nullify the input stream, conversion logic and the output queue, external clients may still need those
        // references even after the component was stopped
        //
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------


}
