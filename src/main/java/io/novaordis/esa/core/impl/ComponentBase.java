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
import java.util.List;

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

    private Thread thread;

    // Constructors ----------------------------------------------------------------------------------------------------

    protected ComponentBase(String name) {
        this.name = name;
        this.endOfStreamListeners = new ArrayList<>();
        this.active = false;
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

        thread = new Thread(new ComponentRunnable(), getThreadName());

        thread.start();

        active = true;

        log.debug(this + " started");
    }

    /**
     * @see Component#stop()
     */
    @Override
    public void stop() {
        throw new RuntimeException("stop() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean isActive() {
        return active;
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

    /**
     * Makes sure the component is ready for start: all its dependencies are in place, etc. If the method completes
     * successfully, it means the component is ready for start.
     *
     * @exception IllegalStateException if the component is not ready for start.
     */
    protected abstract void insureReadyForStart() throws IllegalStateException;

    // Private ---------------------------------------------------------------------------------------------------------

    private String getThreadName() {

        return getName() + " Thread";
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

    private class ComponentRunnable implements Runnable {

        // Runnable implementation -------------------------------------------------------------------------------------

        @Override
        public void run() {
            throw new RuntimeException("run() NOT YET IMPLEMENTED");
        }
    }

}
