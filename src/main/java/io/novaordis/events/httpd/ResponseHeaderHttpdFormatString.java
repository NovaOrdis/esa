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

import java.util.regex.Pattern;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class ResponseHeaderHttpdFormatString extends ParameterizedHttpdFormatStringBase implements ParameterizedHttpdFormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String PREFIX = "%{o,"; // handles this format %{o,Something}
    public static final Pattern ALTERNATIVE_FORMAT_PATTERN = Pattern.compile("%\\{(.+)\\}o"); // handles this format %{Something}o

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String responseHeaderName;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param formatStringLiteral - we expect a cookie format specification (%{o,Some-Header} or %{Some-Header}o) to
     *                            start the given string, but it is acceptable that other format strings follow, without
     *                            any intermediary space. They will be ignored.
     *
     * @throws IllegalArgumentException if the literal does not match the expected pattern.
     */
    public ResponseHeaderHttpdFormatString(String formatStringLiteral) throws IllegalArgumentException {
        super(formatStringLiteral);
    }

    // ParameterizedHttpdFormatString implementation ------------------------------------------------------------------------

    @Override
    public String getParameter() {

        return responseHeaderName;
    }

    @Override
    public void setParameter(String parameter) {

        responseHeaderName = parameter;
    }

    // ParameterizedHttpdFormatStringBase overrides -------------------------------------------------------------------------

    @Override
    protected String getPrefix() {
        return PREFIX;
    }

    @Override
    protected Pattern getAlternateFormatPattern() {
        return ALTERNATIVE_FORMAT_PATTERN;
    }

    @Override
    protected String getHttpEventMapName() {

        return HttpEvent.RESPONSE_HEADERS;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getHeaderName() {
        return responseHeaderName;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
