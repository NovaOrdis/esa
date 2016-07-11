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

import io.novaordis.events.core.event.GenericTimedEvent;
import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.StringProperty;
import io.novaordis.utilities.timestamp.Timestamp;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class BusinessScenarioEvent extends GenericTimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String ID_PROPERTY_NAME = "id";
    public static final String DURATION_PROPERTY_NAME = "duration";

    public static final String REQUEST_COUNT_PROPERTY_NAME = "request-count";

    // the number of requests that return with 200
    public static final String SUCCESSFUL_REQUEST_COUNT_PROPERTY_NAME = "successful-request-count";

    public static final String TYPE_PROPERTY_NAME = "type";
    public static final String STATE_PROPERTY_NAME = "state";
    public static final String JSESSIONID_PROPERTY_NAME = "jsessionid";
    public static final String ITERATION_ID_PROPERTY_NAME = "iteration-id";

    // a List property that contains, in order, individual request durations
    public static final String REQUEST_DURATIONS_PROPERTY_NAME = "request-durations";

    // a List property that contains, in order, individual request status codes
    public static final String REQUEST_STATUS_CODES_PROPERTY_NAME = "request-status-codes";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    protected BusinessScenarioEvent(Timestamp timestamp) {
        super(timestamp);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return may return null if the state is not stored in the event, or it may throw
     * IllegalStateException if the event carries an invalid state.
     *
     * @exception IllegalStateException
     */
    public BusinessScenarioState getState() {

        StringProperty sp = getStringProperty(BusinessScenarioEvent.STATE_PROPERTY_NAME);
        String s = sp == null ? null : sp.getString();

        if (s == null) {
            return null;
        }

        try {
            return BusinessScenarioState.valueOf(s);
        }
        catch(Exception e) {
            throw new IllegalStateException(this + " carries an invalid BusinessScenarioState value \"" + s + "\"", e);
        }
    }

    public String getJSessionId() {

        StringProperty sp = getStringProperty(BusinessScenarioEvent.JSESSIONID_PROPERTY_NAME);
        return sp == null ? null : sp.getString();
    }

    public String getIterationId() {

        StringProperty sp = getStringProperty(BusinessScenarioEvent.ITERATION_ID_PROPERTY_NAME);
        return sp == null ? null : sp.getString();
    }

    /**
     * @return null if the scenario event does not contain duration info.
     */
    public Long getDuration() {

        LongProperty p = getLongProperty(BusinessScenarioEvent.DURATION_PROPERTY_NAME);

        if (p == null) {
            return null;
        }

        return p.getLong();
    }

    /**
     * @return the number of requests associated with this scenario. A scenario must have at least one request - the
     * request that started it.
     */
    public int getRequestCount() {

        IntegerProperty p = getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT_PROPERTY_NAME);

        if (p == null) {
            throw new IllegalStateException(this + " does not have a request count");
        }

        return p.getInteger();
    }

    /**
     * @return the count of requests that return with a 200 status code. It may be null, in case the status code
     * information is not available in the original HTTP events.
     */
    public Integer getSuccessfulRequestCount() {

        IntegerProperty p = getIntegerProperty(BusinessScenarioEvent.SUCCESSFUL_REQUEST_COUNT_PROPERTY_NAME);

        if (p == null) {
            return null;
        }

        return p.getInteger();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
