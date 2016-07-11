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
import io.novaordis.utilities.timestamp.Timestamp;
import io.novaordis.utilities.timestamp.TimestampImpl;
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
    private Timestamp beginTimestamp;

    //
    // the timestamp of the moment the last request exists the scenario, null if the scenario is still active
    //
    private Timestamp endTimestamp;

    private String jSessionId;

    private BusinessScenarioState state;

    /**
     * The requests, in the order they are read from the logs and exposed to this instance.
     */
    private List<HttpRequestResponsePair> requestResponsePairs;

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
        this.requestResponsePairs = new ArrayList<>();
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
     * If the request is sufficiently broken to produce a fault, but not broken enough to stop processing, the method
     * will correctly update the state of the scenario and then throw a BusinessScenarioException. The upper layer can
     * rely on a correct state of the scenario even if a BusinessScenarioException was thrown.
     *
     * @see BusinessScenario#isClosed()
     *
     * @exception UserErrorException if the lower layers encountered a problem that stops us from processing the
     *  event stream (most likely because trying to produce further results won't make sense). Example: if the HTTP
     *  request does not belong to the current session, etc. In this case, the process must exit with a user-readable
     *  error.
     * @exception BusinessScenarioException if the request is sufficiently broken to produce a fault, but not broken
     *  enough to stop processing. The business scenario state is guaranteed to be consistent if this exception is
     *  thrown. The fault information (BusinessScenarioException state) will be turned into a fault by the upper layer.
     *
     * @return true is this business scenario instance was closed by this update.
     */
    public boolean update(HttpEvent event) throws BusinessScenarioException, UserErrorException {

        if (isClosed()) {
            throw new IllegalStateException(this + " already closed, cannot be updated with " + event);
        }

        lineNumber = event.getLineNumber();
        Timestamp requestTimestamp = event.getTimestamp();

        String startMarker = event.getRequestHeader(BUSINESS_SCENARIO_START_MARKER_HEADER_NAME);
        String iterationId = event.getIterationId();


        if (startMarker != null) {

            if (state.equals(BusinessScenarioState.OPEN)) {

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
                endTimestamp = requestTimestamp;
                setState(BusinessScenarioState.CLOSED_BY_START_MARKER);
                return true;
            }

            //
            // we "start" the scenario
            //
            setType(startMarker);
            setBeginTimestamp(requestTimestamp);
            setState(BusinessScenarioState.OPEN);
            setJSessionId(event.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY));
            // this is the only time when we set the iteration ID
            if (iterationId != null) {
                setIterationId(iterationId);
            }

            log.debug("A START marker for a scenario of type " + startMarker + " was found in HTTP session " + jSessionId);
        }
        else {

            //
            // ignore the request if the scenario is not open. Throwing a BusinessScenarioException will cause
            // a Fault to be sent down the pipeline, but it won't interrupt processing
            //

            if (beginTimestamp == null) {

                throw new BusinessScenarioException(
                        getLineNumber(),
                        BusinessScenarioFaultType.NO_OPEN_BUSINESS_SCENARIO,
                        "there is no open business scenario for " + event);
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

        updateScenarioStatistics(event);

        Long requestDuration = event.getRequestDuration();
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

            setState(BusinessScenarioState.COMPLETE);
        }

        if (requestDuration == null) {
            throw new BusinessScenarioException(
                    getLineNumber(), BusinessScenarioFaultType.NO_REQUEST_DURATION_INFO,
                    event + " does not have request duration information");
        }

        return isClosed();
    }

    /**
     * Forcibly closes a business scenario in a NEW or OPEN state (closing a NEW scenario is a noop).
     *
     * A COMPLETE instance cannot be closed, will throw an IllegalArgumentException.
     *
     * @exception IllegalArgumentException on attempt to close an already closed instance.
     */
    public void close() {

        if (!state.equals(BusinessScenarioState.NEW) && !state.equals(BusinessScenarioState.OPEN)) {

            //
            // we cannot forcibly close a scenario unless is in OPEN state
            //
            throw new IllegalStateException("cannot forcibly close a " + getState() + " scenario");
        }

        setState(BusinessScenarioState.INCOMPLETE);

        //
        // endTimestamp was already updated by updateScenarioStatistics
        //
    }

    /**
     * @return true if this business scenario was updated with the last HTTP request from the sequence - the one
     * that contains the proper BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME header.
     */
    public boolean isClosed() {

        return !state.equals(BusinessScenarioState.NEW) && !state.equals(BusinessScenarioState.OPEN);
    }

    public boolean isNew() {
        return state.equals(BusinessScenarioState.NEW);
    }

    /**
     * The timestamp of the moment the first request of the scenario enters the server. May return null if there
     * are no requests processed yet.
     */
    public Timestamp getBeginTimestamp() {
        return beginTimestamp;
    }

    /**
     * @return if true, this business scenario has seen the start marker and can be updated with new requests.
     */
    public boolean isOpen() {

        return beginTimestamp != null && !isClosed();
    }

    /**
     * The timestamp of the moment the last request exists the scenario, null if the scenario is still active.
     */
    public Timestamp getEndTimestamp() {
        return endTimestamp;
    }

    /**
     * The sum of all individual HTTP requests duration, in the measure unit logs were generated with.
     */
    public long getDuration() {

        long duration = 0L;

        // always calculate by iterating over individual requests
        for(HttpRequestResponsePair p: requestResponsePairs) {
            Long pairDuration = p.getDuration();
            if (pairDuration != null) {
                duration += pairDuration;
            }
        }

        return duration;
    }

    public int getRequestCount() {
        return requestResponsePairs.size();
    }

    /**
     * @return the count of requests that have the given status code. Note that some requests may not have any
     * status code at all if that was not present in the HttpEvent corresponding to the request. This is not
     * necessarily an error condition, just an incomplete data case.
     */
    public int getRequestCount(int statusCode) {

        int c = 0;
        for(HttpRequestResponsePair r: requestResponsePairs) {
            Integer sc = r.getStatusCode();
            if (sc != null && sc == statusCode) {
                c ++;
            }
        }
        return c;
    }

    public BusinessScenarioEvent toEvent() {

        BusinessScenarioEvent bse = new BusinessScenarioEvent(beginTimestamp);
        bse.setLongProperty(BusinessScenarioEvent.ID_PROPERTY_NAME, getId());
        bse.setLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME, getDuration());
        bse.setIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME, requestResponsePairs.size());
        bse.setStringProperty(BusinessScenarioEvent.TYPE_PROPERTY_NAME, type);
        bse.setStringProperty(BusinessScenarioEvent.STATE_PROPERTY_NAME, getState().name());
        bse.setStringProperty(BusinessScenarioEvent.JSESSIONID_PROPERTY_NAME, jSessionId);
        bse.setStringProperty(BusinessScenarioEvent.ITERATION_ID_PROPERTY_NAME, iterationId);
        bse.setIntegerProperty(BusinessScenarioEvent.SUCCESSFUL_REQUEST_COUNT_PROPERTY_NAME, getRequestCount(200));

        if (requestResponsePairs != null && !requestResponsePairs.isEmpty()) {

            List<Long> durations = new ArrayList<>();
            List<Integer> statusCodes = new ArrayList<>();

            for(HttpRequestResponsePair p: requestResponsePairs) {
                durations.add(p.getDuration());
                statusCodes.add(p.getStatusCode());
            }

            bse.setListProperty(BusinessScenarioEvent.REQUEST_DURATIONS_PROPERTY_NAME, durations);
            bse.setListProperty(BusinessScenarioEvent.REQUEST_STATUS_CODES_PROPERTY_NAME, statusCodes);
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
     * @return the iteration ID. May be null, but once it is set, it must stay the same for the aggregatedScenarioDuration of the business
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

    /**
     * @return the request sequence IDs in the order they were received. The list may be empty but never null.
     */
    public List<String> getRequestSequenceIds() {

        List<String> result = new ArrayList<>();
        for(HttpRequestResponsePair r: requestResponsePairs) {

            String requestSequenceId = r.getRequestSequenceId();

            if (requestSequenceId != null) {
                result.add(requestSequenceId);
            }
        }

        return result;
    }

    @Override
    public String toString() {

        return "BusinessScenario[" + BusinessScenarioCommand.formatTimestamp(getBeginTimestamp()) + " " +
                "line=" + getLineNumber() + ", id=" + getId() + ", iteration-id=" + iterationId + ", " +
                "state=" + getState() + ", type=" + getType() + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void setId(long id) {
        this.id = id;
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

    void setBeginTimestamp(Timestamp ts) {
        this.beginTimestamp = ts;
    }

    /**
     * @exception BusinessScenarioException if duplicate request sequence ID is detected, if more than on iteration ID
     * is detected.
     */
    void updateScenarioStatistics(HttpEvent event) throws BusinessScenarioException {

        updatePerRequestStatistics(event);

        Timestamp requestTimestamp = event.getTimestamp();
        Long rd = event.getRequestDuration();
        long requestDuration = rd == null ? 0 : rd;
        endTimestamp = new TimestampImpl
                (requestTimestamp.getTimestampGMT() + requestDuration, requestTimestamp.getTimezoneOffsetMs());

        // See BUSINESS_SCENARIO_ITERATION_ID_HEADER_NAME constant definition. A business scenario can only exists in
        // the context of a single iteration, so if a business scenario receives requestResponsePairs belonging to different
        // iterations, will throw a BusinessScenarioException
        String iterationId = event.getIterationId();

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

    /**
     * Package protected for testing.
     *
     * @exception BusinessScenarioException if duplicate requestSequenceId is detected.
     */
    void updatePerRequestStatistics(HttpEvent event) throws BusinessScenarioException {

        HttpRequestResponsePair request = new HttpRequestResponsePair(event);

        //
        // check for duplicates, figure out early if we've seen the same request sequence ID and throw exception
        //

        // See BUSINESS_SCENARIO_REQUEST_SEQUENCE_ID_HEADER_NAME
        String requestSequenceId = request.getRequestSequenceId();
        if (requestSequenceId != null) {

            for(HttpRequestResponsePair r: requestResponsePairs) {
                if (requestSequenceId.equals(r.getRequestSequenceId())) {
                    throw new BusinessScenarioException(
                            getLineNumber(),
                            BusinessScenarioFaultType.DUPLICATE_REQUEST_SEQUENCE_ID,
                            this + " received duplicate request sequence ID \"" + requestSequenceId + "\"");
                }
            }
        }

        requestResponsePairs.add(request);
    }

    List<HttpRequestResponsePair> getRequestResponsePairs() {
        return requestResponsePairs;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
