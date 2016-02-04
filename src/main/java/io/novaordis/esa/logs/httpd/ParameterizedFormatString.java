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
 * A httpd log format string that supports a configurable parameter: %{i,xxx} (incoming headers), %{o,xxx} (outgoing
 * headers), %(c,xxx} (cookies).
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/3/16
 */
public interface ParameterizedFormatString extends FormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param individualToken - we expect an individual token here, must not include spaces.
     *
     * @return null if no known parameterized format string matches
     */
    static ParameterizedFormatString parameterizedFormatFromString(String individualToken) {

        if (individualToken.contains(" ")) {
            throw new IllegalArgumentException("'" + individualToken + "' contains spaces and it should not");
        }

        if (individualToken.startsWith(IncomingHeaderFormatString.PREFIX)) {

            return new IncomingHeaderFormatString(individualToken);
        }
        else if (individualToken.startsWith(OutgoingHeaderFormatString.PREFIX)) {

            return new OutgoingHeaderFormatString(individualToken);
        }
        else if (individualToken.startsWith(CookieFormatString.PREFIX)) {

            return new CookieFormatString(individualToken);
        }

        return null;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @see ParameterizedFormatString#getParameter()
     */
    void setParameter(String parameter);

    /**
     * @return the parameterized format string parameter - for example, if the parameterized format string is the
     * request header %i{Some-Header}, then the parameter is "Some-Header"
     */
    String getParameter();



}
