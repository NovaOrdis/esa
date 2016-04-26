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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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

    private static final Logger log = LoggerFactory.getLogger(BusinessScenario.class);

    // If this header is encountered, it means the containing HTTP request is the first HTTP request of a business
    // scenario of the type mentioned as the header value
    public static final String BUSINESS_SCENARIO_START_MARKER_HEADER_NAME = "Business-Scenario-Start-Marker";

    // If this header is encountered, it means the containing HTTP request is the last HTTP request of a business
    // scenario of the type mentioned as the header value; all HTTP requests between the start and stop marker
    // belong to the business scenario
    public static final String BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME = "Business-Scenario-Stop-Marker";

    // The load generator is supposed to generate an unique ID for each iteration (an iteration is defined as a "full"
    // sequence of requests the load scenario consists of; runs are usually executed in loops). Nothing essential should
    // break if the run id is not present, in the worst case we will not be able to do some types of analyses.
    public static final String BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME = "Business-Scenario-Iteration-ID";

    // Each request of a business scenario may have its own unique ID, if the scenario is instrumented as such.
    // Nothing essential should break if the run id is not present, in the worst case we will not be able to do some
    // types of analyses.
    public static final String BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME =
            "Business-Scenario-Request-Sequence-ID";

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

    private String jSessionId;

    private BusinessScenarioState state;

    /**
     * The request sequence IDs seen by this scenario, in the order in which they were detected.
     */
    private List<String> requestSequenceIds;

    /**
     * May be null, but once it is set, it must stay the same for the duration of the business scenario, if an update
     * with a different iteration ID is received, the update invocation must throw exception.
     */
    private String iterationId;

    //
    // The line number of the first request of the scenario.
    //
    private Long lineNumber;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * Note that after the creation of the BusinessScenario instance, you will always need to invoke update() for the
     * first request to initialize internal statistics.
     */
    public BusinessScenario() {

        this.id = getNextId();
        this.state = BusinessScenarioState.NEW;
        this.requestSequenceIds = new ArrayList<>();
        log.debug(this + " constructed");
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
        String iterationId = event.getIterationId();

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
            lineNumber = event.getLineNumber();
            setType(startMarker);
            setBeginTimestamp(event.getTimestamp());
            setState(BusinessScenarioState.ACTIVE);
            setJSessionId(event.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY));
            // this is the only time when we set the iteration ID
            if (iterationId != null) {
                setIterationId(iterationId);
            }
        }
        else {

            //
            // ignore the request if the scenario is not active. Throwing a BusinessScenarioException will cause
            // a Fault to be sent down the pipeline, but it won't interrupt processing
            //

            if (beginTimestamp <= 0) {

                throw new BusinessScenarioException(
                        getLineNumber(),
                        BusinessScenarioFaultType.NO_ACTIVE_BUSINESS_SCENARIO,
                        "there is no active business scenario for " + event);
            }

            //
            // sanity check, all events must be part of the same session
            //

            String eventSession = event.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY);
            if ((jSessionId != null || eventSession != null) &&
                    (jSessionId == null || !jSessionId.equals(eventSession))) {

                throw new UserErrorException(
                        this + " was updated with a request that belongs to a different session " +
                                event.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY));
            }
        }

        Long requestDuration = event.getRequestDuration();
        String requestSequenceId = event.getRequestSequenceId();
        updateScenarioStatistics(requestDuration, requestSequenceId, iterationId);

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
            setState(BusinessScenarioState.CLOSED_NORMALLY);
        }

        if (requestDuration == null) {
            throw new BusinessScenarioException(
                    getLineNumber(), BusinessScenarioFaultType.NO_REQUEST_DURATION_INFO,
                    event + " does not have request duration information");
        }

        return isClosed();
    }

    /**
     * Forcibly closes a business scenario in a NEW or ACTIVE state (closing a NEW scenario is a noop).
     *
     * A CLOSED_NORMALLY instance cannot be closed, will throw an IllegalArgumentException.
     *
     * @exception IllegalArgumentException on attempt to close an already closed instance.
     */
    public void close() {

        if (!state.equals(BusinessScenarioState.NEW) && !state.equals(BusinessScenarioState.ACTIVE )) {

            //
            // we cannot forcibly close a scenario unless is in ACTIVE state
            //
            throw new IllegalStateException("cannot forcibly close a " + getState() + " scenario");
        }

        setState(BusinessScenarioState.CLOSED_EXPLICITLY);

        //
        // we don't know end timestamp
        //
        endTimestamp = -1L;
    }

    /**
     * @return true if this business scenario was updated with the last HTTP request from the sequence - the one
     * that contains the proper BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME header.
     */
    public boolean isClosed() {

        return !state.equals(BusinessScenarioState.NEW) && !state.equals(BusinessScenarioState.ACTIVE);
    }

    public boolean isNew() {
        return state.equals(BusinessScenarioState.NEW);
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
        if (jSessionId != null) {
            bse.setStringProperty(BusinessScenarioEvent.JSESSIONID, jSessionId);
        }
        if (iterationId != null) {
            bse.setStringProperty(BusinessScenarioEvent.ITERATION_ID, iterationId);
        }
        return bse;
    }

    public BusinessScenarioState getState() {
        return state;
    }

    public void setState(BusinessScenarioState state) {
        this.state = state;
    }

    public void setJSessionId(String jSessionId) {
        this.jSessionId = jSessionId;
    }

    public String getJSessionId() {
        return jSessionId;
    }

    /**
     * @return the request sequence IDs in the order they were received. Returns the underlying storage, so handle with
     * care. The list may be empty but never null.
     */
    public List<String> getRequestSequenceIds() {

        return requestSequenceIds;
    }

    /**
     * @return the iteration ID. May be null, but once it is set, it must stay the same for the duration of the business
     * scenario, if an update with a different iteration ID is received, the update invocation must throw exception.
     */
    public String getIterationId() {

        return iterationId;
    }

    /**
     * @return the line number of the first request of the scenario.
     */
    public Long getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {

        return "BusinessScenario[" + BusinessScenarioCommand.formatTimestamp(getBeginTimestamp()) + " " +
                "line=" + getLineNumber() + ", id=" + getId() + ", iteration-id=" + iterationId + ", " +
                "state=" + getState() + ", type=" + getType() + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

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
     * @param requestDuration may be null.
     * @param requestSequenceId the request sequence ID, may be null. For more details, see
     *                    BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME constant definition.
     * @param iterationId the iteration ID, may be null. For more details, see
     *                    BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME constant definition. A business scenario
     *                    can only exists in the context of a single iteration, so if a business scenario receives
     *                    requests belonging to different iterations, will throw a BusinessScenarioException
     * @exception BusinessScenarioException if duplicate request sequence ID is detected, if more than on iteration ID
     * is detected.
     */
    void updateScenarioStatistics(Long requestDuration, String requestSequenceId, String iterationId)
            throws BusinessScenarioException {

        requestCount ++;
        duration += (requestDuration == null ? 0 : requestDuration);

        if (requestSequenceId != null) {

            //
            // check for duplicates
            //
            if (requestSequenceIds.contains(requestSequenceId)) {
                throw new BusinessScenarioException(
                        getLineNumber(),
                        BusinessScenarioFaultType.DUPLICATE_REQUEST_SEQUENCE_ID,
                        this + " received duplicate request sequence ID \"" + requestSequenceId + "\"");

            }
            requestSequenceIds.add(requestSequenceId);
        }

        if (this.iterationId == null && iterationId != null)
        {
            throw new BusinessScenarioException(
                    getLineNumber(),
                    BusinessScenarioFaultType.SUDDEN_ITERATION_IDS,
                    this + " is suddenly starting to see iteration IDs after it started without one: " + iterationId);
        }

        if (this.iterationId != null && iterationId == null) {

            throw new BusinessScenarioException(
                    getLineNumber(),
                    BusinessScenarioFaultType.MISSING_ITERATION_ID,
                    this + " does not see iteration IDs anymore");
        }

        if (this.iterationId != null && !this.iterationId.equals(iterationId)) {

            throw new BusinessScenarioException(
                    getLineNumber(),
                    BusinessScenarioFaultType.MULTIPLE_ITERATION_IDS,
                    this + " exposed to multiple iterations: " + this.iterationId + ", " + iterationId);
        }
    }

    void setIterationId(String iterationId) {
        this.iterationId = iterationId;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
