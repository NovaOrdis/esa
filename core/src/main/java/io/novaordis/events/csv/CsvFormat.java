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

import io.novaordis.events.core.LineFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * The class encapsulates a CSV line format specification. Instances of this class are used by CSV parsers, which
 * turn CSV text lines into events, or CSV formatters, which turn events into CSV text lines. The parsing/formatting
 * is an external concern, this class' only responsibility is to maintain the format information in a way that is as
 * flexible as possible.
 *
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class CSVFormat implements LineFormat {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private int unnamedFieldCounter = 0;

    private List<CSVField> fields;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param formatSpecification - a comma-separated field specifications.
     *
     * @see CSVField
     *
     * @throws IllegalArgumentException if the given format specification cannot be used to build a CSV format.
     *
     * @throws InvalidCSVFieldException we determined that the format specification <b>can</b> be used to build a CSV
     * format but we find an incorrectly specified field (example: invalid type, etc.)
     */
    public CSVFormat(String formatSpecification) throws IllegalArgumentException, InvalidCSVFieldException {

        if (formatSpecification == null) {

            throw new IllegalArgumentException("null format specification");
        }

        int lastComma = formatSpecification.lastIndexOf(',');

        if (lastComma == -1) {

            throw new IllegalArgumentException(
                    "\"" + formatSpecification + "\" cannot be a CSV format specification, it does not contain commas");
        }

        fields = new ArrayList<>();

        for(int i = 0, j = formatSpecification.indexOf(','); i < formatSpecification.length(); ) {

            String fieldSpec = formatSpecification.substring(i, j).trim();

            if (i >= lastComma && fieldSpec.length() == 0 && !fields.isEmpty()) {

                // does not count
                break;
            }

            CSVField field = new CSVField(fieldSpec);
            if (field.getName().length() == 0) {
                field.setName(nextUnnamedFieldName());
            }
            fields.add(field);

            i = j + 1;
            j = formatSpecification.indexOf(',', i);
            j = j == -1 ? formatSpecification.length() : j;
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the actual underlying storage so handle with care.
     */
    public List<CSVField> getFields() {
        return fields;
    }

    @Override
    public String toString() {

        String s = "";
        for(int i = 0; i < fields.size(); i ++) {

            s += fields.get(i);

            if (i < fields.size()) {
                s += ", ";
            }
        }
        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private String nextUnnamedFieldName() {

        int i = ++unnamedFieldCounter;
        return "CSVField" + (i < 10 ? "0" : "") + i;
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
