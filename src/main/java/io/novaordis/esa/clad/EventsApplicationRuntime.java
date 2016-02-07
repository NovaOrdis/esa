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

package io.novaordis.esa.clad;

import io.novaordis.clad.application.ApplicationRuntimeBase;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.UserErrorException;
import io.novaordis.clad.option.Option;
import io.novaordis.clad.option.StringOption;
import io.novaordis.esa.LineParserFactory;
import io.novaordis.esa.clad.command.OutputCommand;
import io.novaordis.esa.core.EventProcessor;
import io.novaordis.esa.core.InputStreamInitiator;
import io.novaordis.esa.core.LineParser;
import io.novaordis.esa.core.LineStreamParser;
import io.novaordis.esa.core.OutputStreamTerminator;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.StringEventConverter;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/27/16
 */
public class EventsApplicationRuntime extends ApplicationRuntimeBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(EventsApplicationRuntime.class);

    public static final int QUEUE_SIZE = 1000000;

    public static final StringOption INPUT_FORMAT_OPTION = new StringOption('i', "input-format");

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private InputStreamInitiator initiator;
    private EventProcessor parser;
    private OutputStreamTerminator terminator;
    private CountDownLatch endOfStream;

    // Constructors ----------------------------------------------------------------------------------------------------

    // ApplicationRuntime implementation -------------------------------------------------------------------------------

    @Override
    public String getDefaultCommandName() {
        return new OutputCommand().getName();
    }

    @Override
    public Set<Option> requiredGlobalOptions() {

        return Collections.singleton(INPUT_FORMAT_OPTION);
    }

    @Override
    public Set<Option> optionalGlobalOptions() {
        return Collections.emptySet();
    }

    @Override
    public void init(Configuration configuration) throws Exception {

        log.debug(this + ".init(" + configuration + ")");

        //
        // figure out what kind of format we are going to be parsing
        //

        LineParser lineParser = figureOutParserTypeBasedOnInputFormatString(configuration);

        //
        // assemble the processing pipeline
        //

        initiator = new InputStreamInitiator(
                "Input Stream Reader",
                System.in,
                new StringEventConverter(),
                new ArrayBlockingQueue<>(QUEUE_SIZE));

        parser = new EventProcessor(
                "Input Event Stream Parser",
                initiator.getOutputQueue(),
                new LineStreamParser(lineParser),
                new ArrayBlockingQueue<>(QUEUE_SIZE));

        terminator = new OutputStreamTerminator(
                "Output Writer",
                null,
                new OutputFormatter(),
                System.out);

        endOfStream = new CountDownLatch(1);
        terminator.addEndOfStreamListener(endOfStream::countDown);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public BlockingQueue<Event> getOutputQueue() {

        return parser.getOutputQueue();
    }

    public OutputStreamTerminator getTerminator() {
        return terminator;
    }

    public void start() throws Exception {

        initiator.start();
        parser.start();
        terminator.start();
    }

    public void waitForEndOfStream() throws InterruptedException {

        //
        // wait for the end of stream to propagate through the pipeline
        //
        endOfStream.await();
    }

    @Override
    public String toString() {

        return "Events[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    /**
     * @return guaranteed not null instance.
     *
     * @throws UserErrorException if we cannot figure out what line parser to use.
     */
    private LineParser figureOutParserTypeBasedOnInputFormatString(Configuration configuration)
            throws UserErrorException {


        StringOption inputFormat = (StringOption)configuration.getGlobalOption(INPUT_FORMAT_OPTION);

        if (inputFormat == null) {
            throw new UserErrorException("input format not specified, use " + INPUT_FORMAT_OPTION.getLabel());
        }

        String format = inputFormat.getString();

        LineParser lineParser = LineParserFactory.getInstance(format);

        if (lineParser == null) {
            throw new UserErrorException("no known parser knows how to interpret the format string \"" + format + "\"");
        }

        return lineParser;
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
