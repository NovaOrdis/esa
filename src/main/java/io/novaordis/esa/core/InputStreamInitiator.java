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
import io.novaordis.esa.core.impl.ComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
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
    //private static final boolean debug = log.isDebugEnabled();

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private InputStream inputStream;

    private InputStreamConversionLogic conversionLogic;

    private BlockingQueue<Event> outputQueue;

    private volatile boolean decommissioned;

    // Constructors ----------------------------------------------------------------------------------------------------

    public InputStreamInitiator() {
        this(null);
    }

    public InputStreamInitiator(String name) {
        super(name);
        this.decommissioned = false;
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

        if (!(conversionLogic instanceof InputStreamConversionLogic)) {

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

                    for(;!decommissioned;) {

                        try {

                            int b = inputStream.read();

                            if (decommissioned) {

                                //
                                // if we have been decommissioned after we enter the blocking read, drop everything on
                                // the floor and exit
                                //
                                return;
                            }

                            conversionLogic.process(b);



                        }
                        catch(Throwable t) {

                            //
                            // any exception thrown by the conversion logic will be handled as irrecoverable - we
                            // release the resources, we put the component in a "stopped" state and exit. The
                            // recommended method to deal with recoverable processing faults is to generate specific
                            // fault events.
                            //

                            log.error(InputStreamInitiator.this + " failed and it will irrecoverably shut down", t);

                            //
                            // cleanup
                            //
                            renderInoperable();
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

            inputStream.close();
            return true;
        }
        catch (Exception e) {

            log.error(this + " failed to close the input stream");
            return false;
        }
    }

    /**
     * @see ComponentBase#decommission()
     */
    @Override
    protected void decommission() {

        this.decommissioned = true;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}




