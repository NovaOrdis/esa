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
public class RequestHeaderFormatString extends ParameterizedFormatStringBase implements ParameterizedFormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String PREFIX = "%{i,"; // handles this format %{i,Something}
    public static final Pattern ALTERNATIVE_FORMAT_PATTERN = Pattern.compile("%\\{(.+)\\}i"); // handles this format %{Something}i

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String requestHeaderName;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param formatStringLiteral - we expect a cookie format specification (%{i,Some-Header}) to start the given
     *                            string, but it is acceptable that other format strings follow, without any
     *                            intermediary space. They will be ignored.
     *
     * @throws IllegalArgumentException if the literal does not match the expected pattern.
     */
    public RequestHeaderFormatString(String formatStringLiteral) throws IllegalArgumentException {
        super(formatStringLiteral);
    }

    // ParameterizedFormatString implementation ------------------------------------------------------------------------

    @Override
    public void setParameter(String parameter) {

        requestHeaderName = parameter;
    }

    @Override
    public String getParameter() {

        return requestHeaderName;
    }

    // ParameterizedFormatStringBase overrides -------------------------------------------------------------------------

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

        return HttpEvent.REQUEST_HEADERS;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the header name as read from the format string, maintaining the original capitalization.
     */
    public String getHeaderName() {
        return requestHeaderName;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
