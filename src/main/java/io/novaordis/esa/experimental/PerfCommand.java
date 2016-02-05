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
import io.novaordis.esa.core.event.EndOfStreamEvent;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.FaultEvent;
import io.novaordis.esa.core.event.LongProperty;
import io.novaordis.esa.core.event.MapProperty;
import io.novaordis.esa.logs.httpd.HttpEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class PerfCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> terminatorQueue;
    private Map<String, String> cookies;


    // Constructors ----------------------------------------------------------------------------------------------------

    public PerfCommand() {

        terminatorQueue = new ArrayBlockingQueue<Event>(100);
        cookies = new HashMap<>();
    }

    // CommandBase overrides -------------------------------------------------------------------------------------------

    @Override
    public void execute(Configuration configuration, ApplicationRuntime r) throws Exception {

        EventsApplicationRuntime runtime = (EventsApplicationRuntime)r;
        runtime.getTerminator().setInputQueue(terminatorQueue);
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

            perfStats(event);
        }


        System.out.println("distinct users: " + cookies.size());
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void perfStats(Event event) {

        if (!(event instanceof HttpEvent)) {
            throw new IllegalStateException("not a HttpEvent: " + event);
        }

        HttpEvent httpEvent = (HttpEvent)event;

        MapProperty requests = (MapProperty)httpEvent.getProperty(HttpEvent.REQUEST_HEADERS);
        MapProperty cookies = (MapProperty)httpEvent.getProperty(HttpEvent.COOKIES);
        LongProperty processingTime = (LongProperty)httpEvent.getProperty(HttpEvent.REQUEST_PROCESSING_TIME);

        if (cookies != null) {

            String s = (String) cookies.getMap().get("JSESSIONID");

            if (s != null) {
                this.cookies.put(s, s);
            }
        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
