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

package io.novaordis.events.csv;

import io.novaordis.events.ParsingException;
import io.novaordis.events.core.LineFormat;
import io.novaordis.events.core.LineParser;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.GenericEvent;
import io.novaordis.events.core.event.GenericTimedEvent;
import io.novaordis.events.core.event.Property;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class CsvLineParser implements LineParser {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Package Protected Static ----------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private CsvFormat lineFormat;

    //
    // we maintain a header different from the line format because this allows us to discover the structure of a
    // CSV file dynamically, even without the presence of a line format specification
    //
    private List<Field> headers;

    private int timestampFieldIndex;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @throws IllegalArgumentException if the given format specification cannot be used to build a CSV format.
     * @throws InvalidFieldException we determined that the format specification <b>can</b> be used to build a CSV
     * format but we find an incorrectly specified field (example: invalid type, etc.)
     */
    public CsvLineParser(String formatSpecification) throws IllegalArgumentException, InvalidFieldException {

        lineFormat = new CsvFormat(formatSpecification);

        int i = 0;
        timestampFieldIndex = -1;
        headers = new ArrayList<>();
        for(Field f: lineFormat.getFields()) {

            headers.add(f);

            //
            // the first "Date" fields will be used as timestamp
            //
            if (Date.class.equals(f.getType()) && timestampFieldIndex == -1) {
                // this is our timestamp
                timestampFieldIndex = i;
            }

            i++;
        }
    }

    // LineParser implementation ---------------------------------------------------------------------------------------

    @Override
    public LineFormat getLineFormat() {

        return lineFormat;
    }

    @Override
    public Event parseLine(String line) throws ParsingException {

        Event event;
        if (timestampFieldIndex >= 0) {
            event = new GenericTimedEvent();
        }
        else {
            event = new GenericEvent();
        }

        int headerIndex = 0;

        for(StringTokenizer st = new StringTokenizer(line, ",");
            st.hasMoreTokens() && headerIndex < headers.size();
            headerIndex ++) {

            String tok = st.nextToken().trim();
            Field header = headers.get(headerIndex);

            if (headerIndex == timestampFieldIndex) {

                //
                // this is our timestamp, set the timed event timestamp, and not a regular property
                //
                DateFormat dateFormat = (DateFormat)header.getFormat();

                Date d;

                try {
                    d = dateFormat.parse(tok);
                }
                catch(Exception e) {
                    throw new ParsingException(
                            "invalid timestamp value \"" + tok + "\", does not match the required timestamp format", e);
                }

                ((GenericTimedEvent)event).setTimestamp(d.getTime());
            }
            else {
                Property p = header.toProperty(tok);
                event.setProperty(p);
            }
        }

        return event;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return "CsvLineParser[format: " + lineFormat + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    List<Field> getHeaders() {
        return headers;
    }

    int getTimestampFieldIndex() {
        return timestampFieldIndex;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
