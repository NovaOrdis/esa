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

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

/**
 * Terminates an stream by converting the events received on the input queue into bytes it writes on its output stream.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class OutputStreamTerminator implements Terminator {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String name;

    // Constructors ----------------------------------------------------------------------------------------------------

    public OutputStreamTerminator(String name) {
        this.name = name;
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

    // Consumer implementation -----------------------------------------------------------------------------------------

    @Override
    public BlockingQueue<Event> getInputQueue() {
        throw new RuntimeException("getInputQueue() NOT YET IMPLEMENTED");
    }

    @Override
    public void setInputQueue(BlockingQueue<Event> inputQueue) {
        throw new RuntimeException("setInputQueue() NOT YET IMPLEMENTED");
    }

    @Override
    public void setConversionLogic(ConversionLogic conversionLogic) {
        throw new RuntimeException("setConversionLogic() NOT YET IMPLEMENTED");
    }

    @Override
    public ConversionLogic getConversionLogic() {
        throw new RuntimeException("getConversionLogic() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public OutputStream getOutputStream() {

        throw new RuntimeException("getOutputStream() NOT YET IMPLEMENTED");
    }

    public void setOutputStream(OutputStream outputStream) {

        throw new RuntimeException("setOutputStream() NOT YET IMPLEMENTED");
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
