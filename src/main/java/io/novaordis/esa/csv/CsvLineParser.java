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

package io.novaordis.esa.csv;

import io.novaordis.esa.ParsingException;
import io.novaordis.esa.core.LineFormat;
import io.novaordis.esa.core.LineParser;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.GenericEvent;
import io.novaordis.esa.core.event.Property;
import io.novaordis.esa.core.event.StringProperty;

import java.util.ArrayList;
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

    static List<Property> buildHeaders(CsvFormat format) {

        List<Property> headers = new ArrayList<>();
        for(String f: format.getFields()) {
            headers.add(new StringProperty(f));
        }
        return headers;
    }


    // Attributes ------------------------------------------------------------------------------------------------------

    private CsvFormat lineFormat;

    //
    // we maintain a header different from the line format because this allows us to discover the structure of a
    // CSV file dynamically, even without the presence of a line format specification
    //
    private List<Property> headers;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @throws IllegalArgumentException if the given format is not a valid CSV format specification.
     */
    public CsvLineParser(String formatSpecification) throws IllegalArgumentException {

        lineFormat = new CsvFormat(formatSpecification);
        headers = buildHeaders(lineFormat);
    }

    // LineParser implementation ---------------------------------------------------------------------------------------

    @Override
    public LineFormat getLineFormat() {

        return lineFormat;
    }

    @Override
    public Event parseLine(String line) throws ParsingException {

        GenericEvent event = new GenericEvent();
        int headerIndex = 0;

        for(StringTokenizer st = new StringTokenizer(line, ",");
            st.hasMoreTokens() && headerIndex < headers.size();
            headerIndex ++) {

            String tok = st.nextToken().trim();
            Property header = headers.get(headerIndex);
            Property p = header.fromString(tok);
            event.setProperty(p);
        }

        return event;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return "CsvLineParser[format: " + lineFormat + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
