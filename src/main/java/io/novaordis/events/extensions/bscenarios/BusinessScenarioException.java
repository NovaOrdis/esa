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

import io.novaordis.events.core.event.FaultType;

/**
 * An exception that will generate a fault to be sent downstream, but not interrupt processing.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public class BusinessScenarioException extends Exception {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------


    private FaultType faultType;

    // Constructors ----------------------------------------------------------------------------------------------------

    public BusinessScenarioException() {
        super();
    }

    public BusinessScenarioException(String message) {
        super(message);
    }

    public BusinessScenarioException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessScenarioException(Throwable cause) {
        super(cause);
    }

    public BusinessScenarioException(FaultType type, String message) {
        super(message);
        this.faultType = type;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * As BusinessScenarioException are usually turned into FaultEvents, we need to know the type. Can be null.
     */
    public FaultType getFaultType() {
        return faultType;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
