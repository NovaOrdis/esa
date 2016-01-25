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

package io.novaordis.esa.core.impl;

import io.novaordis.esa.core.Component;
import io.novaordis.esa.core.EndOfStreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public abstract class ComponentBase implements Component {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ComponentBase.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String name;

    private List<EndOfStreamListener> endOfStreamListeners;

    private volatile boolean active;

    private volatile Thread componentThread;

    private long stopTimeoutMs;

    private volatile CountDownLatch stopLatch;

    private volatile boolean stopped;

    // Constructors ----------------------------------------------------------------------------------------------------

    protected ComponentBase(String name) {

        this.name = name;
        this.endOfStreamListeners = new ArrayList<>();
        this.active = false;
        this.stopped = false;
        this.stopTimeoutMs = DEFAULT_STOP_TIMEOUT_MS;

        // stop only needs to occure once
        this.stopLatch = new CountDownLatch(1);
    }

    // Component implementation ----------------------------------------------------------------------------------------

    /**
     * Can be null.
     */
    @Override
    public String getName() {

        return name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

    @Override
    public void addEndOfStreamListener(EndOfStreamListener listener) {

        endOfStreamListeners.add(listener);
    }

    /**
     * Warning: returns the underlying storage so handle with care.
     */
    @Override
    public List<EndOfStreamListener> getEndOfStreamListeners() {

        if (endOfStreamListeners == null) {
            // we need this because the component may be "cleaned out" during its "render inoperable" phase.
            return Collections.emptyList();
        }

        return endOfStreamListeners;
    }

    @Override
    public void clearEndOfStreamListeners() {

        endOfStreamListeners.clear();
    }

    /**
     * @see Component#start()
     */
    @Override
    public void start() throws Exception {

        if (active) {

            //
            // we're idempotent
            //

            log.debug(this + " already started");
            return;
        }

        insureReadyForStart();

        //
        // we're ready for start, start the thread
        //

        Runnable runnable = getRunnable();
        String threadName = getThreadName();

        componentThread = new Thread(runnable, threadName);

        componentThread.start();

        log.debug(runnable + " installed and " + componentThread + " started");

        active = true;

        log.debug(this + " started");
    }

    /**
     * @see Component#stop()
     */
    @Override
    public boolean stop() throws InterruptedException {

        if (stopped) {

            log.debug(this + " already stopped");
            return true;
        }

        //
        // the component thread is either blocked in I/O, waiting on the input queue or doing processing
        //

        initiateShutdown();

        log.debug(componentThread + " shutdown initiated");

        boolean normalExit = waitForTheComponentThreadToExit();

        //
        // regardless of whether the stop action completed successfully or timed out, render component in an inoperable
        // state
        //

        renderInoperable();

        if (!normalExit) {

            log.warn(this + " did not stop in " + getStopTimeoutMs() + " milliseconds, abandoning it ...");
        }

        return normalExit;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public Thread getThread() {
        return componentThread;
    }

    @Override
    public long getStopTimeoutMs() {

        return this.stopTimeoutMs;
    }

    @Override
    public void setStopTimeoutMs(long ms) {

        this.stopTimeoutMs = ms;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        if (getName() == null) {
            return getClass().getSimpleName() + "[" + Integer.toHexString(System.identityHashCode(this)) + "]";
        }

        return name;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected void releaseTheStopLatch() {

        if (stopLatch == null) {

            return;
        }

        stopLatch.countDown();
    }

    /**
     * This method releases all resources at this level and renders the component inoperable.
     *
     * It is idempotent, so it can be invoked multiple times, from different threads.
     *
     * Also see decommission() which is intended to release the resources at subclass level, and if the component thread
     * is still blocked in I/O and cannot be unblocked, put the component in a state that will allow it to quickly exit
     * without any side effects when the component thread finally unblocks (if ever).
     *
     * @see ComponentBase#decommission()
     */
    protected void renderInoperable() {

        active = false;
        this.stopped = true;

        //
        // give the subclass instance to decommission itself then we clean at this level
        //

        try {

            decommission();
        }
        catch(Throwable t) {

            //
            // log warning but don't prevent the shutdown to complete
            //

            log.warn(this + " decommissioning failed", t);
        }


        if (endOfStreamListeners != null) {
            clearEndOfStreamListeners();
            endOfStreamListeners = null;
        }

        componentThread = null;

        if (stopLatch != null) {
            stopLatch.countDown();
            stopLatch = null;
        }
    }

    /**
     * Makes sure the component is ready for start: all its dependencies are in place, etc. If the method completes
     * successfully, it means the component is ready for start.
     *
     * @exception IllegalStateException if the component is not ready for start.
     */
    protected abstract void insureReadyForStart() throws IllegalStateException;

    /**
     * The subclass will construct a Runnable instance that knows how to deal with its specific input and output
     * channels and its processing logic.
     */
    protected abstract Runnable getRunnable();

    /**
     * Initiates shutdown for the component thread and returns immediately - it does not block.
     *
     * @return true if the shutdown was initiated successfully and there is nothing known at this point indicating
     * that the shutdown won't complete successfully. Return false if the shutdown initiation attempt failed (most
     * likely exception) and it is possible that the shutdown won't complete. This allows us to take a decision on
     * whether to wait for shutdown completion or not.
     */
    protected abstract boolean initiateShutdown();

    /**
     * This method gives the subclass a chance to release all resources at its level. If the component thread is blocked
     * in I/O and cannot be unblocked, the method should put the component in a state that will allow it to quickly exit
     * without any side effects when the component thread finally unblocks (if ever).
     *
     * @see ComponentBase#renderInoperable()
     */

    protected abstract void decommission();

    // Private ---------------------------------------------------------------------------------------------------------

    private String getThreadName() {

        return toString() + " Thread";
    }

    /**
     * Blocks until the underlying component thread had exited.
     *
     * @return <tt>true</tt> if the component thread exited normally. Returned <tt>false</tt> if the calling thread
     * timed out waiting for the component thread to exit.
     */
    private boolean waitForTheComponentThreadToExit() throws InterruptedException {

        long javaUtilConcurrentWaitTime = getStopTimeoutMs();
        // if zero or negative, wait forever
        javaUtilConcurrentWaitTime = javaUtilConcurrentWaitTime <= 0 ? Long.MAX_VALUE : javaUtilConcurrentWaitTime;

        return stopLatch == null || stopLatch.await(javaUtilConcurrentWaitTime, TimeUnit.MILLISECONDS);
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
