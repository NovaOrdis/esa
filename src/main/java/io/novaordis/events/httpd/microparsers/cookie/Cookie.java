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

package io.novaordis.events.httpd.microparsers.cookie;

import io.novaordis.events.api.parser.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The cookies extracted from parsing the log representation.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/1/16
 */
public class Cookie {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(Cookie.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String name;

    private String value;

    /**
     * The representation of the cookie as read from the logs. It includes leading and trailing spaces, if present.
     */
    private String literal;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * Builds a Cookie instance based on its log representation. The string received from the upper layer parser
     * MUST NOT contain semi-colons - breaking down the log into semi-colons separated tokens is the job of the upper
     * layer.
     *
     * @param logRepresentation - the log string from which the cookie will be extracted. Must NOT contain semi-colons.
     *
     * @param lineNumber used for reporting. Can be null.
     *
     * @throws IllegalArgumentException if the log representation contains semi-colons. This means the upper layer
     * did not do its job correctly.
     */
    public Cookie(String logRepresentation, Long lineNumber) throws ParsingException {

        if (logRepresentation.contains(";")) {
            throw new IllegalArgumentException("cookie representation contains semicolons");
        }

        this.literal = logRepresentation;

        int equalIndex = logRepresentation.indexOf('=');

        if (equalIndex == -1) {
            throw new ParsingException(getLiteral() + " missing from \"" + logRepresentation + "\"", lineNumber);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getLiteral() {
        return literal;
    }

    public String toString() {

        return "" + literal;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
