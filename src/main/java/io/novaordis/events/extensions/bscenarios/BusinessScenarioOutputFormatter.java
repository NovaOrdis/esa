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
import io.novaordis.events.core.event.Property;

import java.text.Format;
import java.text.SimpleDateFormat;

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
                    externalizedValue = p.externalizeValue();
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
