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
 * Initiator of an event stream - it creates events from where there weren't before and places them on its output queue.
 * Example: an instance that reads lines from a log file and creates an Event instance for each line.
 *
 * The main concern of the initiator is to handle threading and interaction with the queues. The single threaded
 * byte-to-event (or something else) conversion logic is the responsibility of the initiation logic classes:
 *
 * @see InputStreamConversionLogic
 *
 * We exposed an interface instead of settling to the InputStreamInitiator class because we foresee other initiator
 * implementations: implementations that turn messages into events, implementations that turn HTTP requests into
 * events, etc.
 *
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public interface Initiator extends Component {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    BlockingQueue<Event> getOutputQueue();

    /**
     * @return the queue that has just been successfully installed.
     */
    BlockingQueue<Event> setOutputQueue(BlockingQueue<Event> outputQueue);

    void setConversionLogic(ConversionLogic conversionLogic);

    ConversionLogic getConversionLogic();

}
