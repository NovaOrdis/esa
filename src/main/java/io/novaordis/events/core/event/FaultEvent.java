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

package io.novaordis.events.core.event;

import io.novaordis.events.extensions.bscenarios.BusinessScenarioException;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class FaultEvent extends GenericEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String message;
    private Throwable cause;
    private FaultType type;
    private Long lineNumber;

    // Constructors ----------------------------------------------------------------------------------------------------

    public FaultEvent() {

        this(null, null, null);
    }

    public FaultEvent(String message) {

        this(null, message, null);
    }

    public FaultEvent(Throwable cause) {

        this(null, null, cause);
    }

    public FaultEvent(String message, Throwable cause) {

        this(null, message, cause);
    }

    public FaultEvent(FaultType type, String message) {

        this(type, message, null);
    }

    public FaultEvent(FaultType type, Throwable cause) {

        this(type, null, cause);
    }

    public FaultEvent(FaultType type, String message, Throwable cause) {

        this.type = type;
        this.message = message;
        this.cause = cause;

        if (cause instanceof BusinessScenarioException) {
            this.lineNumber = ((BusinessScenarioException)cause).getLineNumber();
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * May return null.
     */
    public String getMessage() {
        return message;
    }

    /**
     * May return null.
     */
    public Throwable getCause() {
        return cause;
    }

    public FaultType getType() {
        return type;
    }

    public Long getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {

        String s = "FAULT " + (type == null ? "(UNTYPED)" : type);

        if (lineNumber != null) {
            s += ", line " + lineNumber;
        }

        String msg = getMessage();
        Throwable cause = getCause();

        if (msg == null && cause == null) {
            return s + "[" + Integer.toHexString(System.identityHashCode(this)) + "]";
        }

        if (msg != null) {
            s += ": " + msg;
        }

        if (cause != null) {
            if (msg != null) {
                s += ", ";
            }
            else {
                s += ": ";
            }

            String exceptionToString;
            if (cause instanceof BusinessScenarioException) {
                exceptionToString = cause.getMessage();
            }
            else {
                exceptionToString = cause.getClass().getSimpleName() + ": " + cause.getMessage();
            }

            s += exceptionToString;
        }

        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
