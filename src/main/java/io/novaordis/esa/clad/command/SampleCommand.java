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

package io.novaordis.esa.clad.command;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.UserErrorException;
import io.novaordis.clad.command.CommandBase;
import io.novaordis.esa.clad.EventsApplicationRuntime;
import io.novaordis.esa.core.EventProcessor;
import io.novaordis.esa.experimental.ExperimentalLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/26/16
 */
@SuppressWarnings("unused")
public class SampleCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(SampleCommand.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Command implementation ------------------------------------------------------------------------------------------

    @Override
    public void execute(Configuration configuration, ApplicationRuntime applicationRuntime) throws Exception {

        log.debug("executing " + this);

        EventsApplicationRuntime eventsApplicationRuntime = (EventsApplicationRuntime)applicationRuntime;

        try {

            EventProcessor sampler = new EventProcessor(
                    "Sampler",
                    eventsApplicationRuntime.getOutputQueue(),
                    new ExperimentalLogic(),
                    new ArrayBlockingQueue<>(EventsApplicationRuntime.QUEUE_SIZE));

            eventsApplicationRuntime.connectToTerminator(sampler.getOutputQueue());

            eventsApplicationRuntime.start();

            sampler.start();

            eventsApplicationRuntime.waitForEndOfStream();
        }
        catch(Exception e) {
            throw new UserErrorException(e);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
