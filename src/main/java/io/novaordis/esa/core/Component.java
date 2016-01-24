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

import java.util.List;

/**
 * An event pipeline component: an instance that can be used as part of an event pipeline. It could have a name, it can
 * be started and stopped and can have listeners registered on it. Usually starting involves putting internal threads
 * to work.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public interface Component {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    String getName();

    void setName(String name);

    /**
     * Starts the internal thread (or threads) so the components begins to read from its input, process and write to
     * its output.
     *
     * The implementation is idempotent: once started, subsequent start() calls are noops.
     *
     * @exception IllegalStateException if the component is not properly configured: it needs an input queue (or stream)
     * processing or conversion logic and an output queue (or stream).
     */
    void start() throws Exception;

    /**
     * Synchronously stop the component: the method won't exit until the component is stopped and released its
     * resources.
     *
     * The implementation is idempotent: once stopped, subsequent stop() calls are noops.
     */
    void stop();

    /**
     * @return true if the component was started and it is in a state that allows it to process events.
     */
    boolean isActive();

    /**
     * Adds an EndOfStream event listener at the end of the list.
     */
    void addEndOfStreamListener(EndOfStreamListener listener);

    /**
     * Returns the EndOfStream event listener list. Consult with implementation whether the list is the actual
     * underlying storage or a copy.
     */
    List<EndOfStreamListener> getEndOfStreamListeners();

    /**
     * Clears the EndOfStream event listener list
     */
    void clearEndOfStreamListeners();


}
