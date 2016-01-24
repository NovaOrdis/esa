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

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

/**
 * Terminates an stream by converting the events received on the input queue into bytes it writes on its output stream.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class OutputStreamTerminator extends ComponentBase implements Terminator {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> inputQueue;

    private OutputStreamConversionLogic conversionLogic;

    private OutputStream outputStream;

    // Constructors ----------------------------------------------------------------------------------------------------

    public OutputStreamTerminator() {
        this(null);
    }

    public OutputStreamTerminator(String name) {
        super(name);
    }

    // Component implementation ----------------------------------------------------------------------------------------

    @Override
    public void start() throws Exception {
        throw new RuntimeException("start() NOT YET IMPLEMENTED");
    }

    @Override
    public void stop() {
        throw new RuntimeException("stop() NOT YET IMPLEMENTED");
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

        if (!(conversionLogic instanceof OutputStreamConversionLogic)) {

            throw new IllegalArgumentException(this + " only accepts OutputStreamConversionLogic instances");
        }

        this.conversionLogic = (OutputStreamConversionLogic)conversionLogic;
    }

    @Override
    public OutputStreamConversionLogic getConversionLogic() {

        return conversionLogic;
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

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
