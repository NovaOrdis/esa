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

import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.FaultEvent;
import io.novaordis.events.httpd.HttpEvent;

/**
 * Collects data associated with a HTTP session, as identified in the incoming event stream by its JSESSIONID.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/21/16
 */
class HttpSession {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String jSessionId;

    private BusinessScenario current;

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpSession() {
        this(null);
    }

    public HttpSession(String jSessionId) {
        this.jSessionId = jSessionId;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getJSessionId() {
        return jSessionId;
    }

    public void setJSessionId(String jSessionId) {
        this.jSessionId = jSessionId;
    }

    /**
     * NOT thread safe.
     *
     * @return a BusinessScenarioEvent, a FaultEvent or null if it gets a HTTP event that belongs to the business
     * scenario being processes.
     *
     * @exception IllegalArgumentException signals a condition serious enough to stop processing (if the HTTP request
     * does not belong to the current session, etc.)
     */
    public Event processBusinessScenario(HttpEvent event) {

        // sanity check
        if (event == null) {
            throw new IllegalArgumentException("null event");
        }

        if (!jSessionId.equals(event.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY))) {
            throw new IllegalArgumentException("HTTP request " + event + " does not belong to " + this);
        }

        if (current != null) {

            //
            // we're in the middle of a business scenario, and we do belong to the right session
            //

            try {
                current.update(event);
            }
            catch(BusinessScenarioException e) {

                //
                // we don't stop the processing of the current scenario, return a fault instead
                //
                return new FaultEvent(e);
            }

            if (!current.isClosed()) {
                return null;
            }

            //
            // this request closed the scenario
            //

            BusinessScenarioEvent bse = current.toEvent();
            current = null;
            return bse;
        }

        //
        // no business scenario active
        //

        //
        // are we the first request of a scenario?
        //

        String bsType = event.getRequestHeader(BusinessScenario.BUSINESS_SCENARIO_START_MARKER_HEADER_NAME);
        if (bsType != null) {

            //
            // we do start a scenario indeed
            //
            current = new BusinessScenario(bsType);

            try {
                current.update(event);
            }
            catch(BusinessScenarioException e) {

                //
                // we don't stop the processing of the current scenario, return a fault instead
                //
                return new FaultEvent(e);
            }

            //
            // nothing (yet) for the layer above
            //
            return null;
        }

        //
        // there's no current scenario, and we did not encounter the scenario start marker yet, issue a fault
        //

        return new FaultEvent("HTTP request " + event + " does not belong to any business scenario");
    }

    @Override
    public String toString() {

        return "HTTP session JSESSIONID=" + getJSessionId();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
