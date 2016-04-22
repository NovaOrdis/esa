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

package io.novaordis.esa.extensions.bscenarios;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.command.CommandBase;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.esa.clad.EventsApplicationRuntime;
import io.novaordis.esa.core.OutputFormatter;
import io.novaordis.esa.core.Terminator;
import io.novaordis.esa.core.event.EndOfStreamEvent;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.FaultEvent;
import io.novaordis.esa.httpd.HttpEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 3/1/16
 */
public class BusinessScenarioCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BusinessScenarioCommand.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> terminatorQueue;

    private HttpSessionFactory httpSessionFactory;

    // JSESSIONID - HttpSession instance
    private Map<String, HttpSession> sessions;

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

        log.debug(this + " created");
    }

    // CommandBase overrides -------------------------------------------------------------------------------------------

    @Override
    public void execute(Configuration configuration, ApplicationRuntime r) throws Exception {

        EventsApplicationRuntime runtime = (EventsApplicationRuntime)r;

        Terminator terminator = runtime.getTerminator();

        if (terminator != null) {

            terminator.setInputQueue(terminatorQueue);

            ((OutputFormatter) terminator.getConversionLogic()).setOutputFormat(
                    "timestamp, request-count, total-processing-time");
        }

        runtime.start();

        BlockingQueue<Event> httpRequestQueue = runtime.getEventProcessor().getOutputQueue();

        while(true) {

            Event event = httpRequestQueue.take();

            if (event == null || event instanceof EndOfStreamEvent) {
                break;
            }
            else if (event instanceof FaultEvent) {
                terminatorQueue.put(event);
            }
            else if (!(event instanceof HttpEvent)) {
                terminatorQueue.put(new FaultEvent("not a HttpEvent: " + event));
            }
            else {

                Event result = process((HttpEvent)event);

                if (result != null) {

                    // null is possible, it means we're in flight while processing a business scenario
                    terminatorQueue.put(result);
                }
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
     */
    Event process(HttpEvent event) {

        String jSessionId = event.getCookie(HttpSession.JSESSIONID_COOKIE_KEY);

        if (jSessionId == null) {
            return new FaultEvent(
                    "HTTP request " + event + " does not carry a \"" + HttpSession.JSESSIONID_COOKIE_KEY + "\" cookie");
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
