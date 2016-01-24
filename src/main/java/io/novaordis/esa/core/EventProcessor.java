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

    // Constructors ----------------------------------------------------------------------------------------------------

    public EventProcessor() {
        this(null);
    }

    public EventProcessor(String name) {
        super(name);
    }

    // Service implementation ------------------------------------------------------------------------------------------

    @Override
    public void start() throws Exception {
        throw new RuntimeException("start() NOT YET IMPLEMENTED");
    }

    @Override
    public void stop() {
        throw new RuntimeException("stop() NOT YET IMPLEMENTED");
    }

    @Override
    public void addEndOfStreamListener(EndOfStreamListener listener) {
        throw new RuntimeException("addEndOfStreamListener() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public BlockingQueue<Event> getInputQueue() {
        throw new RuntimeException("getInputQueue() NOT YET IMPLEMENTED");
    }

    public void setInputQueue(BlockingQueue<Event> inputQueue) {
        throw new RuntimeException("setInputQueue() NOT YET IMPLEMENTED");
    }

    public BlockingQueue<Event> getOutputQueue() {
        throw new RuntimeException("getOutputQueue() NOT YET IMPLEMENTED");
    }

    public void setOutputQueue(BlockingQueue<Event> outputQueue) {
        throw new RuntimeException("setOutputQueue() NOT YET IMPLEMENTED");
    }

    public void setProcessingLogic(ProcessingLogic processingLogic) {
        throw new RuntimeException("setProcessingLogic() NOT YET IMPLEMENTED");
    }

    public ProcessingLogic getProcessingLogic() {
        throw new RuntimeException("getProcessingLogic() NOT YET IMPLEMENTED");
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------


}
