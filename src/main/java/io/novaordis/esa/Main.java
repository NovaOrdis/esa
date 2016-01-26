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

import io.novaordis.esa.core.EndOfStreamListener;
import io.novaordis.esa.core.InputStreamInitiator;
import io.novaordis.esa.core.EventProcessor;
import io.novaordis.esa.core.OutputStreamTerminator;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.StringEventProducer;
import io.novaordis.esa.csv.EventToCSV;
import io.novaordis.esa.logs.httpd.HttpdLogParsingLogic;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class Main {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final int QUEUE_SIZE = 10;

    // Static ----------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        InputStreamInitiator initiator = new InputStreamInitiator("Input Stream Reader");
        initiator.setInputStream(System.in);
        initiator.setConversionLogic(new StringEventProducer());
        initiator.setOutputQueue(new ArrayBlockingQueue<>(QUEUE_SIZE));

        BlockingQueue<Event> streamReaderToParserQueue = initiator.getOutputQueue();

        EventProcessor httpdLogParser = new EventProcessor("HTTP Log Parser");
        httpdLogParser.setInputQueue(streamReaderToParserQueue);
        httpdLogParser.setProcessingLogic(new HttpdLogParsingLogic());
        httpdLogParser.setOutputQueue(new ArrayBlockingQueue<>(QUEUE_SIZE));

        BlockingQueue<Event> parserToCsvWriterQueue = httpdLogParser.getOutputQueue();

        OutputStreamTerminator terminator = new OutputStreamTerminator("CSV Writer");
        terminator.setInputQueue(parserToCsvWriterQueue);
        terminator.setConversionLogic(new EventToCSV());
        terminator.setOutputStream(System.out);

        final CountDownLatch endOfStream = new CountDownLatch(1);
;
        EndOfStreamListener eos = new EndOfStreamListener() {
            @Override
            public void eventStreamEnded() {
                endOfStream.countDown();
            }
        };

        terminator.addEndOfStreamListener(eos);

        initiator.start();
        httpdLogParser.start();
        terminator.start();

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
