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

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Terminates an stream by converting the events received on the input queue into bytes it writes on its output stream.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class OutputStreamTerminator extends ComponentBase implements Terminator {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final int DEFAULT_SHUTDOWN_INITIATION_TIMEOUT_MS = 100;

    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> inputQueue;

    private OutputStreamConversionLogic conversionLogic;

    private OutputStream outputStream;

    // this is another way of saying "stopped" - we have a "stopped" variable in the super class and we don't want
    // those to clash
    private volatile boolean subStopped;

    private volatile boolean disabled;

    // Constructors ----------------------------------------------------------------------------------------------------

    public OutputStreamTerminator() {
        this(null, null, null, null);
    }

    public OutputStreamTerminator(String name) {
        this(name, null, null, null);
    }

    public OutputStreamTerminator(String name, BlockingQueue<Event> inputQueue,
                                  OutputStreamConversionLogic conversionLogic,  OutputStream outputStream) {

        super(name);
        setInputQueue(inputQueue);
        setConversionLogic(conversionLogic);
        setOutputStream(outputStream);
    }

    // Terminator implementation ---------------------------------------------------------------------------------------

    @Override
    public BlockingQueue<Event> getInputQueue() {

        return inputQueue;
    }

    @Override
    public void setInputQueue(BlockingQueue<Event> inputQueue) {

        this.inputQueue = inputQueue;
    }

    /**
     * @exception IllegalArgumentException if the conversion logic being fed is not an OutputStreamConversionLogic
     */
    @Override
    public void setConversionLogic(ConversionLogic conversionLogic) {

        if (conversionLogic != null && !(conversionLogic instanceof OutputStreamConversionLogic)) {

            throw new IllegalArgumentException(this + " only accepts OutputStreamConversionLogic instances");
        }

        this.conversionLogic = (OutputStreamConversionLogic)conversionLogic;
    }

    @Override
    public OutputStreamConversionLogic getConversionLogic() {

        return conversionLogic;
    }

    @Override
    public void disable() {

        disabled = true;
    }

    @Override
    public boolean isDisabled() {

        return disabled;
    }

    /**
     * Handle disable.
     */
    @Override
    public void start() throws Exception {

        if (disabled) {
            stop();
            return;
        }

        super.start();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public OutputStream getOutputStream() {

        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {

        this.outputStream = outputStream;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    protected void insureReadyForStart() throws IllegalStateException {

        if (disabled) {
            //
            // won't ever start
            //
            return;
        }

        //
        // we need the input queue, the conversion logic and the output stream in place
        //

        if (inputQueue == null) {
            throw new IllegalStateException(this + " not properly configured, it is missing its input queue");
        }

        if (conversionLogic == null) {
            throw new IllegalStateException(this + " not properly configured, it is missing its conversion logic");
        }

        if (outputStream == null) {
            throw new IllegalStateException(this + " not properly configured, it is missing its output stream");
        }
    }

    @Override
    protected Runnable getRunnable() {

        //noinspection Convert2Lambda
        return new Runnable() {

            @Override
            public void run() {

                boolean eos = false;

                try {

                    boolean shutdown = false;

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

                            conversionLogic.process(ie);

                            byte[] bytes = conversionLogic.getBytes();

                            if (bytes == null) {
                                // close the output stream
                                outputStream.close();
                            }
                            else {
                                outputStream.write(bytes);
                            }

                            if (eos || shutdown) {

                                log.debug(this + (eos ? " reached the end of stream" : " received a shutdown event") + " and it is now stopping ...");

                                outputStream.close();

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

                            log.error(OutputStreamTerminator.this + " failed and it will irrecoverably shut down", t);

                            //
                            // we let downstream know that no more events will come from us - if we can
                            //
                            try {
                                outputStream.close();
                            }
                            catch(Exception e) {

                                log.error(OutputStreamTerminator.this + " attempted to sent and EndOfStream event but the output queue did not accept it");
                            }

                            //
                            // it's also end of stream
                            //
                            eos = true;

                            //
                            // cleanup
                            //
                            stopSuperclass();
                        }
                    }
                }
                finally {

                    if (eos) {

                        // call EnoOfStreamListeners

                        for(EndOfStreamListener eosl: getEndOfStreamListeners()) {

                            try {

                                log.debug(this + " invoking " + eosl);
                                eosl.eventStreamEnded();
                            }
                            catch(Exception e) {
                                log.error("end of stream listener invocation failed");
                            }
                        }
                    }

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
