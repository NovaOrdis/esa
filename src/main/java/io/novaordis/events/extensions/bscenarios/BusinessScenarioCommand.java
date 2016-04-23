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

package io.novaordis.events.extensions.bscenarios;

import io.novaordis.clad.UserErrorException;
import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.command.CommandBase;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.BooleanOption;
import io.novaordis.clad.option.Option;
import io.novaordis.events.clad.EventsApplicationRuntime;
import io.novaordis.events.core.OutputFormatter;
import io.novaordis.events.core.Terminator;
import io.novaordis.events.core.event.EndOfStreamEvent;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.FaultEvent;
import io.novaordis.events.httpd.FormatString;
import io.novaordis.events.httpd.HttpEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 3/1/16
 */
public class BusinessScenarioCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BusinessScenarioCommand.class);

    private static final BooleanOption IGNORE_FAULTS_OPTION = new BooleanOption("ignore-faults");

    // Static ----------------------------------------------------------------------------------------------------------

    public static String formatTimestamp(long timestamp) {

        //
        // currently we use the standard httpd timestamp format, but TODO in the future we must generalize this and
        // be able to use the same time format used in the input log - to ease searching.
        //

        return FormatString.TIMESTAMP_FORMAT.format(timestamp);

    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> terminatorQueue;

    private HttpSessionFactory httpSessionFactory;

    // JSESSIONID - HttpSession instance
    private Map<String, HttpSession> sessions;

    private boolean ignoreFaults;

    // Constructors ----------------------------------------------------------------------------------------------------

    public BusinessScenarioCommand() {

        terminatorQueue = new ArrayBlockingQueue<>(100);
        sessions = new HashMap<>();
        //noinspection Convert2Lambda,Anonymous2MethodRef
        httpSessionFactory = new HttpSessionFactory() {
            @Override
            public HttpSession create() {
                return new HttpSession();
            }
        };

        ignoreFaults = false;

        log.debug(this + " created");
    }

    // CommandBase overrides -------------------------------------------------------------------------------------------

    @Override
    public Set<Option> optionalOptions() {

        return new HashSet<>(Collections.singletonList(IGNORE_FAULTS_OPTION));
    }

    /**
     * Set our local variables
     */
    @Override
    public void configure(int from, List<String> commandLineArguments) throws Exception {

        super.configure(from, commandLineArguments);

        BooleanOption o = (BooleanOption)getOption(IGNORE_FAULTS_OPTION);

        if (o != null) {
            ignoreFaults = o.getValue();
        }

        log.debug(this + "'s ignoreFaults set to " + ignoreFaults);
    }

    @Override
    public void execute(Configuration configuration, ApplicationRuntime r) throws Exception {

        EventsApplicationRuntime runtime = (EventsApplicationRuntime)r;

        Terminator terminator = runtime.getTerminator();

        if (terminator != null) {

            terminator.setInputQueue(terminatorQueue);

            String propertiesToDisplay =
                    "timestamp, " +
                    BusinessScenarioEvent.REQUEST_COUNT + ", " +
                    BusinessScenarioEvent.DURATION;

            ((OutputFormatter) terminator.getConversionLogic()).setOutputFormat(propertiesToDisplay);
        }

        runtime.start();

        BlockingQueue<Event> httpRequestQueue = runtime.getEventProcessor().getOutputQueue();

        while(true) {

            Event event = httpRequestQueue.take();

            if (event == null || event instanceof EndOfStreamEvent) {
                break;
            }

            Event resultEvent;

            if (event instanceof FaultEvent) {

                resultEvent = event;
            }
            else if (!(event instanceof HttpEvent)) {

                //
                // this is a programming error, stop processing right away
                //
                throw new IllegalArgumentException(this + " got an " + event + " while it is only expecting HttpEvents");
            }
            else {

                resultEvent = process((HttpEvent)event);
            }

            // the result event can be null if multiple individual HTTP request are "consolidated" into a larger
            // in-flight business scenario event

            if (resultEvent != null) {

                if (ignoreFaults && resultEvent instanceof FaultEvent) {

                    //
                    // we are configured to not display faults
                    //

                    log.debug("ignoring " + resultEvent);
                    continue;
                }

                terminatorQueue.put(resultEvent);
            }
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * This method only exists to allow unit testing. If used, it overwrites the default factory.
     */
    public void setHttpSessionFactory(HttpSessionFactory httpSessionFactory) {
        this.httpSessionFactory = httpSessionFactory;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * The method locates the session for the specific request, and then delegates further processing to the session.
     *
     * @return a BusinessScenarioEvent, a FaultEvent or null if we're in mid-flight while processing a scenario.
     *
     * @exception UserErrorException if the lower layers encountered a problem that stops us from processing the
     *  event stream (most likely because trying to produce further results won't make sense). In this case, the process
     *  must exit with a user-readable error.
     */
    Event process(HttpEvent event) throws UserErrorException {

        String jSessionId = event.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY);

        if (jSessionId == null) {
            return new FaultEvent(
                    "HTTP request " + event + " does not carry a \"" + HttpEvent.JSESSIONID_COOKIE_KEY + "\" cookie");
        }

        HttpSession s = sessions.get(jSessionId);

        if (s == null) {
            s = httpSessionFactory.create();
            s.setJSessionId(jSessionId);
            sessions.put(jSessionId, s);
        }

        return s.processBusinessScenario(event);
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
