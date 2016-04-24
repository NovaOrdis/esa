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
import io.novaordis.events.core.event.FaultType;
import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.extensions.bscenarios.stats.FaultStats;
import io.novaordis.events.httpd.FormatString;
import io.novaordis.events.httpd.HttpEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
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
    private static final BooleanOption STATS_OPTION = new BooleanOption("stats");

    // Static ----------------------------------------------------------------------------------------------------------

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(FormatString.TIMESTAMP_FORMAT_STRING);

    public static String formatTimestamp(long timestamp) {

        //
        // currently we use the standard httpd timestamp format, but TODO in the future we must generalize this and
        // be able to use the same time format used in the input log - to ease searching.
        //
        // TODO implement a better concurrent access than synchronization
        //

        if (timestamp <= 0) {
            return "-";
        }

        synchronized (TIMESTAMP_FORMAT) {
            return TIMESTAMP_FORMAT.format(timestamp);
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> terminatorQueue;

    private HttpSessionFactory httpSessionFactory;

    // JSESSIONID - HttpSession instance
    private Map<String, HttpSession> sessions;

    private boolean ignoreFaults;
    private boolean statsOnly;

    private long httpEventCount;

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
        statsOnly = false;

        log.debug(this + " created");
    }

    // CommandBase overrides -------------------------------------------------------------------------------------------

    @Override
    public Set<Option> optionalOptions() {

        return new HashSet<>(Arrays.asList(IGNORE_FAULTS_OPTION, STATS_OPTION));
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

        o = (BooleanOption)getOption(STATS_OPTION);

        if (o != null) {
            statsOnly = o.getValue();
        }

        log.debug(this + "'s stats set to " + statsOnly);
    }

    @Override
    public void execute(Configuration configuration, ApplicationRuntime r) throws Exception {

        EventsApplicationRuntime runtime = (EventsApplicationRuntime) r;

        Terminator terminator = runtime.getTerminator();

        if (terminator != null) {

            terminator.setInputQueue(terminatorQueue);

            String propertiesToDisplay =
                    "timestamp, " +
                            BusinessScenarioEvent.ID + ", " +
                            BusinessScenarioEvent.REQUEST_COUNT + ", " +
                            BusinessScenarioEvent.DURATION + ", " +
                            BusinessScenarioEvent.STATE;

            ((OutputFormatter) terminator.getConversionLogic()).setOutputFormat(propertiesToDisplay);
        }

        runtime.start();

        BlockingQueue<Event> httpRequestQueue = runtime.getEventProcessor().getOutputQueue();

        boolean incomingStreamOpen = true;

        while (incomingStreamOpen) {

            Event event = httpRequestQueue.take();

            if (event == null || event instanceof EndOfStreamEvent) {
                incomingStreamOpen = false;
            }

            List<Event> outgoing = process(event);
            handleOutgoing(outgoing);
        }

        if (statsOnly) {
            displayStatistics();
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
     * @param incoming usually a civilian, but it can also be null or EndOfStreamEvent, so the method must be able
     *                 to handle that.
     */
    List<Event> process (Event incoming) throws UserErrorException {

        if (incoming == null || incoming instanceof EndOfStreamEvent) {

            //
            // handle end-of-stream, cleanup whatever in-flight state we might have when we detect the end of the input
            // stream
            //
            return Collections.emptyList();
        }

        if (incoming instanceof FaultEvent) {

            return Collections.singletonList(incoming);
        }

        if (!(incoming instanceof HttpEvent)) {

            //
            // this is a programming error, stop processing right away
            //
            throw new IllegalArgumentException(this + " got an " + incoming + " while it is only expecting HttpEvents");

        }

        Event outgoing = processHttpEvent((HttpEvent)incoming);

        if (outgoing == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(outgoing);
    }

    /**
     * The method locates the session for the specific request, and then delegates further processing to the session.
     *
     * @return a BusinessScenarioEvent, a FaultEvent or null if we're in mid-flight while processing a scenario.
     *
     * @exception UserErrorException if the lower layers encountered a problem that stops us from processing the
     *  event stream (most likely because trying to produce further results won't make sense). In this case, the process
     *  must exit with a user-readable error.
     */
    Event processHttpEvent(HttpEvent event) throws UserErrorException {

        httpEventCount ++;

        String jSessionId = event.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY);

        if (jSessionId == null) {
            return new FaultEvent(
                    BusinessScenarioFaultType.NO_JSESSIONID_COOKIE,
                    "HTTP request " + event + " does not carry a \"" + HttpEvent.JSESSIONID_COOKIE_KEY + "\" cookie");
        }

        HttpSession s = sessions.get(jSessionId);

        if (s == null) {
            s = httpSessionFactory.create();
            s.setJSessionId(jSessionId);
            sessions.put(jSessionId, s);
        }

        return s.process(event);
    }

    void handleOutgoing(List<Event> outgoing) throws InterruptedException{

        if (statsOnly) {

            updateStatistics(outgoing);
        }
        else {

            //
            // send outgoing events downstream
            //

            for (Event o : outgoing) {

                if (o == null) {
                    continue;
                }

                if (ignoreFaults && o instanceof FaultEvent) {

                    //
                    // we are configured to not display faults
                    //

                    log.debug("ignoring " + o);
                    continue;
                }

                terminatorQueue.put(o);
            }
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private long businessScenarioCount;
    private long otherEventsCount;
    private int maxRequestsPerScenario = 0;
    private int minRequestsPerScenario = Integer.MAX_VALUE;
    private long aggregatedScenarioDuration = 0L;
    private FaultStats faultStats = new FaultStats();

    private void updateStatistics(List<Event> outgoing) {

        for(Event e: outgoing) {

            if (e == null) {
                continue;
            }

            if (e instanceof FaultEvent) {

                faultStats.update((FaultEvent) e);
            }
            else if (e instanceof BusinessScenarioEvent) {

                businessScenarioCount++;

                BusinessScenarioEvent bse = (BusinessScenarioEvent) e;

                IntegerProperty p = bse.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT);
                int requestCount = p == null ? -1 : p.getInteger();
                if (requestCount > maxRequestsPerScenario) {
                    maxRequestsPerScenario = requestCount;
                }
                if (requestCount < minRequestsPerScenario) {
                    minRequestsPerScenario = requestCount;
                }

                LongProperty lp = bse.getLongProperty(BusinessScenarioEvent.DURATION);
                if (lp != null) {
                    aggregatedScenarioDuration += lp.getLong();
                }
            }
            else {

                otherEventsCount++;
            }
        }
    }

    private void displayStatistics() {

        double requestsPerScenario =
                ((double)(httpEventCount - faultStats.getFaultCount() - otherEventsCount))/businessScenarioCount;
        double averageScenarioDuration = ((double)aggregatedScenarioDuration)/businessScenarioCount;

        System.out.printf("Counters\n");
        System.out.printf("       business scenarios: %d\n", businessScenarioCount);
        System.out.printf("                   faults: %d (%d different types)\n",
                faultStats.getFaultCount(), faultStats.getFaultTypeCount());

        Set<FaultType> faultTypes = faultStats.getFaultTypes();
        for(FaultType ft: faultTypes) {
            System.out.printf("                             %s: %d\n", ft, faultStats.getCountPerType(ft));
        }
        System.out.printf("             other events: %d\n", otherEventsCount);
        System.out.printf("            HTTP requests: %d\n", httpEventCount);
        System.out.printf("        requests/scenario: %2.2f\n", requestsPerScenario);
        System.out.printf("    max requests/scenario: %d\n", maxRequestsPerScenario);
        System.out.printf("    min requests/scenario: %d\n", minRequestsPerScenario);
        System.out.printf(" average request duration: %2.2f ms\n", averageScenarioDuration);
        System.out.printf("            HTTP sessions: %d\n", sessions.size());

        System.out.println();

        for(HttpSession s: sessions.values()) {

            BusinessScenario bs = s.getCurrentBusinessScenario();
            System.out.println(s + " has " + (bs.isActive() ? "an active" : "a non-active") + " current scenario " + bs);
        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
