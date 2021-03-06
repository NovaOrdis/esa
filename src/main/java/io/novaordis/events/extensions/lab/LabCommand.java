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

package io.novaordis.events.extensions.lab;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.command.CommandBase;
import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.clad.EventsApplicationRuntime;
import io.novaordis.events.httpd.HttpEvent;
import io.novaordis.utilities.UserErrorException;

import java.util.concurrent.BlockingQueue;

/**
 * The command gives access to the event processor output stream, allowing for programmatic experiments.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class LabCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Command implementation ------------------------------------------------------------------------------------------

    @Override
    public void execute(ApplicationRuntime r) throws Exception {

        EventsApplicationRuntime runtime = (EventsApplicationRuntime)r;
        runtime.getTerminator().disable();
        BlockingQueue<Event> queue = runtime.getLastEventProcessor().getOutputQueue();

        runtime.start();

        while(true) {

            Event e = queue.take();

            if (e == null || e instanceof EndOfStreamEvent) {
                break;
            }

            onHttpEvent((HttpEvent)e);
        }
    }



    private long previousTimestamp = Long.MIN_VALUE;


    public void onHttpEvent(HttpEvent e) throws Exception {

        long currentTimestamp = e.getTime();

        if (currentTimestamp < previousTimestamp) {
            throw new UserErrorException("found misplaced event " + e);
        }

        previousTimestamp = currentTimestamp;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
