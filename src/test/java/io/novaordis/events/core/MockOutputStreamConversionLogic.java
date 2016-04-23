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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class MockOutputStreamConversionLogic extends MockConversionLogic implements OutputStreamConversionLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final byte[] EMPTY_ARRAY = new byte[0];

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean closed;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockOutputStreamConversionLogic() {

        closed = false;
    }

    // OutputStreamConversionLogic implementation ----------------------------------------------------------------------

    @Override
    public boolean process(Event inputEvent) {

        if (inputEvent instanceof EndOfStreamEvent) {
            closed = true;
        }

        //
        // act like we drop everything on the floor
        //
        return false;
    }

    @Override
    public byte[] getBytes() {

        return EMPTY_ARRAY;
    }

    @Override
    public boolean isClosed() {

        return closed;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
