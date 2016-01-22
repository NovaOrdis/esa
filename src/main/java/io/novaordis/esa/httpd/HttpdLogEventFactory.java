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

package io.novaordis.esa.httpd;

import io.novaordis.esa.ParsingException;

import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public class HttpdLogEventFactory implements LogEventFactory {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private HttpdLogFormat logFormat;

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpdLogEventFactory(HttpdLogFormat logFormat) {

        this.logFormat = logFormat;
    }

    // LogEventFactory implementation ----------------------------------------------------------------------------------

    @Override
    public HttpdLogFormat getLogFormat() {
        return logFormat;
    }

    @Override
    public HttpdLogEvent parse(String line) throws ParsingException {

        HttpdLogEvent e = new HttpdLogEvent();

        //
        // we don't perform a match against am aggregated format that would match the entire line because
        // we also want to handle incomplete lines. In the future, we will see if this is useful, and if not
        // replace the current parsing code with matching against the aggregated pattern (TODO: evaluate this later)
        //

        List<HttpdFormatElement> fes = logFormat.getFormatElements();

        int cursor = 0;
        for(HttpdFormatElement fe: fes) {

            char c = line.charAt(cursor);

            // find the next gap

            if (c == '"') {
                throw new RuntimeException("\" NOT PROPERLY HANDLED YET");
            }

            int i = line.indexOf(' ', cursor);

            String value = line.substring(cursor, i);
            Object o = fe.parse(value);
            e.setValue(fe, o);

            // advance the cursor to the next non-blank character
            //noinspection StatementWithEmptyBody
            while(line.charAt(i) == ' ') {
                i++;
            }
            cursor = i;
        }

        return e;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return "format: " + getLogFormat();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
