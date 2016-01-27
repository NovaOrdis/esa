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

import io.novaordis.esa.core.ClosedException;
import io.novaordis.esa.core.ProcessingLogic;
import io.novaordis.esa.core.event.ContainerEvent;
import io.novaordis.esa.core.event.EndOfStreamEvent;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.logs.httpd.HttpdLogLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Forwards events.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class ExperimentalLogic implements ProcessingLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ExperimentalLogic.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean closed;
    private List<Event> buffer;

    // Constructors ----------------------------------------------------------------------------------------------------

    public ExperimentalLogic() {

        this.buffer = new ArrayList<>();
    }

    // ProcessingLogic implements --------------------------------------------------------------------------------------

    @Override
    public boolean process(Event inputEvent) throws ClosedException {

        if (closed) {
            throw new ClosedException(this + " is closed");
        }

        if (inputEvent instanceof EndOfStreamEvent) {
            closed = true;
            return false;
        }
        else {

            ContainerEvent ce = (ContainerEvent)inputEvent;

            try {
                interpolate((HttpdLogLine) ce.get());
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
                buffer.add(new EndOfStreamEvent());
                return true;
            }

            //buffer.add(inputEvent);
        }

        return false;
    }

    @Override
    public List<Event> getEvents() {

        List<Event> result = new ArrayList<>(buffer);
        buffer.clear();
        return result;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // in milliseconds
    long sampleSize = 5000L;

    // beginning of the current sample, bounded to second
    long beginning = -1;

    // beginning of the current sample, bounded to second
    long end = -1;

    long currentRequestCounter = 0;
    long currentAggregatedTime = 0;
    double lastAverageTime = -1;

    private void interpolate(HttpdLogLine line) throws Exception {

        long time = line.timestamp.getTime();

        Long rt = line.getRequestProcessingTimeMs();

        if (rt == null) {
            throw new Exception("request with no time");
        }

        if (beginning == -1) {
            // first sample, initialize

            beginning = (time / 1000) * 1000;
            end = beginning + sampleSize;
            currentRequestCounter ++;
            currentAggregatedTime += rt;
        }
        else if (beginning <= time && time < end) {
            // within the same sample
            currentRequestCounter ++;
            currentAggregatedTime += rt;
        }
        else {

            //
            // we need to start a new sample, so send the current sample down the pipeline
            //

            SampleEvent e = new SampleEvent(beginning);
            e.setCount(currentRequestCounter);
            if (currentRequestCounter > 0) {
                e.setAverageTime(((double) currentAggregatedTime) / currentRequestCounter);
            }
            buffer.add(e);
            //
            // save the average time in case we need it for extrapolation
            //
            lastAverageTime = e.getAverageTime();

            currentRequestCounter = 0;
            currentAggregatedTime = 0;

            //
            // start a new sample
            //

            if (time - end <  sampleSize) {
                // the very next sample
                beginning = (time / 1000) * 1000;
                end = beginning + sampleSize;
                currentRequestCounter ++;
                currentAggregatedTime += rt;
            }
            else {

                // there are samples in between there are no requests for

                // figure out how many samples we skipped

                long skipped = (time - end) / (sampleSize);

                //
                // generate the missing "empty" samples
                //

                for(int i = 0; i < skipped; i ++) {
                    SampleEvent se = new SampleEvent(end + i * sampleSize);
                    se.setCount(0);
                    se.setAverageTime(lastAverageTime);
                    buffer.add(se);
                }

                beginning = (time / 1000) * 1000;
                end = beginning + sampleSize ;
                currentRequestCounter ++;
                currentAggregatedTime += rt;
            }

        }

    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
