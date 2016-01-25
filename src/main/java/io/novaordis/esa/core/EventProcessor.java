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

import java.util.concurrent.BlockingQueue;

/**
 * A single-threaded processor of an event stream - it reads events from its input queue, passes them to the pluggable
 * single-threaded logic and write the resulted output events to the output queue.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class EventProcessor extends ComponentBase implements Component {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> inputQueue;

    private ProcessingLogic logic;

    private BlockingQueue<Event> outputQueue;

    // Constructors ----------------------------------------------------------------------------------------------------

    public EventProcessor() {
        this(null);
    }

    public EventProcessor(String name) {
        super(name);
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
                throw new RuntimeException("run() NOT YET IMPLEMENTED");
            }

        };
    }

    /**
     * @see ComponentBase#initiateShutdown()
     */
    @Override
    protected boolean initiateShutdown() {
        throw new RuntimeException("initiateShutdown() NOT YET IMPLEMENTED");
    }

    @Override
    protected void decommission() {
        throw new RuntimeException("decommission() NOT YET IMPLEMENTED");
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------


}
