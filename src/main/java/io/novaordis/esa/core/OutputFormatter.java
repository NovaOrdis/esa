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

import io.novaordis.esa.core.event.EndOfStreamEvent;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.FaultEvent;
import io.novaordis.esa.core.event.Property;
import io.novaordis.esa.core.event.ShutdownEvent;
import io.novaordis.esa.core.event.TimedEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class OutputFormatter implements OutputStreamConversionLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final DateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static final String NULL_EXTERNALIZATION = "";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private StringBuilder sb;
    private volatile boolean closed;
    private String commandLineRequestedOutputFormat;

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
            return true; // need to collect the output stream close() information
        }

        if (inputEvent instanceof ShutdownEvent) {
            throw new RuntimeException("ShutdownEvent SUPPORT NOT YET IMPLEMENTED");
        }

        return externalizeEvent(inputEvent);
    }

    @Override
    public byte[] getBytes() {

        if (closed) {

            if (sb.length() != 0) {

                // there are previously uncollected bytes
                byte[] result = sb.toString().getBytes();
                sb.setLength(0);
                return result;
            }
            else {
                return null;
            }
        }

        if (sb.length() == 0) {
            return EMPTY_BYTE_ARRAY;
        }

        byte[] result = sb.toString().getBytes();
        sb.setLength(0);
        return result;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setFormat(String format) {
        this.commandLineRequestedOutputFormat = format;
    }

    public String getFormat() {
        return commandLineRequestedOutputFormat;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    /**
     * @return true if there are bytes to be collected.
     */
    private boolean externalizeEvent(Event event) {

        if (event instanceof FaultEvent) {

            //
            // TODO we may want to consider to send the fault events to stderr so we don't interfere with stdout
            //

            sb.append(event.toString()).append("\n");
            return true;
        }

        Set<Property> properties = event.getProperties();
        List <Property> orderedProperties = new ArrayList<>(properties);
        Collections.sort(orderedProperties);

        if (event instanceof TimedEvent) {

            //
            // if it's a timed event, always start with the timestamp
            //

            Long timestamp = ((TimedEvent)event).getTimestamp();

            if (timestamp == null) {
                sb.append(NULL_EXTERNALIZATION);
            }
            else {
                sb.append(DEFAULT_TIMESTAMP_FORMAT.format(timestamp));
            }

            if (!properties.isEmpty()) {
                sb.append(", ");
            }
        }

        for(int i = 0; i < orderedProperties.size(); i++) {

            Property p = orderedProperties.get(i);
            String s = p.externalizeValue();

            if (s == null) {
                s = NULL_EXTERNALIZATION;
            }
            sb.append(s);

            if (i < orderedProperties.size() - 1) {
                sb.append(", ");
            }

            //
            // TODO Map Handling
            //
//                    int dot = propertyName.indexOf('.');
//                    if (dot != -1) {
//
//                        // map
//
//                        String mapPropertyName = propertyName.substring(0, dot);
//                        MapProperty mp = (MapProperty)event.getProperty(mapPropertyName);
//                        if (mp != null) {
//
//                            String key = propertyName.substring(dot + 1);
//                            Object value = mp.getMap().get(key);
//
//                            if (value != null) {
//                                line += value;
//                            }
//                        }
//                    }
//                    else {
//
//                        Property p = event.getProperty(propertyName);
//
//                        if (p != null) {
//
//                            Object o = p.getValue();
//
//                            if (o instanceof Map) {
//
//                                line += "<>";
//                            } else {
//                                line += o;
//                            }
//                        }
//                    }
//                }

        }

        sb.append("\n");

        return true;
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

    // Constants -------------------------------------------------------------------------------------------------------



}
