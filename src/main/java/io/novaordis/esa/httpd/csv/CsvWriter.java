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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * An event processor that writes the events in a CSV format at the output stream.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public class CsvWriter implements EventProcessor {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final DateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");

    // Static ----------------------------------------------------------------------------------------------------------

    public static String toCsvLine(DateFormat timestampFormat, HttpdLogEvent event) {

        return timestampFormat.format(event.getTimestamp()) + ", -";
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private OutputStream outputStream;

    private DateFormat timestampFormat;

    // Constructors ----------------------------------------------------------------------------------------------------

    public CsvWriter() {

        this.timestampFormat = DEFAULT_TIMESTAMP_FORMAT;
    }

    // EventProcessor implementation -----------------------------------------------------------------------------------

    @Override
    public void process(Event event) throws Exception {

        String line = toCsvLine(timestampFormat, (HttpdLogEvent)event);
        line += "\n";
        outputStream.write(line.getBytes());
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setOutputStream(OutputStream os) {
        this.outputStream = os;
    }

    public DateFormat getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(DateFormat df) {
        this.timestampFormat = df;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
