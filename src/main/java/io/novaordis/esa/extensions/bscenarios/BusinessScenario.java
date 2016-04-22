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

package io.novaordis.esa.extensions.bscenarios;

import io.novaordis.esa.core.event.IntegerProperty;
import io.novaordis.esa.core.event.LongProperty;
import io.novaordis.esa.httpd.HttpEvent;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class BusinessScenario {

    // Constants -------------------------------------------------------------------------------------------------------

    // If this header is encountered, it means the containing HTTP request is the first HTTP request of a business
    // scenario of the type mentioned as the header value
    public static final String BUSINESS_SCENARIO_START_MARKER_HEADER_NAME = "Events-Business-Scenario-Start";

    // If this header is encountered, it means the containing HTTP request is the last HTTP request of a business
    // scenario of the type mentioned as the header value; all HTTP requests between the start and stop marker
    // belong to the business scenario
    public static final String BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME = "Events-Business-Scenario-Stop";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    //
    // the business scenario type - the value of the business scenario start marker header that starts this scenario
    //
    private String type;

    //
    // the timestamp of the first request of the scenario
    //
    private long timestamp;
    private long totalProcessingTime;

    // the number of requests comprising this scenario
    private int requestCount;

    private boolean closed;

    // Constructors ----------------------------------------------------------------------------------------------------

    public BusinessScenario(String type) {

        this.type = type;
        closed = false;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getType() {
        return type;
    }

    /**
     * Update the current business scenario's statistics with this request. Close the business scenario if appropriate.
     *
     * @see BusinessScenario#isClosed()
     */
    public void update(HttpEvent event) {

        requestCount++;
        updateProcessingTime(event);


        if (event.getRequestHeader(BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME) != null) {
            closed = true;
        }
    }

    /**
     * @return true if this business scenario was updated with the last HTTP request from the sequence - the one
     * that contains the proper BUSINESS_SCENARIO_STOP_MARKER_HEADER_NAME header.
     */
    public boolean isClosed() {

        return closed;
    }

    public BusinessScenarioEvent toEvent() {

        BusinessScenarioEvent bse = new BusinessScenarioEvent(timestamp);
        bse.setProperty(new LongProperty(BusinessScenarioEvent.TOTAL_PROCESSING_TIME, totalProcessingTime));
        bse.setProperty(new IntegerProperty(BusinessScenarioEvent.REQUEST_COUNT, requestCount));
        return bse;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public int getRequestCount() {
        return requestCount;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void updateProcessingTime(HttpEvent event) {

        LongProperty p = event.getLongProperty(HttpEvent.REQUEST_PROCESSING_TIME);

        if (p == null) {
            throw new IllegalArgumentException("current request does not have request processing time information");
        }

        totalProcessingTime += p.getLong();
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
