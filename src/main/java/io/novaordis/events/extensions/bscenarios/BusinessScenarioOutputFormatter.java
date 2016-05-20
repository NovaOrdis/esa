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

package io.novaordis.events.extensions.bscenarios;

import io.novaordis.events.core.CsvOutputFormatter;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.ListProperty;
import io.novaordis.events.core.event.Property;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * BusinessScenario-to-CSV formatting logic.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 5/18/16
 */
public class BusinessScenarioOutputFormatter extends CsvOutputFormatter {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final Format TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    // Static ----------------------------------------------------------------------------------------------------------

    private static String[] PROPERTIES_TO_DISPLAY = {
            "timestamp",
            BusinessScenarioEvent.ID,
            BusinessScenarioEvent.JSESSIONID,
            BusinessScenarioEvent.ITERATION_ID,
            BusinessScenarioEvent.TYPE,
            BusinessScenarioEvent.STATE,
            BusinessScenarioEvent.REQUEST_COUNT,
            BusinessScenarioEvent.SUCCESSFUL_REQUEST_COUNT,
            BusinessScenarioEvent.DURATION,

            // this is a List property, it'll generate a comma-separated list of values
            BusinessScenarioEvent.REQUEST_DURATIONS,

            // this is a List property, it'll generate a comma-separated list of values
            BusinessScenarioEvent.REQUEST_STATUS_CODES,
    };

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public BusinessScenarioOutputFormatter() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected String toString(Event event) {

        if (!(event instanceof BusinessScenarioEvent)) {
            throw new IllegalArgumentException("expected BusinessScenarioEvent, got " + event);
        }

        BusinessScenarioEvent bse = (BusinessScenarioEvent)event;

        String s = "";

        for(int i = 0; i < PROPERTIES_TO_DISPLAY.length; i ++) {

            String propertyName = PROPERTIES_TO_DISPLAY[i];

            if ("timestamp".equals(propertyName)) {
                Long timestamp = bse.getTimestamp();
                s += timestamp == null ? NULL_EXTERNALIZATION : TIMESTAMP_FORMAT.format(timestamp);
            }
            else {

                Object externalizedValue = null;
                Property p = event.getProperty(propertyName);

                if (p != null) {

                    if (p instanceof ListProperty) {
                        //
                        // for ListProperties, the externalized value is no good, create our own
                        //
                        externalizedValue = "";
                        //noinspection unchecked
                        List values = ((ListProperty)p).getList();
                        for(int j = 0; j < values.size(); j ++) {
                            Object v = values.get(j);
                            s += (v == null ? NULL_EXTERNALIZATION : "" + v);
                            if (j < values.size() - 1) {
                                s += ", ";
                            }
                        }
                    }
                    else {
                        externalizedValue = p.externalizeValue();
                    }
                }
                if (externalizedValue == null) {
                    s += NULL_EXTERNALIZATION;
                }
                else {
                    s += externalizedValue;
                }
            }

            if (i < PROPERTIES_TO_DISPLAY.length - 1) {
                s += ", ";
            }
        }

        //
        // list the status code and the duration of the enclosed requests
        //


        return s;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
