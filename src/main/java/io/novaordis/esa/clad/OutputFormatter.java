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

package io.novaordis.esa.clad;

import io.novaordis.esa.core.ClosedException;
import io.novaordis.esa.core.OutputStreamConversionLogic;
import io.novaordis.esa.core.event.EndOfStreamEvent;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.Property;
import io.novaordis.esa.core.event.TimedEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class OutputFormatter implements OutputStreamConversionLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final DateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    // Static ----------------------------------------------------------------------------------------------------------

    public static String toLine(TimedEvent event, String format) {

        return "...";

//        sb.append(DEFAULT_TIMESTAMP_FORMAT.format(e.getTimestamp()));
//
//        Set<Property> properties = e.getProperties();
//
//        if (!properties.isEmpty()) {
//            sb.append(", ");
//        }
//
//        for(Iterator<Property> i = properties.iterator(); i.hasNext(); ) {
//            Property p = i.next();
//            Object value = p.getValue();
//            if (value instanceof Map) {
//                value = "query string";
//            }
//            sb.append(value);
//            if (i.hasNext()) {
//                sb.append(", ");
//            }
//        }

    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private StringBuilder sb;
    private volatile boolean closed;
    private String format;

    // Constructors ----------------------------------------------------------------------------------------------------

    public OutputFormatter() {
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

        TimedEvent e = (TimedEvent)inputEvent;
        sb.append(toLine(e, format)).append("\n");
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

    public void setFormat(String format) {
        this.format = format;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

    // Constants -------------------------------------------------------------------------------------------------------



}
