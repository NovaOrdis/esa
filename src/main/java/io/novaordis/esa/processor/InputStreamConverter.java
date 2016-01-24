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

package io.novaordis.esa.processor;

import io.novaordis.esa.event.Event;
import io.novaordis.esa.event.special.EndOfStreamEvent;
import io.novaordis.esa.event.special.StringEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This logic reads bytes coming from an input byte stream, identifies lines and generates a new StringEvent per line.
 * At the end of stream generates an EndOfStreamEvent.
 *
 * Guaranteed single threaded.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/23/16
 */
public class InputStreamConverter implements ByteLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private StringBuilder sb;

    // Constructors ----------------------------------------------------------------------------------------------------

    public InputStreamConverter() {

        this.sb = new StringBuilder();
    }

    // ByteLogic implementation ----------------------------------------------------------------------------------------

    @Override
    public List<Event> process(int b) {

        List<Event> result;

        if (b == -1) {

            //
            // end of stream
            //

            if (sb.length() == 0) {
                result = Collections.singletonList(new EndOfStreamEvent());
            }
            else
            {
                result = Arrays.asList(new StringEvent(sb.toString()), new EndOfStreamEvent());
            }
            sb.setLength(0);
            return result;
        }
        else if (b == '\n') {

            result =  Collections.singletonList(new StringEvent(sb.toString()));
            sb.setLength(0);
        }
        else {
            sb.append((char) b);
            result = Collections.emptyList();
        }

        return result;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
