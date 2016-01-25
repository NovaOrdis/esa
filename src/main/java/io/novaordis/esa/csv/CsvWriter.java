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

import io.novaordis.esa.logs.httpd.LogLine;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * An event processor that writes the events in a CSV format at the output stream.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public class CsvWriter {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final DateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");

    // Static ----------------------------------------------------------------------------------------------------------

    public static String toCsvLine(DateFormat timestampFormat, LogLine event) {

        String s =
                timestampFormat.format(event.timestamp) + ", " +
                event.getThreadName() + ", " +
                event.getFirstRequestLine() + ", " +
                event.getOriginalRequestStatusCode() + ", " +
                event.getResponseEntityBodySize() + ", " +
                event.getRequestProcessingTimeMs();

        return s;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private OutputStream outputStream;

    private DateFormat timestampFormat;

    // Constructors ----------------------------------------------------------------------------------------------------

    public CsvWriter() {

        this.timestampFormat = DEFAULT_TIMESTAMP_FORMAT;
    }

    public void process(LogLine event) throws Exception {

        String line = toCsvLine(timestampFormat, (LogLine)event);
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