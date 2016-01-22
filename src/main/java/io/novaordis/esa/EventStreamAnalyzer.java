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
import io.novaordis.esa.httpd.HttpdLogEventFactory;
import io.novaordis.esa.httpd.HttpdLogFormat;
import io.novaordis.esa.httpd.HttpdLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicLong;

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

        HttpdLogFormat httpdLogFormat = new HttpdLogFormat();
        HttpdLogEventFactory eventFactory = new HttpdLogEventFactory(httpdLogFormat);

        AtomicLong counter = new AtomicLong(0);

        EventProcessor ep = new EventProcessor() {

            @Override
            public void process(Event event) throws UserErrorException {

                HttpdLogEvent httpdle = (HttpdLogEvent)event;
                counter.incrementAndGet();
            }
        };

        BufferedReader input = null;

        try {

            input = new BufferedReader(new InputStreamReader(System.in), BUFFER_SIZE);

            String line;
            while ((line = input.readLine()) != null) {

                HttpdLogEvent le = eventFactory.parse(line);
                ep.process(le);
            }
        }
        catch(IOException | ParsingException e) {
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

        System.out.println("total events: " + counter.get());

    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
