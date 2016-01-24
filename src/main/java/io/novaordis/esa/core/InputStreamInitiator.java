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

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private InputStream inputStream;

    private InputStreamConversionLogic conversionLogic;

    private BlockingQueue<Event> outputQueue;

    // Constructors ----------------------------------------------------------------------------------------------------

    public InputStreamInitiator() {
        this(null);
    }

    public InputStreamInitiator(String name) {
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

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}




