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

import io.novaordis.esa.core.event.IntegerProperty;
import io.novaordis.esa.core.event.LongProperty;
import io.novaordis.esa.logs.httpd.HttpEvent;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class BusinessScenario {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    //
    // the timestamp of the first request of the scenario
    //
    private long timestamp;
    private long totalProcessingTime;

    // the number of requests comprising this scenario
    private int requestCount;

    // Constructors ----------------------------------------------------------------------------------------------------

    public BusinessScenario(HttpEvent firstEvent) {

        timestamp = firstEvent.getTimestamp();
        totalProcessingTime = 0L;
        requestCount = 1;
        updateProcessingTime(firstEvent);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Update the current business scenario's statistics with this request's information.
     */
    public void update(HttpEvent event) {

        requestCount++;
        updateProcessingTime(event);
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
