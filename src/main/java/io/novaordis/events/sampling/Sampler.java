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

package io.novaordis.events.sampling;

import io.novaordis.events.core.ProcessingLogicBase;
import io.novaordis.events.core.event.DoubleProperty;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.GenericTimedEvent;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.TimedEvent;

import java.util.List;

/**
 * Forwards events.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class Sampler extends ProcessingLogicBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private long sampleSizeMs;
    private String propertyName;

    // beginning of the current sample, bounded to second
    private long beginning = -1;

    // beginning of the current sample, bounded to second
    private long end = -1;

    private long currentInputEventCounter = 0;
    private double currentAggregatedValue = 0;
    private double lastAverageValue = -1d;

    // Constructors ----------------------------------------------------------------------------------------------------

    public Sampler(long sampleSizeMs, String propertyName) {
        this.sampleSizeMs = sampleSizeMs;
        this.propertyName = propertyName;
    }

    // ProcessingLogicBase implementation ------------------------------------------------------------------------------

    @Override
    protected Event processInternal(Event e) throws Exception {

        // TODO n7k4S
        // we may produce more than one output event per input event, so we add n-1 events directly into the event
        // buffer and we return the last one. This is awkward, we need to revisit the ProcessingLogicBase API.

        if (!(e instanceof TimedEvent)) {
            throw new IllegalArgumentException("we expect only TimedEvents and we got " + e);
        }

        interpolate(getEventBuffer(), (TimedEvent)e);
        return null;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void interpolate(List<Event> outputEventBuffer, TimedEvent e) throws Exception {

        long time = e.getTimestamp();

        Property p = e.getProperty(propertyName);

        if (p == null) {
            throw new IllegalArgumentException("property \"" + propertyName + "\" not found on " + e);
        }

        Object value = p.getValue();

        if (!(value instanceof Number)) {

            throw new IllegalArgumentException("property \"" + propertyName + "\" is not a Number on  " + e + ", but " + value);
        }

        Number n = (Number)value;
        double d = n.doubleValue();

        if (beginning == -1) {
            //
            // first sample, initialize
            //
            beginning = (time / 1000) * 1000;
            end = beginning + sampleSizeMs;
            currentInputEventCounter++;
            currentAggregatedValue += d;
        }
        else if (beginning <= time && time < end) {
            // within the same sample
            currentInputEventCounter++;
            currentAggregatedValue += d;
        }
        else {

            //
            // we need to start a new sample, so send the current sample down the pipeline
            //

            GenericTimedEvent oe = new GenericTimedEvent(beginning);
            oe.setProperty(new LongProperty("input-event-count", currentInputEventCounter));

            if (currentInputEventCounter > 0) {
                lastAverageValue = currentAggregatedValue / currentInputEventCounter;
                oe.setProperty(new DoubleProperty("average-per-sample", lastAverageValue));
            }
            outputEventBuffer.add(oe);
            //
            // save the average time in case we need it for extrapolation
            //
            currentInputEventCounter = 0;
            currentAggregatedValue = 0;

            //
            // start a new sample
            //

            if (time - end < sampleSizeMs) {
                // the very next sample
                beginning = (time / 1000) * 1000;
                end = beginning + sampleSizeMs;
                currentInputEventCounter++;
                currentAggregatedValue += d;
            }
            else {

                // there are samples in between there are no requests for

                // figure out how many samples we skipped

                long skipped = (time - end) / (sampleSizeMs);

                //
                // generate the missing "empty" samples
                //

                for(int i = 0; i < skipped; i ++) {
                    GenericTimedEvent ioe = new GenericTimedEvent(end + i * sampleSizeMs);
                    ioe.setProperty(new LongProperty("input-event-count", 0L));
                    ioe.setProperty(new DoubleProperty("average-per-sample", lastAverageValue));
                    outputEventBuffer.add(ioe);
                }

                beginning = (time / 1000) * 1000;
                end = beginning + sampleSizeMs;
                currentInputEventCounter++;
                currentAggregatedValue += d;
            }
        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
