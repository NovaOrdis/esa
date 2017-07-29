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

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.core.impl.ComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Initiates an event stream by converting bytes it reads from its input stream into events that are placed on the
 * output queue.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class InputStreamInitiator extends ComponentBase implements Initiator {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(InputStreamInitiator.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private InputStream inputStream;

    private InputStreamConversionLogic conversionLogic;

    private BlockingQueue<Event> outputQueue;

    // this is another way of saying "stopped" - we have a "stopped" variable in the super class and we don't want
    // those to clash
    private volatile boolean subStopped;

    // Constructors ----------------------------------------------------------------------------------------------------

    public InputStreamInitiator() {
        this(null, null, null, null);
    }

    public InputStreamInitiator(String name) {
        this(name, null, null, null);
    }

    public InputStreamInitiator(
            String name, InputStream inputStream, ConversionLogic conversionLogic, BlockingQueue<Event> outputQueue) {

        super(name);
        this.subStopped = false;
        setInputStream(inputStream);
        setConversionLogic(conversionLogic);
        setOutputQueue(outputQueue);

        log.debug(this + " constructed");
    }

    // Initiator implementation ----------------------------------------------------------------------------------------

    @Override
    public BlockingQueue<Event> getOutputQueue() {
        return outputQueue;
    }

    @Override
    public BlockingQueue<Event> setOutputQueue(BlockingQueue<Event> outputQueue) {

        this.outputQueue = outputQueue;
        return this.outputQueue;
    }

    /**
     * @exception IllegalArgumentException if the conversion logic being fed is not an InputStreamConversionLogic
     */
    @Override
    public void setConversionLogic(ConversionLogic conversionLogic) {

        if (conversionLogic != null && !(conversionLogic instanceof InputStreamConversionLogic)) {

            throw new IllegalArgumentException(this + " only accepts InputStreamConversionLogic instances");
        }

        this.conversionLogic = (InputStreamConversionLogic)conversionLogic;
    }

    @Override
    public InputStreamConversionLogic getConversionLogic() {

        return conversionLogic;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public InputStream getInputStream() {

        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {

        this.inputStream = inputStream;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected void insureReadyForStart() throws IllegalStateException {

        //
        // we need the input stream, the conversion logic and the output queue in place
        //

        if (inputStream == null) {
            throw new IllegalStateException(this + " not properly configured, it is missing its input stream");
        }

        if (conversionLogic == null) {
            throw new IllegalStateException(this + " not properly configured, it is missing its conversion logic");
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
                    boolean conversionLogicIssuedEoSEvent = false;

                    for(; !subStopped; ) {

                        try {

                            int b = inputStream.read();

                            if (subStopped) {

                                //
                                // if we have been decommissioned after we entered the blocking read, drop everything
                                // on the floor and exit
                                //
                                return;
                            }

                            if (b == -1) {

                                log.debug(this + " received End-Of-Stream");
                                eos = true;
                            }

                            conversionLogic.process(b);

                            List<Event> events = conversionLogic.getEvents();

                            for(Event e: events) {

                                outputQueue.put(e);

                                if (e instanceof EndOfStreamEvent) {
                                    conversionLogicIssuedEoSEvent = true;
                                }
                            }

                            if (eos) {

                                log.debug(this + " reached the end of the input stream and it is now stopping ...");

                                if (!conversionLogicIssuedEoSEvent) {
                                    //
                                    // the conversion logic did not issue an EndOfStreamEvent, we do it ourselves
                                    //
                                    outputQueue.put(new EndOfStreamEvent());
                                }

                                //
                                // at this point we voluntarily stop, and since we are not blocked on read and we don't
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
                            // recommended method to deal with recoverable processing faults in the conversion logic
                            // is to generate specific fault events, not to throw exceptions.
                            //

                            log.error(InputStreamInitiator.this + " failed and it will irrecoverably shut down", t);

                            //
                            // we let downstream know that no more events will come from us - if we can
                            //
                            boolean endOfStreamSent = outputQueue.offer(new EndOfStreamEvent());

                            if (!endOfStreamSent) {
                                log.error(InputStreamInitiator.this + " attempted to sent and EndOfStream event but the output queue did not accept it");
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

            @Override
            public String toString() {
                return InputStreamInitiator.this.toString() + "$Runnable";
            }

        };
    }

    /**
     * @see ComponentBase#initiateShutdown()
     */
    @Override
    protected boolean initiateShutdown() {

        try {

            log.debug(this + " closing the input stream " + inputStream);

            inputStream.close();

            log.debug(this + " successfully closed the input stream");

            return true;
        }
        catch (Exception e) {

            log.error(this + " failed to close the input stream");
            return false;
        }
    }

    /**
     * @see ComponentBase#stopSubclass()
     */
    @Override
    protected void stopSubclass() {

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




