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

    static List<Field> buildHeaders(CsvFormat format) {

        List<Field> headers = new ArrayList<>();
        for(Field f: format.getFields()) {
            headers.add(f);
        }
        return headers;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private CsvFormat lineFormat;

    //
    // we maintain a header different from the line format because this allows us to discover the structure of a
    // CSV file dynamically, even without the presence of a line format specification
    //
    private List<Field> headers;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @throws IllegalArgumentException if the given format specification cannot be used to build a CSV format.
     * @throws InvalidFieldException we determined that the format specification <b>can</b> be used to build a CSV
     * format but we find an incorrectly specified field (example: invalid type, etc.)
     */
    public CsvLineParser(String formatSpecification) throws IllegalArgumentException, InvalidFieldException {

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
            Field header = headers.get(headerIndex);
            Property p = header.toProperty(tok);
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
