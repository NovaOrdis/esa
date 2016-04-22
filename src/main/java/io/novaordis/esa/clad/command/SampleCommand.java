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
import io.novaordis.clad.option.Option;
import io.novaordis.clad.option.StringOption;
import io.novaordis.esa.clad.EventsApplicationRuntime;
import io.novaordis.esa.core.EventProcessor;
import io.novaordis.esa.sampling.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/26/16
 */
@SuppressWarnings("unused")
public class SampleCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(SampleCommand.class);

    // string because we allow for measure unit to be optionally specified with the value (eg --sampling-interval=10s)
    public static final StringOption SAMPLING_INTERVAL_OPTION = new StringOption("sampling-interval");
    public static final StringOption SAMPLING_FIELD_OPTION = new StringOption("field-name");

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private long samplingIntervalSecs;
    private String samplingFieldName;

    // Constructors ----------------------------------------------------------------------------------------------------

    // Command implementation ------------------------------------------------------------------------------------------

    @Override
    public Set<Option> requiredOptions() {
        return new HashSet<>(Arrays.asList(SAMPLING_INTERVAL_OPTION, SAMPLING_FIELD_OPTION));
    }

    @Override
    public void execute(Configuration configuration, ApplicationRuntime applicationRuntime) throws Exception {

        log.debug("executing " + this);

        setSamplingInterval();
        setSamplingFieldName();

        EventsApplicationRuntime eventsApplicationRuntime = (EventsApplicationRuntime)applicationRuntime;

        try {

            EventProcessor sampler = new EventProcessor(
                    "Sampler",
                    eventsApplicationRuntime.getTerminator().getInputQueue(),
                    new Sampler(samplingIntervalSecs * 1000L, samplingFieldName),
                    new ArrayBlockingQueue<>(EventsApplicationRuntime.QUEUE_SIZE));

            eventsApplicationRuntime.getTerminator().setInputQueue(sampler.getOutputQueue());
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

    private void setSamplingInterval() throws UserErrorException {

        StringOption samplingIntervalOption = null;
        for(Option o: getOptions()) {

            if (SAMPLING_INTERVAL_OPTION.equals(o)) {
                samplingIntervalOption = (StringOption)o;
                break;
            }
        }

        if (samplingIntervalOption == null) {
            throw new UserErrorException("missing required " + SAMPLING_INTERVAL_OPTION);
        }

        String s = samplingIntervalOption.getValue();

        //
        // get the digits and interpret them as seconds
        //

        String val = "";
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                val += c;
            }
            else {
                break;
            }
        }

        samplingIntervalSecs = Integer.parseInt(val);
    }

    private void setSamplingFieldName() throws UserErrorException {

        StringOption samplingFieldOption = null;
        for(Option o: getOptions()) {

            if (SAMPLING_FIELD_OPTION.equals(o)) {
                samplingFieldOption = (StringOption)o;
                break;
            }
        }

        if (samplingFieldOption == null) {
            throw new UserErrorException("missing required " + SAMPLING_FIELD_OPTION);
        }

        samplingFieldName = samplingFieldOption.getValue();
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
