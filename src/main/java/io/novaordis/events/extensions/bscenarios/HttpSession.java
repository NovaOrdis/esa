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

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.FaultEvent;
import io.novaordis.events.httpd.HttpEvent;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects data associated with a HTTP session, as identified in the incoming event stream by its JSESSIONID.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/21/16
 */
class HttpSession {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpSession.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String jSessionId;

    private BusinessScenario current;

    private long requestsProcessedBySessionCount;
    private long faultCount;

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpSession() {
        this(null);
    }

    public HttpSession(String jSessionId) {

        this.jSessionId = jSessionId;
        current = new BusinessScenario();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getJSessionId() {
        return jSessionId;
    }

    public void setJSessionId(String jSessionId) {
        this.jSessionId = jSessionId;
    }

    /**
     * Updates the statistics associated with the corresponding business scenario or throws it away.
     *
     * NOT thread safe.
     *
     * @return one or more events. May be an empty list, but never null.
     *
     * @exception UserErrorException if the lower layers encountered a problem that stops us from processing the
     *  event stream (most likely because trying to produce further results won't make sense). Example: if the HTTP
     *  request does not belong to the current session, etc. In this case, the process must exit with a user-readable
     *  error.
     */
    public List<Event> process(HttpEvent event) throws UserErrorException {

        List<Event> result = null;

        // sanity check
        if (event == null) {
            throw new IllegalArgumentException("null event");
        }

        if (!jSessionId.equals(event.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY))) {
            throw new UserErrorException("HTTP request " + event + " does not belong to " + this);
        }

        requestsProcessedBySessionCount ++;

        try {

            current.update(event);
        }
        catch(BusinessScenarioException e) {

            //
            // this type of failure is translated into a FaultEvent and it does not stop processing
            //

            //noinspection ConstantConditions
            result = addToResult(result, e);
        }

        //
        // check the state
        //

        if (current.isClosed()) {

            //
            // we've just "closed" the current business scenario, issue the corresponding BusinessScenarioEvent
            // and initialize a new instance
            //

            BusinessScenarioEvent bse = current.toEvent();

            log.debug(this + " closed " + bse);

            result = addToResult(result, bse);

            current = new BusinessScenario();

            //
            // This is a special case were the stop marker never arrived and now we're seeing a start marker again -
            // the request containing the start marker of the next scenario is used both to close the previous scenario
            // and to update the new scenario, below:
            //

            if (event.getRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME) != null) {

                try {

                    current.update(event);

                    //
                    // sanity check, this is a bit redundant
                    //
                    if (current.isClosed()) {
                        throw new IllegalStateException(event + " closed " + current);
                    }
                }
                catch(BusinessScenarioException e) {

                    //
                    // this type of failure is translated into a FaultEvent and it does not stop processing
                    //

                    result = addToResult(result, e);
                }
            }
        }

        if (result == null) {
            return Collections.emptyList();
        }

        return result;
    }

    @Override
    public String toString() {

        return "HTTP session JSESSIONID=" + getJSessionId();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * For testing only.
     */
    BusinessScenario getCurrentBusinessScenario() {

        return current;
    }

    long getRequestsProcessedBySessionCount() {
        return requestsProcessedBySessionCount;
    }

    long getFaultCount() {
        return faultCount;
    }


    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    /**
     * Adds the current argument (either a BusinessScenarioEvent or a BusinessScenarioException that will turned into
     * a FaultEvent) to the result, handling null and empty list as appropriate.
     *
     * @param current the current list - may be null.
     * @param o a BusinessScenarioEvent or a BusinessScenarioException that will be converted to a Fault.
     */
    private List<Event> addToResult(List<Event> current, Object o) {

        Event e;

        if (o instanceof BusinessScenarioEvent) {

            e = (BusinessScenarioEvent)o;
        }
        else if (o instanceof BusinessScenarioException) {

            BusinessScenarioException bse = (BusinessScenarioException)o;
            e = new FaultEvent(bse.getFaultType(), bse);
            faultCount ++;
        }
        else {
            throw new IllegalArgumentException(
                    "the argument must be either a BusinessScenarioEvent or a BusinessScenarioException, but it is " +
                            o);
        }

        if (current == null) {
             current = new ArrayList<>();
        }

        current.add(e);
        return current;
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
