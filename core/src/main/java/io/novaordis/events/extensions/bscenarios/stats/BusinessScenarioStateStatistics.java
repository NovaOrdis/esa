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

package io.novaordis.events.extensions.bscenarios.stats;

import io.novaordis.events.extensions.bscenarios.BusinessScenarioEvent;
import io.novaordis.events.extensions.bscenarios.BusinessScenarioState;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/23/16
 */
public class BusinessScenarioStateStatistics {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BusinessScenarioState state;

    private long businessScenariosCount;

    private long minDurationMs = Long.MAX_VALUE;
    private long maxDurationMs;
    private long aggregatedDurationMs;

    private int minRequestsPerScenario = Integer.MAX_VALUE;
    private int maxRequestsPerScenario;
    private int aggregatedRequests;

    // the number of scenarios in this state, for which *all* requests are successful (status code 200)
    private int successfulScenariosCount;

    // Constructors ----------------------------------------------------------------------------------------------------

    public BusinessScenarioStateStatistics(BusinessScenarioState state) {
        this.state = state;
        this.businessScenariosCount = 0L;
        this.successfulScenariosCount = 0;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void update(BusinessScenarioEvent bse) {

        if (!bse.getState().equals(state)) {

            throw new IllegalArgumentException(bse + " has a different state than " + this);
        }

        businessScenariosCount ++;

        Long duration = bse.getDuration();
        if (duration != null) {

            if (duration > maxDurationMs) {
                maxDurationMs = duration;
            }
            if (duration < minDurationMs) {
                minDurationMs = duration;
            }
            aggregatedDurationMs += duration;
        }

        int requestCount = bse.getRequestCount();
        if (requestCount > maxRequestsPerScenario) {
            maxRequestsPerScenario = requestCount;
        }
        if (requestCount < minRequestsPerScenario) {
            minRequestsPerScenario = requestCount;
        }
        aggregatedRequests += requestCount;

        Integer successfulRequestCount = bse.getSuccessfulRequestCount();

        if (successfulRequestCount != null && successfulRequestCount == requestCount) {
            successfulScenariosCount ++;
        }
    }

    public long getBusinessScenarioCount() {
        return businessScenariosCount;
    }

    /**
     * The duration of the shortest scenario in this state.
     */
    public long getMinDurationMs() {

        return minDurationMs;
    }

    public long getAverageDurationMs() {

        return (long)(((double)aggregatedDurationMs)/businessScenariosCount);
    }

    public long getMaxDurationMs() {

        return maxDurationMs;
    }

    public int getMinRequestsPerScenario() {

        return minRequestsPerScenario;
    }

    public double getAverageRequestsPerScenario() {

        return ((double) aggregatedRequests)/businessScenariosCount;
    }

    public int getMaxRequestsPerScenario() {

        return maxRequestsPerScenario;
    }

    /**
     * @return the number of scenarios in this state, for which *all* requests are successful (status code 200)
     */
    public int getSuccessfulCount() {

        return successfulScenariosCount;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
