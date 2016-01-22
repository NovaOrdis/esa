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

}
