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

import io.novaordis.esa.core.InputStreamInitiator;
import io.novaordis.esa.core.EventProcessor;
import io.novaordis.esa.core.NoopProcessingLogic;
import io.novaordis.esa.core.OutputStreamTerminator;
import io.novaordis.esa.core.event.StringEventConverter;
import io.novaordis.esa.csv.EventToCSV;
import io.novaordis.esa.experimental.ExperimentalLogic;
import io.novaordis.esa.logs.httpd.HttpdLogParsingLogic;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class Main {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final int QUEUE_SIZE = 1000000;

    // Static ----------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        InputStreamInitiator initiator = new InputStreamInitiator(
                "Input Stream Reader",
                System.in,
                new StringEventConverter(),
                new ArrayBlockingQueue<>(QUEUE_SIZE));

        EventProcessor httpdLogParser = new EventProcessor(
                "HTTP Log Parser",
                initiator.getOutputQueue(),
                new HttpdLogParsingLogic(),
                new ArrayBlockingQueue<>(QUEUE_SIZE));

        EventProcessor sampler = new EventProcessor(
                "Sampler",
                httpdLogParser.getOutputQueue(),
                new ExperimentalLogic(),
                new ArrayBlockingQueue<>(QUEUE_SIZE));

        OutputStreamTerminator terminator = new OutputStreamTerminator(
                "CSV Writer",
                sampler.getOutputQueue(),
                new EventToCSV(),
                System.out);

        final CountDownLatch endOfStream = new CountDownLatch(1);
        sampler.addEndOfStreamListener(endOfStream::countDown);

        initiator.start();
        httpdLogParser.start();
        sampler.start();
        //terminator.start();

        //
        // wait for the end of stream to propagate through the pipeline
        //
        endOfStream.await();
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
