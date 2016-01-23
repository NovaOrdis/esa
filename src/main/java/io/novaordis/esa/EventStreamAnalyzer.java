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

package io.novaordis.esa;

import io.novaordis.clad.CommandLineDriven;
import io.novaordis.clad.UserErrorException;
import io.novaordis.esa.httpd.HttpdFormatElement;
import io.novaordis.esa.httpd.csv.CsvWriter;
import io.novaordis.esa.httpd.HttpdLogEventFactory;
import io.novaordis.esa.httpd.HttpdLogFormat;
import io.novaordis.esa.httpd.HttpdLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class EventStreamAnalyzer implements CommandLineDriven {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(EventStreamAnalyzer.class);

    public static final int BUFFER_SIZE = 1024 * 1024;

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // CommandLineDriven implementation --------------------------------------------------------------------------------

    @Override
    public void executeCommandLine(String[] strings) throws UserErrorException {

        // TODO - how do I infer this from the log file? I need to externalize it in a friendly way
        HttpdLogFormat httpdLogFormat = new HttpdLogFormat(
                HttpdFormatElement.THREAD_NAME,
                HttpdFormatElement.REMOTE_HOST,
                HttpdFormatElement.REMOTE_LOGNAME,
                HttpdFormatElement.REMOTE_USER,
                HttpdFormatElement.OPENING_BRACKET,
                HttpdFormatElement.TIMESTAMP,
                HttpdFormatElement.CLOSING_BRACKET,
                HttpdFormatElement.DOUBLE_QUOTES,
                HttpdFormatElement.FIRST_REQUEST_LINE,
                HttpdFormatElement.DOUBLE_QUOTES,
                HttpdFormatElement.STATUS_CODE,
                HttpdFormatElement.RESPONSE_ENTITY_BODY_SIZE,
                HttpdFormatElement.REQUEST_PROCESSING_TIME_MS
        );


        HttpdLogEventFactory eventFactory = new HttpdLogEventFactory(httpdLogFormat);

        CsvWriter csvWriter = new CsvWriter();
        csvWriter.setOutputStream(System.out);

        BufferedReader input = null;

        try {

            input = new BufferedReader(new InputStreamReader(System.in), BUFFER_SIZE);

            String line;
            while ((line = input.readLine()) != null) {

                HttpdLogEvent le = eventFactory.parse(line);
                csvWriter.process(le);
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        finally {

            if (input != null) {

                try {
                    input.close();
                }
                catch(IOException e) {
                    log.error("failed to close input stream", e);
                }
            }
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
