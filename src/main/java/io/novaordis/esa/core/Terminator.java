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

import java.util.concurrent.BlockingQueue;

/**
 * Terminator of an event stream - it reads events from its input queue and turns them into something else.
 *
 * The main concern of the terminator is to handle threading and interaction with the queues. The single threaded
 * event-to-byte (or something else) conversion logic is the responsibility of the termination logic classes:
 *
 * @see OutputStreamConversionLogic
 *
 * We exposed an interface instead of settling to the OutputStreamTerminator class because we foresee other terminator
 * implementations: implementations that turn events into messages, implementations that events into HTTP requests,
 * etc.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public interface Terminator extends Component {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    BlockingQueue<Event> getInputQueue();

    void setInputQueue(BlockingQueue<Event> inputQueue);

    void setConversionLogic(ConversionLogic conversionLogic);

    ConversionLogic getConversionLogic();

    /**
     * Takes this terminator out - it stops it (if necessary) and removes it from the pipeline.
     */
    void disable();

    /**
     * @return true if the component was disable (disable()) was invoked), false otherwise.
     */
    boolean isDisabled();

}
