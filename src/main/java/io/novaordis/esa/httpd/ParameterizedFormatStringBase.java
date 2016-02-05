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

package io.novaordis.esa.httpd;

import io.novaordis.esa.core.event.MapProperty;
import io.novaordis.esa.ParsingException;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
abstract class ParameterizedFormatStringBase implements ParameterizedFormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param formatStringLiteral as declared in the format specification, example: %{i,Some-Header} or
     *                            %{o,Some-Header}
     *
     * @throws IllegalArgumentException if the literal does not match the expected pattern.
     */
    protected ParameterizedFormatStringBase(String formatStringLiteral) throws IllegalArgumentException {

        parseInternal(formatStringLiteral);
    }

    // ParameterizedFormatString implementation ------------------------------------------------------------------------

    @Override
    public Object parse(String logStringRepresentation) throws ParsingException {

        if (logStringRepresentation == null ||
                logStringRepresentation.length() == 0 ||
                "-".equals(logStringRepresentation)) {
            return null;
        }

        return logStringRepresentation;
    }

    @Override
    public Class getType() {
        return String.class;
    }

    @Override
    public boolean isLeftEnclosure() {
        return false;
    }

    @Override
    public boolean isRightEnclosure() {
        return false;
    }

    @Override
    public FormatString getMatchingEnclosure() {
        return null;
    }

    /**
     * We maintain all values corresponding to parameterized format strings in maps: the values corresponding to
     * request header format strings go to a "request-headers" Map,  the values corresponding to response header format
     * strings go to a "response-headers" Map and all cookies go into a "cookies" Map. If the map already exists in the
     * event, the contents of this one will be merged into the existing one.
     */
    @Override
    public MapProperty toProperty(Object value) {

        MapProperty result = new MapProperty(getHttpEventMapName());
        result.getMap().put(getParameter(), value);
        return result;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract String getPrefix();

    protected abstract String getHttpEventMapName();

    // Private ---------------------------------------------------------------------------------------------------------

    private void parseInternal(String formatStringLiteral) {

        if (formatStringLiteral == null) {
            throw new IllegalArgumentException("null format string literal");
        }

        String prefix = getPrefix();

        if (!formatStringLiteral.startsWith(prefix)) {
            throw new IllegalArgumentException("'" + formatStringLiteral + "' does not start with '" + prefix + "'");
        }

        formatStringLiteral = formatStringLiteral.substring(prefix.length());

        if (!formatStringLiteral.endsWith("}")) {
            throw new IllegalArgumentException("'" + formatStringLiteral + "' does not end with '}'");
        }

        String parameter = formatStringLiteral.substring(0, formatStringLiteral.length() - 1);
        setParameter(parameter);
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
