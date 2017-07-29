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

package io.novaordis.events.core;

import java.util.List;

/**
 * An event pipeline component: an instance that can be used as part of an event pipeline. It could have a name, it can
 * be started and stopped and can have listeners registered on it. All components have at least one internal thread.
 * Some components may have in principle more than one thread, though the current API does not reflect this, getThread()
 * implicitly indicates there is just a single thread.. Starting a component means putting the internal thread to work.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public interface Component {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final long DEFAULT_STOP_TIMEOUT_MS = 3000L;

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    String getName();

    void setName(String name);

    /**
     * Starts the internal thread (or threads) so the components begins to read from its input, process and write to
     * its output.
     *
     * The implementation is synchronous, the component is fully started when the method exits.
     *
     * The implementation is idempotent: once started, subsequent start() calls are noops.
     *
     * @exception IllegalStateException if the component is not properly configured: it needs an input queue (or stream)
     * processing or conversion logic and an output queue (or stream).
     */
    void start() throws Exception;

    /**
     * Attempts to synchronously stop the component: the method won't exit until the component is stopped and it
     * released its resources or a stop timeout occurred - this happens when the component is blocked reading its input
     * stream and no content comes down the pipe - we attempt to close the input stream, but there are cases when
     * closing the input stream does not send end-of-stream to the reader. In this case, those specific implementations
     * should take precautions to put the component into a state that will allow it to quickly exit without any side
     * effects when the read finally unblocks (if ever).
     *
     * The implementation is idempotent: once stopped, subsequent stop() calls are noops.
     *
     * IMPORTANT: once stopped, the component cannot be reused - if you need the functionality again, create a new one.
     *
     * @return true is the component stopped synchronously and gracefully or false if a timeout occurred.
     *
     * @see Component#getStopTimeoutMs()
     *
     * @exception InterruptedException if the thread waiting on this component to stop is externally interrupted.
     */
    boolean stop() throws InterruptedException;

    /**
     * Configure the stop timeout. Default value is DEFAULT_STOP_TIMEOUT_MS.
     *
     * @see Component#getStopTimeoutMs()
     */
    void setStopTimeoutMs(long stopTimeOutMs);

    /**
     * The amount of time in milliseconds to wait for a stop attempt to succeed. If the component could not be stopped
     * within the specified time interval, stop() throws StopTimedOutException, but the implementations are advised to
     * take precautions to put the component in a state that will allow it to quickly exit without any side effects when
     * the blocking I/O operation that prevented the stop to complete finally unblocks (if ever).
     *
     * The default value is Component#DEFAULT_STOP_TIMEOUT_MS.
     *
     * Zero means potentially block forever.
     */
    long getStopTimeoutMs();

    /**
     * @return true if the component was started and it is in a state that allows it to process events. It does
     * not necessarily mean it's currently processing events, which would be the case if no events are coming over the
     * input queue/stream.
     */
    boolean isActive();

    /**
     * @return whether the component was stopped (stop() method was invoked and returned successfully or via timeout)
     *
     * Once stopped, a component cannot be reused anymore. If you need again the functionality, create a new similar
     * component.
     */
    boolean isStopped();

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

    /**
     * @return the component thread. May return null if the component is stopped.
     */
    Thread getThread();


}
