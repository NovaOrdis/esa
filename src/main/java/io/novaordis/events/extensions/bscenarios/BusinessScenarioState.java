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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 4/23/16
 */
public enum BusinessScenarioState {

    //
    // the business scenario has just been created and it has not seen a START marker yet
    //
    NEW,

    //
    // the business scenario has seen a START marker and it is waiting to be updated with http requests
    // belonging to the scenario
    //
    OPEN,

    //
    // the scenario has been closed normally by the occurrence of a STOP marker. If the load was sent sequentially,
    // it is guaranteed that all component HTTP requests have been applied to the scenario.
    //
    COMPLETE,

    //
    // the business scenario was closed by the occurrence of a new START marker (it has not seen the STOP marker).
    //
    CLOSED_BY_START_MARKER,

    //
    // the business scenario was closed explicitly via the close() API call and left in an "incomplete" state, usually
    // because the end of stream occurred before being updated with a STOP marker.
    //
    INCOMPLETE,


    //
    // an invalid state - a BusinessScenario instance in this state cannot be relied upon and must be discarded
    //
    FAULT
}
