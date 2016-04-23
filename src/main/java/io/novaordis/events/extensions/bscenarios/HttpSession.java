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
     * NOT thread safe.
     *
     * @exception UserErrorException if the lower layers encountered a problem that stops us from processing the
     *  event stream (most likely because trying to produce further results won't make sense). Example: if the HTTP
     *  request does not belong to the current session, etc. In this case, the process must exit with a user-readable
     *  error.
     */
    public Event processBusinessScenario(HttpEvent event) throws UserErrorException {

        // sanity check
        if (event == null) {
            throw new IllegalArgumentException("null event");
        }

        if (!jSessionId.equals(event.getCookie(HttpEvent.JSESSIONID_COOKIE_KEY))) {
            throw new UserErrorException("HTTP request " + event + " does not belong to " + this);
        }

        Event result = null;

        try {

            if (current.update(event)) {

                //
                // we've just "closed" the current business scenario, issue the corresponding BusinessScenarioEvent
                // and initialize a new instance
                //

                result = current.toEvent();
                current = new BusinessScenario();
            }
        }
        catch(BusinessScenarioException e) {

            //
            // this type of failure is translated into a FaultEvent and it does not stop processing
            //

            result = new FaultEvent(e);
        }

        return result;
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
