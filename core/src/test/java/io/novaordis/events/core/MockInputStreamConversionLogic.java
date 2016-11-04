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

import io.novaordis.events.core.event.EndOfStreamEvent;
import io.novaordis.events.core.event.Event;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class MockInputStreamConversionLogic extends MockConversionLogic implements InputStreamConversionLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private CountDownLatch endOfStreamLatch;

    private EndOfStreamEvent endOfStreamEvent;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockInputStreamConversionLogic() {

        endOfStreamLatch = new CountDownLatch(1);
    }

    // InputStreamConversionLogic implementation -----------------------------------------------------------------------

    /**
     * Simplified implementation: we ignore everything except for the end-of-stream.
     */
    @Override
    public boolean process(int b) {

        if (b == -1) {

            endOfStreamLatch.countDown();
            endOfStreamEvent = new EndOfStreamEvent();
            return true;
        }

        return false;
    }

    @Override
    public List<Event> getEvents() {

        if (endOfStreamEvent == null) {
            return Collections.emptyList();
        }

        EndOfStreamEvent e = endOfStreamEvent;
        endOfStreamEvent = null;
        return Collections.singletonList(e);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public boolean waitForEndOfStreamMs(long timeoutMs) throws InterruptedException{

        return endOfStreamLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
