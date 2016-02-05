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

package io.novaordis.esa.experimental;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.command.CommandBase;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.esa.clad.EventsApplicationRuntime;
import io.novaordis.esa.clad.OutputFormatter;
import io.novaordis.esa.core.event.EndOfStreamEvent;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.FaultEvent;
import io.novaordis.esa.core.event.MapProperty;
import io.novaordis.esa.logs.httpd.HttpEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class PerfCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(PerfCommand.class);

    public static final String JSESSIONID = "JSESSIONID";
    public static final String MARKER_REQUEST_HEADER_NAME = "NovaOrdis-Request-Group-Marker";
    public static final String START_BUSINESS_SCENARIO_MARKER = "start";
    public static final String STOP_BUSINESS_SCENARIO_MARKER = "stop";

    // Static ----------------------------------------------------------------------------------------------------------

    // Package Protected Static ----------------------------------------------------------------------------------------

    /**
     * @return the business scenario marker if it finds one or null otherwise
     */
    static String getMarker(HttpEvent event) {

        if (event == null) {
            return null;
        }

        MapProperty mp = event.getMapProperty(HttpEvent.REQUEST_HEADERS);

        if (mp == null) {
            return null;
        }

        Map<String, Object> map = mp.getMap();
        return (String)map.get(PerfCommand.MARKER_REQUEST_HEADER_NAME);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> terminatorQueue;

    // JSESSIONID - context
    private Map<String, UserContext> userContexts;


    // Constructors ----------------------------------------------------------------------------------------------------

    public PerfCommand() {

        terminatorQueue = new ArrayBlockingQueue<>(100);
        userContexts = new HashMap<>();
    }

    // CommandBase overrides -------------------------------------------------------------------------------------------

    @Override
    public void execute(Configuration configuration, ApplicationRuntime r) throws Exception {

        EventsApplicationRuntime runtime = (EventsApplicationRuntime)r;
        runtime.getTerminator().setInputQueue(terminatorQueue);
        ((OutputFormatter)runtime.getTerminator().getConversionLogic()).setFormat("timestamp, request-count, total-processing-time");
        runtime.start();

        BlockingQueue<Event> inputQueue = runtime.getOutputQueue();

        for(;;) {

            Event event = inputQueue.take();
            if (event == null || event instanceof EndOfStreamEvent) {
                break;
            }

            if (event instanceof FaultEvent) {
                terminatorQueue.put(event);
            }

            collectBusinessScenarioStatistics(event);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public BlockingQueue<Event> getOutputQueue() {

        return terminatorQueue;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void collectBusinessScenarioStatistics(Event event) throws InterruptedException {

        if (!(event instanceof HttpEvent)) {
            throw new IllegalStateException("not a HttpEvent: " + event);
        }

        HttpEvent httpEvent = (HttpEvent)event;

        MapProperty cookies = (MapProperty)httpEvent.getProperty(HttpEvent.COOKIES);

        UserContext uc = null;

        if (cookies != null) {

            String jSessionIdValue = (String) cookies.getMap().get("JSESSIONID");

            if (jSessionIdValue != null) {

                uc = userContexts.get(jSessionIdValue);
                if (uc == null) {
                    uc = new UserContext();
                    userContexts.put(jSessionIdValue, uc);
                }
            }
        }

        if (uc == null) {

            //
            // this request does not belong to any user
            //

            return;
        }

        BusinessScenario bs = uc.getCurrentBusinessScenario();
        String marker = getMarker(httpEvent);

        if (bs != null) {

            if (START_BUSINESS_SCENARIO_MARKER.equals(marker)) {
                throw new IllegalStateException(
                        "got the start business scenario marker while a business scenario is active");
            }

            // the current request belongs to a business scenario
            bs.update(httpEvent);

            if (STOP_BUSINESS_SCENARIO_MARKER.equals(marker)) {

                //
                // this is the last request of this scenario, wrap it up and send statistics downstream
                //

                BusinessScenarioEvent bsEvent = bs.toEvent();
                uc.clear();
                getOutputQueue().put(bsEvent);
            }
        }
        else {

            // no current business scenario

            if (START_BUSINESS_SCENARIO_MARKER.equals(marker)) {

                uc.startNewBusinessScenario(httpEvent);
                return;
            }
            else if (STOP_BUSINESS_SCENARIO_MARKER.equals(marker)) {
                throw new IllegalStateException(
                        "got the stop business scenario marker while there is no active business scenario");
            }


            log.debug("ignored request " + httpEvent);

            //
            // ignored request
            //
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------


    // Inner classes ---------------------------------------------------------------------------------------------------

}
