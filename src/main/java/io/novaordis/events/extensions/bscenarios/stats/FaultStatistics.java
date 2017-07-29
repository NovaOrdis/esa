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

import io.novaordis.events.api.event.FaultEvent;
import io.novaordis.events.api.event.FaultType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/23/16
 */
public class FaultStatistics {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private long faultCount;

    private Map<FaultType, Integer> faultPerType;

    // Constructors ----------------------------------------------------------------------------------------------------

    public FaultStatistics() {

        this.faultPerType = new HashMap<>();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void update(FaultEvent e) {

        faultCount ++;

        FaultType type = e.getType();

        Integer count = faultPerType.get(type);

        if (count == null) {
            count = 1;
        }
        else {
            count = count + 1;
        }
        faultPerType.put(type, count);
    }

    public long getFaultCount() {

        return faultCount;
    }

    public int getFaultTypeCount() {
        return faultPerType.size();
    }

    public Set<FaultType> getFaultTypes() {
        return faultPerType.keySet();
    }

    public int getCountPerType(FaultType t) {

        Integer c = faultPerType.get(t);

        if (c == null) {
            return 0;
        }

        return c;
    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
