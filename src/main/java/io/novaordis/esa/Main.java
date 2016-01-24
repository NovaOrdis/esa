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
import io.novaordis.esa.csv.EventToCsvConverter;
import io.novaordis.esa.logs.httpd.HttpdLogParsingLogic;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class Main {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final int QUEUE_SIZE = 10;

    // Static ----------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

//        EventProcessor inputStreamReader = new EventProcessor("Input Stream Reader");
//        inputStreamReader.setInput(System.in);
//        inputStreamReader.setByteLogic(new InputStreamConverter());
//        inputStreamReader.setOutput(new ArrayBlockingQueue<>(QUEUE_SIZE));

        InputStreamInitiator initiator = new InputStreamInitiator("Input Stream Reader");
        initiator.setInputStream(System.in);
        initiator.setConversionLogic(new StringEventProducer());
        initiator.setOutputQueue(new ArrayBlockingQueue<>(QUEUE_SIZE));

        BlockingQueue<Event> streamReaderToParserQueue = initiator.getOutputQueue();

//        EventProcessor two = new EventProcessor("httpd log parser");
//        two.setInput(inputStreamReader.getOutputQueue());
//        two.setEventLogic(new HttpdLogParser());
//        two.setOutput(new ArrayBlockingQueue<>(QUEUE_SIZE));

        EventProcessor httpdLogParser = new EventProcessor("HTTP Log Parser");
        httpdLogParser.setInputQueue(streamReaderToParserQueue);
        httpdLogParser.setProcessingLogic(new HttpdLogParsingLogic());
        httpdLogParser.setOutputQueue(new ArrayBlockingQueue<>(QUEUE_SIZE));

        BlockingQueue<Event> parserToCsvWriterQueue = httpdLogParser.getOutputQueue();

//        OldEventProcessor three = new OldEventProcessor("csv writer");
//        three.setInput(two.getOutputQueue());
//        three.setEventLogic(new EventCSVWriter());
//        three.setOutput(System.out);

        OutputStreamTerminator terminator = new OutputStreamTerminator("CSV Writer");
        terminator.setInputQueue(parserToCsvWriterQueue);
        terminator.setConversionLogic(new EventToCsvConverter());
        terminator.setOutputStream(System.out);


        EndOfStreamListener eos = new EndOfStreamListener() {
            @Override
            public void eventStreamEnded() {

                //
                // document synchronization primitives
                //

                throw new RuntimeException(this + ".eventStreamEnded() NOT YET IMPLEMENTED");
            }
        };

        terminator.addEndOfStreamListener(eos);

        initiator.start();
        httpdLogParser.start();
        terminator.start();

        //
        // document synchronization primitives
        //

        Thread.currentThread().sleep(3600 * 1000L);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
