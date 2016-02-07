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

import io.novaordis.esa.core.LineFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class CsvFormat implements LineFormat {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<String> fields;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @throws IllegalArgumentException if the given format specification cannot be used to build a CSV format.
     */
    public CsvFormat(String formatSpecification) throws IllegalArgumentException {

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

            String field = formatSpecification.substring(i, j).trim();

            if (i >= lastComma && field.length() == 0 && !fields.isEmpty()) {
                // does not count
                break;
            }

            fields.add(field);

            i = j + 1;
            j = formatSpecification.indexOf(',', i);
            j = j == -1 ? j = formatSpecification.length() : j;
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the actual underlying storage so handle with care.
     */
    public List<String> getFields() {
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

    // Inner classes ---------------------------------------------------------------------------------------------------

}
