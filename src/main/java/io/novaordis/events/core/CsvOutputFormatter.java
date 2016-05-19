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
import io.novaordis.events.core.event.FaultEvent;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.ShutdownEvent;
import io.novaordis.events.core.event.TimedEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The class that generically converts events into comma-separated lines containing values for the event's properties.
 * If an output format (a list of property names) is provided externally, the formatter will only include the specified
 * properties in the output. Otherwise, all the properties are introspected and included in the output.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class CsvOutputFormatter implements OutputStreamConversionLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final DateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static final String NULL_EXTERNALIZATION = "";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private StringBuilder sb;
    private volatile boolean closed;
    private List<String> outputFormat;

    // Constructors ----------------------------------------------------------------------------------------------------

    public CsvOutputFormatter() {

        sb = new StringBuilder();
        outputFormat = null;
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

    /**
     * We interpret the given format as comma separated property names (plus "timestamp")
     */
    public void setOutputFormat(String format) {

        if (format == null) {
            this.outputFormat = null;
            return;
        }

        outputFormat = new ArrayList<>();

        for(StringTokenizer st = new StringTokenizer(format, ","); st.hasMoreTokens(); ) {

            outputFormat.add(st.nextToken().trim());
        }
    }

    public String getOutputFormat() {

        if (outputFormat == null) {
            return null;
        }

        String s = "";

        for(Iterator<String> is = outputFormat.iterator(); is.hasNext(); ) {

            s += is.next();

            if (is.hasNext()) {
                s += ", ";
            }
        }

        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * This method was designed to be overridden by more specialized sub-classes, if they choose so. The method
     * receives the Event and returns a comma-separated string generated based on the sub-class instance state.
     */
    protected String toString(Event event) {

        if (outputFormat != null) {
            return externalizeEventInOutputFormat(outputFormat, event);
        }
        else {
            return externalizeEventViaIntrospection(event);
        }
    }

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
        }
        else {
            sb.append(toString(event));
        }
        return true;
    }

    private String externalizeEventInOutputFormat(List<String> outputFormat, Event event) {

        String s = "";

        for(Iterator<String> fni = outputFormat.iterator(); fni.hasNext(); ) {

            String fieldName = fni.next();

            if ("timestamp".equals(fieldName)) {
                Long timestamp = null;
                if (event instanceof TimedEvent) {
                    timestamp = ((TimedEvent)event).getTimestamp();
                }
                s += externalizeTimestamp(timestamp);
            }
            else {

                Object externalizedValue = null;
                Property p = event.getProperty(fieldName);
                if (p != null) {
                    externalizedValue = p.externalizeValue();
                }
                if (externalizedValue == null) {
                    s += NULL_EXTERNALIZATION;
                }
                else {
                    s += externalizedValue;
                }
            }

            if (fni.hasNext()) {
                s += ", ";
            }
        }

        s += "\n";
        return s;
    }

    private String externalizeEventViaIntrospection(Event event) {

        String s = "";

        Set<Property> properties = event.getProperties();
        List <Property> orderedProperties = new ArrayList<>(properties);
        Collections.sort(orderedProperties);

        if (event instanceof TimedEvent) {

            //
            // if it's a timed event, always start with the timestamp
            //

            Long timestamp = ((TimedEvent)event).getTimestamp();

            if (timestamp == null) {
                s += NULL_EXTERNALIZATION;
            }
            else {
                s += DEFAULT_TIMESTAMP_FORMAT.format(timestamp);
            }

            if (!properties.isEmpty()) {
                s += ", ";
            }
        }

        for(int i = 0; i < orderedProperties.size(); i++) {

            Property p = orderedProperties.get(i);
            String ev = p.externalizeValue();

            if (ev == null) {
                ev = NULL_EXTERNALIZATION;
            }
            s += ev;

            if (i < orderedProperties.size() - 1) {
                s += ", ";
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

        s += "\n";
        return s;
    }

    private String externalizeTimestamp(Long timestamp) {

        if (timestamp == null) {
            return NULL_EXTERNALIZATION;
        }
        else {
            return DEFAULT_TIMESTAMP_FORMAT.format(timestamp);
        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

    // Constants -------------------------------------------------------------------------------------------------------



}
