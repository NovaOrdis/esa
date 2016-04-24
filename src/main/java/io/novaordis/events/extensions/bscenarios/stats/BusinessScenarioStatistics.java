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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/23/16
 */
public class BusinessScenarioStatistics {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Map<BusinessScenarioState, BusinessScenarioStateStatistics> statsPerState;

    // Constructors ----------------------------------------------------------------------------------------------------

    public BusinessScenarioStatistics() {

        this.statsPerState = new HashMap<>();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void update(BusinessScenarioEvent bse) {

        BusinessScenarioState state = bse.getState();
        BusinessScenarioStateStatistics bsss = statsPerState.get(state);

        if (bsss == null) {
            bsss = new BusinessScenarioStateStatistics(state);
            statsPerState.put(state, bsss);
        }

        bsss.update(bse);
    }

    /**
     * @return the total number of business scenarios that entered the statistics instance so far
     */
    public long getBusinessScenarioCount() {

        long c = 0;
        for(BusinessScenarioStateStatistics s: statsPerState.values()) {

            c += s.getBusinessScenarioCount();
        }

        return c;
    }

    public Set<BusinessScenarioState> getBusinessScenarioStates() {

        return statsPerState.keySet();
    }

    /**
     * May return null if no such state is known.
     */
    public BusinessScenarioStateStatistics getBusinessScenarioStatisticsPerState(BusinessScenarioState s) {

        return statsPerState.get(s);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
