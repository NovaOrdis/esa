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

import io.novaordis.esa.processor.EventCSVWriter;
import io.novaordis.esa.processor.HttpdLogParser;
import io.novaordis.esa.processor.InputStreamConverter;
import io.novaordis.esa.processor.SingleThreadedEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class EventStreamAnalyzer {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(EventStreamAnalyzer.class);

    // Static ----------------------------------------------------------------------------------------------------------

    public static final int QUEUE_SIZE = 10;

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // CommandLineDriven implementation --------------------------------------------------------------------------------

    public void run() throws Exception {

        SingleThreadedEventProcessor one = new SingleThreadedEventProcessor("input stream reader");

        one.setInput(System.in);
        one.setByteLogic(new InputStreamConverter());
        one.setOutput(new ArrayBlockingQueue<>(QUEUE_SIZE));

        SingleThreadedEventProcessor two = new SingleThreadedEventProcessor("httpd log parser");
        two.setInput(one.getOutputQueue());
        two.setEventLogic(new HttpdLogParser());
        two.setOutput(new ArrayBlockingQueue<>(QUEUE_SIZE));

        SingleThreadedEventProcessor three = new SingleThreadedEventProcessor("csv writer");
        three.setInput(two.getOutputQueue());
        three.setEventLogic(new EventCSVWriter());
        three.setOutput(System.out);

        one.start();
        two.start();
        three.start();

        three.waitForEndOfStream();

        log.info("sleeping ...");
        Thread.currentThread().sleep(3600 * 1000L);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
