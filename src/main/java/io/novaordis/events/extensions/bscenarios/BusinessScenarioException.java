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

import io.novaordis.events.api.event.FaultType;
import io.novaordis.utilities.LineNumberException;

/**
 * An exception that will generate a fault to be sent downstream, but not interrupt processing.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public class BusinessScenarioException extends LineNumberException {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private FaultType faultType;

    // Constructors ----------------------------------------------------------------------------------------------------

    public BusinessScenarioException(Long lineNumber, String message) {

        this(lineNumber, null, message, null);
    }

    public BusinessScenarioException(Long lineNumber, FaultType type, String message) {

        this(lineNumber, type, message, null);
    }

    public BusinessScenarioException(Long lineNumber, FaultType type, String message, Throwable cause) {

        super(message, cause, lineNumber);
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
