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
import io.novaordis.esa.core.event.MapProperty;
import io.novaordis.esa.core.event.Property;
import io.novaordis.esa.core.event.StringEvent;
import io.novaordis.esa.core.event.TimedEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

        String line = "?";

        if ("def".equals(format)) {

            //
            // dump the structure of the request (property names)
            //

            line = "timestamp";

            Set<Property> properties = event.getProperties();
            List<String> propertyNames = new ArrayList<>();

            for(Property p: properties) {

                if (p.getType().equals(Map.class)) {

                    Map map = (Map)p.getValue();
                    for(Object key: map.keySet()) {
                        propertyNames.add(p.getName() + "." + key);
                    }
                }
                else {
                    propertyNames.add(p.getName());
                }
            }

            if (!propertyNames.isEmpty()) {

                line += ", ";

                Collections.sort(propertyNames);

                for(Iterator<String> i = propertyNames.iterator(); i.hasNext(); ) {

                    String propertyName = i.next();
                    line += propertyName;
                    if (i.hasNext()) {
                        line += ", ";
                    }
                }
            }
        }
        else {
            //
            // interpreted as event property names
            //
            String[] propertyNames = format.split(", *");

            for(int i = 0; i < propertyNames.length; i ++) {

                String propertyName = propertyNames[i];

                if ("timestamp".equals(propertyName)) {

                    line = DEFAULT_TIMESTAMP_FORMAT.format(event.getTimestamp());
                }
                else {

                    //
                    // if the property has a dot in it, it's a map
                    //
                    int dot = propertyName.indexOf('.');
                    if (dot != -1) {

                        // map

                        String mapPropertyName = propertyName.substring(0, dot);
                        MapProperty mp = (MapProperty)event.getProperty(mapPropertyName);
                        if (mp != null) {

                            String key = propertyName.substring(dot + 1);
                            Object value = mp.getMap().get(key);

                            if (value != null) {
                                line += value;
                            }
                        }
                    }
                    else {

                        Property p = event.getProperty(propertyName);

                        if (p != null) {

                            Object o = p.getValue();

                            if (o instanceof Map) {

                                line += "<>";
                            } else {
                                line += o;
                            }
                        }
                    }
                }

                if (i < propertyNames.length - 1) {
                    line += ", ";
                }
            }
        }

        return line;
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
        else if (inputEvent instanceof StringEvent) {

            String s = ((StringEvent)inputEvent).get();
            sb.append(s).append("\n");
            return true;
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
