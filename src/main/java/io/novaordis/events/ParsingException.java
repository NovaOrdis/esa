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

package io.novaordis.events;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public class ParsingException extends Exception {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Long lineNumber;
    private Integer positionInLine;

    // Constructors ----------------------------------------------------------------------------------------------------

    public ParsingException() {
        this(null, null, null, null);
    }

    public ParsingException(String message) {
        this(message, null, null, null);
    }

    public ParsingException(String message, Throwable cause) {
        this(message, cause, null, null);
    }

    public ParsingException(Throwable cause) {
        this(null, cause, null, null);
    }

    /**
     * @param lineNumber the line number of the line that generated the parsing exception. Null is acceptable if the
     *                   line number is not known.
     * @param positionInLine the position in line where the parsing exception occurred. Null is acceptable if the
     *                       position is not known.
     */
    public ParsingException(String message, Throwable cause, Long lineNumber, Integer positionInLine) {
        super(message, cause);
        this.lineNumber = lineNumber;
        this.positionInLine = positionInLine;
    }


    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the line number of the line that generated the parsing error, or null if that is not available
     */
    public Long getLineNumber() {

        return lineNumber;
    }

    /**
     * @return the position in line where the cause of the parsing exception can be found, or null if the position is
     * not available
     */
    public Integer getPositionInLine() {
        return positionInLine;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
