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

    private BusinessScenarioState state;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * Note that after the creation of the BusinessScenario instance, you will always need to invoke update() for the
     * first request to initialize internal statistics.
     */
    public BusinessScenario() {

        this.id = getNextId();
        this.state = BusinessScenarioState.NEW;
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
     * @return true is this business scenario instance was closed by this update.
     */
    public boolean update(HttpEvent event) throws BusinessScenarioException, UserErrorException {

        if (isClosed()) {
            throw new IllegalStateException(this + " already closed, cannot be updated with " + event);
        }

        String startMarker = event.getRequestHeader(BUSINESS_SCENARIO_START_MARKER_HEADER_NAME);

        if (startMarker != null) {

            if (state.equals(BusinessScenarioState.ACTIVE)) {

                //
                // start marker arrived before a end marker
                //
                // We've seen situations where the end marker is not present for a small percentage of scenarios;
                // we don't know why, yet, it could be a load generator artifact. For the time being, we create
                // a "problematic" business scenario, with an note.
                //

                //
                // "close" the business scenario
                //
                endTimestamp = event.getTimestamp();
                setState(BusinessScenarioState.CLOSED_BY_START_MARKER);
                return true;
            }

            //
            // we "start" the scenario
            //
            setType(startMarker);
            setBeginTimestamp(event.getTimestamp());
            setState(BusinessScenarioState.ACTIVE);
        }
        else {

            //
            // ignore the request if the scenario is not active. Throwing a BusinessScenarioException will cause
            // a Fault to be sent down the pipeline, but it won't interrupt processing
            //

            if (beginTimestamp <= 0) {

                throw new BusinessScenarioException(
                        BusinessScenarioFaultType.NO_ACTIVE_BUSINESS_SCENARIO,
                        "there is no active business scenario for " + event);
            }
        }

        Long requestDuration = event.getRequestDuration();
        updateCounters(requestDuration);

        String stopMarker = event.getRequestHeader(BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        if (stopMarker != null) {

            //
            // this is the last request of the scenario, make sure the stop marker is either empty string or it
            // coincides with the scenario type
            //

            stopMarker = stopMarker.trim();

            if (stopMarker.length() != 0 && !stopMarker.equals(type)) {

                setState(BusinessScenarioState.FAULT);
                throw new UserErrorException(event +
                        " ends a different scenario type (" + stopMarker + ") than the current one (" + type + ")");
            }

            endTimestamp = event.getTimestamp() + (requestDuration == null ? 0 : requestDuration);
            setState(BusinessScenarioState.CLOSED);
        }

        if (requestDuration == null) {
            throw new BusinessScenarioException(
                    BusinessScenarioFaultType.NO_REQUEST_DURATION_INFO,
                    event + " does not have request duration information");
        }

        return isClosed();
    }

    /**
     * @return true if this business scenario was updated with the last HTTP request from the sequence - the one
     * that contains the proper BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME header.
     */
    public boolean isClosed() {

        return !state.equals(BusinessScenarioState.NEW) && !state.equals(BusinessScenarioState.ACTIVE);
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
        bse.setLongProperty(BusinessScenarioEvent.ID, getId());
        bse.setLongProperty(BusinessScenarioEvent.DURATION, duration);
        bse.setIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT, requestCount);
        bse.setStringProperty(BusinessScenarioEvent.TYPE, type);
        bse.setStringProperty(BusinessScenarioEvent.STATE, getState().name());
        return bse;
    }

    public BusinessScenarioState getState() {
        return state;
    }

    public void setState(BusinessScenarioState state) {
        this.state = state;
    }

    @Override
    public String toString() {

        return "BusinessScenario[" + BusinessScenarioCommand.formatTimestamp(getBeginTimestamp()) + "][" +
                getId() + "](" + getType() + ")";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * For internal class use and testing only.
     */
    void close() {

        setState(BusinessScenarioState.CLOSED);
    }

    void setType(String type) {

        //
        // BusinessScenario type can be set only once
        //

        if (this.type != null && !this.type.equals(type)) {
            throw new IllegalStateException(this + "'s type can be set only once");
        }
        this.type = type;
    }

    void setBeginTimestamp(long ts) {
        this.beginTimestamp = ts;
    }

    /**
     * @param requestDuration may be null
     */
    void updateCounters(Long requestDuration) {
        requestCount ++;
        duration += (requestDuration == null ? 0 : requestDuration);
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
