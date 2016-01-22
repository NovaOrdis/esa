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

package io.novaordis.esa;

/**
 * An individual format element - for example the "%h" element that represents the remote client address for a httpd
 * log.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public interface FormatElement {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    String getLiteral();

    /**
     * Turns a string representation of the format element, as read from the log, into a typed value.
     *
     * @return a typed value, never null.
     *
     * @throws ParsingException if the string representation does not match the format element.
     */
    Object parse(String logStringRepresentation) throws ParsingException;

    /**
     * @return the type of the values maintained for this format element.
     */
    Class getType();

    /**
     * @return true if this element is used to enclose at the left a string (possibly containing multiple spaces).
     * Examples: left bracket, double quotes, single quote.
     */
    boolean isLeftEnclosure();

    /**
     * @return true if this element is used to enclose at the right a string (possibly containing multiple spaces).
     * Examples: right bracket, double quotes, single quote.
     */
    boolean isRightEnclosure();

    /**
     * If this element is an enclosure, the method returns corresponding matching enclosure (right brace if we
     * are a left brace, left brace if we are a right brace, double quotes if we are double quotes, single quote if
     * we are a single quote. Return null if this element is not an enclosure.
     */
    FormatElement getMatchingEnclosure();

}
