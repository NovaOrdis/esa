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

import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.httpd.HttpEvent;

/**
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

    // Attributes ------------------------------------------------------------------------------------------------------

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
     * Note that after the creation of the BusinessScenario instance, you will still need to invoke update() for the
     * first request to initialize internal statistics.
     */
    public BusinessScenario(String type) {

        this.type = type;
        this.endTimestamp = 0L;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getType() {
        return type;
    }

    /**
     * Update the current business scenario's statistics with this request. Close the business scenario if appropriate.
     *
     * @see BusinessScenario#isClosed()
     *
     * @exception IllegalArgumentException fatal error that breaks the processing
     * @exception BusinessScenarioException checked exception that does not stop processing, but it is turned into
     *            a fault by the upper layer
     */
    public void update(HttpEvent event) throws BusinessScenarioException {

        if (beginTimestamp == 0) {
            beginTimestamp = event.getTimestamp();
        }

        Long rd = event.getRequestDuration();

        if (rd == null) {
            throw new BusinessScenarioException(event + " does not have request duration information");
        }

        duration += rd;

        requestCount++;

        String stopMarker = event.getRequestHeader(BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME);

        if (stopMarker == null) {
            return;
        }

        //
        // this is the last request of the scenario, make sure the stop marker is either empty string or it conincides
        // with the scenario type
        //

        stopMarker = stopMarker.trim();

        if (stopMarker.length() != 0 && !stopMarker.equals(type)) {
            throw new IllegalArgumentException(event +
                    " ends a different scenario type (" + stopMarker + ") than the current one (" + type + ")");
        }

        endTimestamp = event.getTimestamp() + rd;
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
        bse.setProperty(new LongProperty(BusinessScenarioEvent.DURATION, duration));
        bse.setProperty(new IntegerProperty(BusinessScenarioEvent.REQUEST_COUNT, requestCount));
        return bse;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
