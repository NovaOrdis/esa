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

package io.novaordis.events.clad;

import io.novaordis.clad.application.ApplicationRuntimeBase;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.BooleanOption;
import io.novaordis.clad.option.Option;
import io.novaordis.clad.option.StringOption;
import io.novaordis.clad.option.TimestampOption;
import io.novaordis.events.LineParserFactory;
import io.novaordis.events.clad.command.OutputCommand;
import io.novaordis.events.core.EventFilter;
import io.novaordis.events.core.EventProcessor;
import io.novaordis.events.core.InputStreamInitiator;
import io.novaordis.events.core.LineParser;
import io.novaordis.events.core.LineStreamParser;
import io.novaordis.events.core.CsvOutputFormatter;
import io.novaordis.events.core.OutputStreamTerminator;
import io.novaordis.events.core.ProcessingLogic;
import io.novaordis.events.core.event.ByteToLineEventConverter;
import io.novaordis.utilities.UserErrorException;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Lifecycle:
 *
 * 1. Constructed with EventsApplicationRuntime.class.newInstance()
 *
 * 2. init(configuration) is invoked.
 *
 * 3. command.execute(Configuration, runtime instance)
 *
 * For more details see:
 *
 * @see io.novaordis.clad.CommandLineApplication
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/27/16
 */
public class EventsApplicationRuntime extends ApplicationRuntimeBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(EventsApplicationRuntime.class);

    public static final int QUEUE_SIZE = 1000000;

    public static final StringOption INPUT_FORMAT_OPTION = new StringOption('i', "input-format");
    public static final StringOption INPUT_FORMAT_FILE_OPTION = new StringOption("input-format-file");

    //
    // Configure the application to simply drop parsing errors instead of sending them to output
    //
    // @see CsvOutputFormatter#isIgnoreFaults()
    //
    public static final BooleanOption IGNORE_FAULTS_OPTION = new BooleanOption("ignore-faults");

    //
    // If present, the application discards all events preceding the value of the option FROM_OPTION and does not
    // send them to the command.
    //
    public static final TimestampOption FROM_OPTION = new TimestampOption("from");

    //
    // If present, the application discards all events succeeding the value of the option FROM_OPTION and does not
    // send them to the command.
    //
    public static final TimestampOption TO_OPTION = new TimestampOption("to");

    static {

        //
        // establish option equivalency
        //

        INPUT_FORMAT_OPTION.addEquivalentOption(INPUT_FORMAT_FILE_OPTION);
    }

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private InputStreamInitiator initiator;
    private EventProcessor parser;

    // may be null if there are no filtering options
    private EventProcessor filter;

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

        return new HashSet<>((Arrays.asList(
                IGNORE_FAULTS_OPTION,
                FROM_OPTION,
                TO_OPTION)));
    }

    @Override
    public void init(Configuration configuration) throws UserErrorException {

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
                new ByteToLineEventConverter(),
                new ArrayBlockingQueue<>(QUEUE_SIZE));

        parser = new EventProcessor(
                "Input Event Stream Parser",
                initiator.getOutputQueue(),
                new LineStreamParser(lineParser),
                new ArrayBlockingQueue<>(QUEUE_SIZE));

        //
        // if there are filtering options, create and wire a filter, otherwise connect the parser directly
        // into the terminator
        //
        ProcessingLogic eventFilter = EventFilter.buildInstance(configuration);

        if (eventFilter != null) {

            filter = new EventProcessor(
                    "Event Filter",
                    parser.getOutputQueue(),
                    eventFilter,
                    new ArrayBlockingQueue<>(QUEUE_SIZE));
        }

        terminator = new OutputStreamTerminator(
                "Output Writer",
                null,
                new CsvOutputFormatter(),
                System.out);

        endOfStream = new CountDownLatch(1);
        terminator.addEndOfStreamListener(endOfStream::countDown);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public OutputStreamTerminator getTerminator() {
        return terminator;
    }

    /**
     * @return the last event processor from the pipeline, which is the component whose output queue must be wired
     * into the terminator.
     */
    public EventProcessor getLastEventProcessor() {

        if (filter != null) {
            return filter;
        }

        return parser;
    }

    public void start() throws Exception {

        initiator.start();
        parser.start();

        if (filter != null) {
            filter.start();
        }

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

    // Package protected static ----------------------------------------------------------------------------------------

    /**
     * The input stream format specification can be specified on command line (-i|--input-format) or in a file
     * (--input-format-file). This method reconciles all these possibilities and performs sanity checks.
     * @throws UserErrorException
     */
    static String getInputFormatSpecification(Configuration configuration) throws UserErrorException {

        String inputFormatSpec;

        StringOption inputFormat = (StringOption)configuration.getGlobalOption(INPUT_FORMAT_OPTION);
        StringOption inputFormatFile = (StringOption)configuration.getGlobalOption(INPUT_FORMAT_FILE_OPTION);

        if (inputFormat != null) {

            inputFormatSpec = inputFormat.getString();
            log.debug("format specification on command line: " + inputFormatSpec);

            if (inputFormatFile != null) {
                throw new UserErrorException(
                        "both " + INPUT_FORMAT_OPTION.getLabel() + " and " + INPUT_FORMAT_FILE_OPTION.getLabel() + " are specified, remove one");
            }

            return inputFormatSpec;
        }

        //
        // try --input-format-file
        //

        if (inputFormatFile == null) {

            throw new UserErrorException(
                    "input format not specified, use " + INPUT_FORMAT_OPTION.getLabel() + " or " + INPUT_FORMAT_FILE_OPTION.getLabel());
        }

        String fileName = inputFormatFile.getValue();
        File file =  new File(fileName);
        if (!file.isFile() || !file.canRead()) {
            throw new UserErrorException("file " + fileName + " does not exist or cannot be read");
        }

        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader(file));
            inputFormatSpec = br.readLine();
            log.debug("format specification loaded from file " + fileName + ": " + inputFormatSpec);
            return inputFormatSpec;
        }
        catch (Exception e) {

            throw new UserErrorException("failed to read file " + file, e);
        }
        finally {

            if (br != null) {

                try {
                    br.close();
                }
                catch(Exception e) {
                    log.warn("failed to close buffered reader", e);
                }
            }
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    /**
     * @return guaranteed not null instance.
     *
     * @throws UserErrorException if we cannot figure out what line parser to use.
     */
    private LineParser figureOutParserTypeBasedOnInputFormatString(Configuration configuration)
            throws UserErrorException {

        String inputFormatSpec = getInputFormatSpecification(configuration);

        LineParser lineParser = LineParserFactory.getInstance(inputFormatSpec);

        if (lineParser == null) {
            throw new UserErrorException(
                    "no known parser knows how to interpret the format string \"" + inputFormatSpec + "\"");
        }

        return lineParser;
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
