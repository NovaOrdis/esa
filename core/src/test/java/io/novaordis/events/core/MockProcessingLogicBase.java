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

import io.novaordis.events.api.event.Event;
import io.novaordis.events.core.event.MockEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/7/16
 */
public class MockProcessingLogicBase extends ProcessingLogicBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean broken;
    private int rate;
    private int counter;

    private List<Event> contributors;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockProcessingLogicBase() {
        this.rate = 1;
        this.counter = 0;
        this.contributors = new ArrayList<>();
    }

    // ProcessingLogicBase overrides -----------------------------------------------------------------------------------

    @Override
    protected Event processInternal(Event e) throws Exception {

        if (broken) {
            throw new RuntimeException("SYNTHETIC");
        }

        contributors.add(e);

        counter = (counter + 1) % rate;

        if (counter == 0) {

            MockEvent outputEvent = new MockEvent();
            outputEvent.setPayload(new ArrayList<>(contributors));
            contributors.clear();
            return outputEvent;
        }
        else {
            return null;
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setBroken(boolean b) {
        this.broken = b;
    }

    /**
     * Configurable output event generation rate - this processing logic only generates an output event for r input
     * events.
     */
    public void setRate(int rate) {

        this.rate = rate;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
