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
import io.novaordis.events.httpd.HttpEvent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A "collector" of state related to a business scenario. It is always created "empty" and updated with successive
 * requests, changing its state depending on the request state.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class BusinessScenario {

    // Constants -------------------------------------------------------------------------------------------------------

    // If this header is encountered, it means the containing HTTP request is the first HTTP request of a business
    // scenario of the type mentioned as the header value
    public static final String BUSINESS_SCENARIO_START_MARKER_HEADER_NAME = "Business-Scenario-Start-Marker";

    // If this header is encountered, it means the containing HTTP request is the last HTTP request of a business
    // scenario of the type mentioned as the header value; all HTTP requests between the start and stop marker
    // belong to the business scenario
    public static final String BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME = "Business-Scenario-Stop-Marker";

    // Static ----------------------------------------------------------------------------------------------------------

    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    private static long getNextId() {

        return ID_GENERATOR.getAndIncrement();
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private long id;

    //
    // the business scenario type - the value of the business scenario start marker header that starts this scenario
    //
    private String type;

    //
    // the timestamp of the moment the first request of the scenario enters the server
    //
    private long beginTimestamp;

    //
    // the timestamp of the moment the last request exists the scenario, 0 if the scenario is still active
    //
    private long endTimestamp;

    //
    // the sum of all individual HTTP requests duration
    //
    private long duration;

    //
    // the number of requests comprising this scenario
    //
    private int requestCount;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * Note that after the creation of the BusinessScenario instance, you will always need to invoke update() for the
     * first request to initialize internal statistics.
     */
    public BusinessScenario() {

        this.id = getNextId();
        this.endTimestamp = 0L;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    /**
     * Update the current business scenario's statistics with this request. Close the business scenario if appropriate.
     *
     * @see BusinessScenario#isClosed()
     *
     * @exception BusinessScenarioException checked exception that does not stop processing, but it is turned into
     *            a fault by the upper layer
     * @exception UserErrorException if the lower layers encountered a problem that stops us from processing the
     *  event stream (most likely because trying to produce further results won't make sense). Example: if the HTTP
     *  request does not belong to the current session, etc. In this case, the process must exit with a user-readable
     *  error.
     *
     * @return true is this business scenario instance is "closed" and another
     */
    public boolean update(HttpEvent event) throws BusinessScenarioException, UserErrorException {

        if (isClosed()) {
            throw new IllegalStateException(this + " already closed, cannot be updated with " + event);
        }

        String startMarker = event.getRequestHeader(BUSINESS_SCENARIO_START_MARKER_HEADER_NAME);

        if (startMarker != null) {

            if (beginTimestamp > 0) {

                //
                // start marker arrived before a end marker
                //

                throw new UserErrorException(
                        "a start marker " + event + " arrived on the already opened scenario " + this);
            }

            //
            // we "start" the scenario
            //
            type = startMarker;
            beginTimestamp = event.getTimestamp();
        }
        else {

            //
            // can't allow regular requests for a non-active scenario
            //

            if (beginTimestamp <= 0) {

                throw new BusinessScenarioException("there is no active business scenario for " + event);
            }
        }

        requestCount++;
        Long requestDuration = event.getRequestDuration();
        duration += (requestDuration == null ? 0 : requestDuration);

        String stopMarker = event.getRequestHeader(BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        if (stopMarker != null) {

            //
            // this is the last request of the scenario, make sure the stop marker is either empty string or it
            // coincides with the scenario type
            //

            stopMarker = stopMarker.trim();

            if (stopMarker.length() != 0 && !stopMarker.equals(type)) {
                throw new UserErrorException(event +
                        " ends a different scenario type (" + stopMarker + ") than the current one (" + type + ")");
            }

            endTimestamp = event.getTimestamp() + (requestDuration == null ? 0 : requestDuration);
        }

        if (requestDuration == null) {
            throw new BusinessScenarioException(event + " does not have request duration information");
        }

        return isClosed();
    }

    /**
     * @return true if this business scenario was updated with the last HTTP request from the sequence - the one
     * that contains the proper BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME header.
     */
    public boolean isClosed() {

        return endTimestamp != 0L;
    }

    /**
     * The timestamp of the moment the first request of the scenario enters the server.
     */
    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    /**
     * @return if true, this business scenario has seen the start marker and can be updated with new requests.
     */
    public boolean isActive() {

        return beginTimestamp > 0 && !isClosed();
    }

    /**
     * The timestamp of the moment the last request exists the scenario, 0 if the scenario is still active.
    */
    public long getEndTimestamp() {
        return endTimestamp;
    }

    /**
     * The sum of all individual HTTP requests duration, in the measure unit logs were generated with.
     */
    public long getDuration() {
        return duration;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public BusinessScenarioEvent toEvent() {

        BusinessScenarioEvent bse = new BusinessScenarioEvent(beginTimestamp);
        bse.setLongProperty(BusinessScenarioEvent.DURATION, duration);
        bse.setIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT, requestCount);
        bse.setStringProperty(BusinessScenarioEvent.TYPE, type);
        return bse;
    }

    @Override
    public String toString() {

        return "BusinessScenario[" + BusinessScenarioCommand.formatTimestamp(getBeginTimestamp()) +
                "][" + getId() + "](" + getType() + ")";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * For internal class use and testing only.
     *
     * @param endTimestamp a value larger than 0L.
     *
     */
    void close(long endTimestamp) {

        if (endTimestamp <= 0L) {

            throw new IllegalArgumentException(
                    "cannot close a business scenario with a zero or negative timestamp: " + endTimestamp);
        }

        this.endTimestamp = endTimestamp;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
