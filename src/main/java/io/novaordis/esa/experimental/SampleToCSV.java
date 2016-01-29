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

package io.novaordis.esa.experimental;

import io.novaordis.esa.core.ClosedException;
import io.novaordis.esa.core.OutputStreamConversionLogic;
import io.novaordis.esa.core.event.ContainerEvent;
import io.novaordis.esa.core.event.EndOfStreamEvent;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.logs.httpd.HttpdLogLine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class SampleToCSV implements OutputStreamConversionLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final DateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private StringBuilder sb;
    private volatile boolean closed;

    // Constructors ----------------------------------------------------------------------------------------------------

    public SampleToCSV() {
        sb = new StringBuilder();
    }

    // OutputStreamConversionLogic implementation ----------------------------------------------------------------------

    @Override
    public boolean process(Event inputEvent) throws ClosedException {

        if (closed) {
            throw new ClosedException(this + " closed");
        }

        if (inputEvent instanceof EndOfStreamEvent) {
            closed = true;
            return false;
        }

        if (inputEvent instanceof SampleEvent) {
            SampleEvent ce = (SampleEvent) inputEvent;
            String s =
                    DEFAULT_TIMESTAMP_FORMAT.format(ce.getTimestamp()) + ", " +
                            ce.getCount() + ", " +
                            String.format("%1$.1f", ce.getAverageTime()) + "\n";

            sb.append(s);
        }
        else if (inputEvent instanceof ContainerEvent) {

            HttpdLogLine line = (HttpdLogLine)((ContainerEvent) inputEvent).get();
            sb.append(line.toString()).append("\n");
        }
        else {
            throw new IllegalArgumentException("unknown event type " + inputEvent);
        }

        return true;
    }

    @Override
    public byte[] getBytes() {

        if (sb.length() == 0) {
            return EMPTY_BYTE_ARRAY;
        }

        byte[] result = sb.toString().getBytes();
        sb.setLength(0);
        return result;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

    // Constants -------------------------------------------------------------------------------------------------------



}
