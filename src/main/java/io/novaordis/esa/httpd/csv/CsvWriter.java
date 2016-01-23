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

package io.novaordis.esa.httpd.csv;

import io.novaordis.clad.UserErrorException;
import io.novaordis.esa.Event;
import io.novaordis.esa.EventProcessor;
import io.novaordis.esa.httpd.HttpdLogEvent;

import java.io.OutputStream;

/**
 * An event processor that writes the events in a CSV format at the output stream.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public class CsvWriter implements EventProcessor {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    public static String toCsvLine(Event event) {
        return ",";
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private OutputStream outputStream;

    // Constructors ----------------------------------------------------------------------------------------------------

    // EventProcessor implementation -----------------------------------------------------------------------------------

    @Override
    public void process(Event event) throws Exception {

        String line = toCsvLine(event);
        line += "\n";
        outputStream.write(line.getBytes());
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setOutputStream(OutputStream os) {
        this.outputStream = os;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
