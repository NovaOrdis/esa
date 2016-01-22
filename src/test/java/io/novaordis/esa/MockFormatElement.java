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
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public class MockFormatElement implements FormatElement {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String literal;
    private Class type;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockFormatElement(String literal) {
        this(literal, null);
    }

    public MockFormatElement(String literal, Class type) {
        this.literal = literal;
        this.type = type;
    }

    // FormatElement implementation ------------------------------------------------------------------------------------

    @Override
    public String getLiteral() {
        return literal;
    }

    @Override
    public Object parse(String logStringRepresentation) throws ParsingException {
        throw new RuntimeException("parse() NOT YET IMPLEMENTED");
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public boolean isLeftEnclosure() {
        throw new RuntimeException("isLeftEnclosure() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean isRightEnclosure() {
        throw new RuntimeException("isRightEnclosure() NOT YET IMPLEMENTED");
    }

    @Override
    public FormatElement getMatchingEnclosure() {
        throw new RuntimeException("getMatchingEnclosure() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
