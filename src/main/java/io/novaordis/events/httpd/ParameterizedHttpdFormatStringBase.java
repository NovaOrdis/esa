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

package io.novaordis.events.httpd;

import io.novaordis.utilities.parsing.ParsingException;
import io.novaordis.events.api.event.MapProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
abstract class ParameterizedHttpdFormatStringBase implements ParameterizedHttpdFormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String literal;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param formatStringLiteral - we expect a parameterized format string specification (%{i,Some-Header},
     *                            %{Some-Header}i, %{o,Some-Header} or %{Some-Header}o) to start the given string, but
     *                            it is acceptable that other format strings follow, without any intermediary space.
     *                            They will be ignored.
     *
     * @throws IllegalArgumentException if the literal does not match the expected pattern.
     */
    protected ParameterizedHttpdFormatStringBase(String formatStringLiteral) throws IllegalArgumentException {

        parseInternal(formatStringLiteral);
    }

    // ParameterizedHttpdFormatString implementation ------------------------------------------------------------------------

    @Override
    public Object parse(String logStringRepresentation, Long lineNumber, Integer positionInLine)
            throws ParsingException {

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
    public HttpdFormatString getMatchingEnclosure() {
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

    @Override
    public String getLiteral() {

        return literal;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return getLiteral();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * "%{i," for the %{i,Something} format.
     */
    protected abstract String getPrefix();

    /**
     * "%{.+}i" for the %{Something}i format.
     */
    protected abstract Pattern getAlternateFormatPattern();

    protected abstract String getHttpEventMapName();

    // Private ---------------------------------------------------------------------------------------------------------

    private void parseInternal(String formatStringLiteral) {

        if (formatStringLiteral == null) {
            throw new IllegalArgumentException("null format string literal");
        }

        Matcher matcher;
        String parameter;
        String prefix = getPrefix();

        if (formatStringLiteral.startsWith(prefix)) {

            //
            // %{i,Some-Header} format
            //

            formatStringLiteral = formatStringLiteral.substring(prefix.length());

            int i = formatStringLiteral.indexOf('}');

            if (i == -1) {
                throw new IllegalArgumentException("'" + formatStringLiteral + "' does not end with '}'");
            }

            parameter = formatStringLiteral.substring(0, i);
            this.literal = "%{" + getPrefix().charAt(2)+ "," + parameter + "}";

        }
        else if ((matcher = getAlternateFormatPattern().matcher(formatStringLiteral)).find()) {

            //
            // %{Some-Header}i format
            //

            parameter = matcher.group(1);
            this.literal = "%{" + parameter + "}" + getPrefix().charAt(2);
        }
        else {

            throw new IllegalArgumentException("'" + formatStringLiteral +
                    "' cannot be parsed into a parameterized format string");
        }

        setParameter(parameter);
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
