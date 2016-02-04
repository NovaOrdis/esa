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

package io.novaordis.esa.logs.httpd;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class IncomingHeaderFormatString extends ParameterizedFormatStringBase implements ParameterizedFormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String PREFIX = "%{i,";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String incomingHeaderName;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param formatStringLiteral as declared in the format specification, example: %{i,Some-Header} or
     *                            %{o,Some-Header}
     *
     * @throws IllegalArgumentException if the literal does not match the expected pattern.
     */
    public IncomingHeaderFormatString(String formatStringLiteral) throws IllegalArgumentException {
        super(formatStringLiteral);
    }

    // ParameterizedFormatString implementation ------------------------------------------------------------------------

    @Override
    public String getLiteral() {

        return PREFIX + incomingHeaderName + "}";
    }

    @Override
    public void setParameter(String parameter) {

        incomingHeaderName = parameter;
    }

    @Override
    public String getParameter() {

        return incomingHeaderName;
    }

    // ParameterizedFormatStringBase overrides -------------------------------------------------------------------------

    @Override
    protected String getPrefix() {
        return PREFIX;
    }

    @Override
    protected String getHttpEventMapName() {

        return HttpEvent.REQUEST_HEADERS;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getIncomingHeaderName() {
        return incomingHeaderName;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
