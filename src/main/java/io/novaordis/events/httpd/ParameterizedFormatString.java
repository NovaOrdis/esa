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

/**
 * A httpd log format string that supports a configurable parameter: %{i,xxx} (incoming/request headers), %{o,xxx}
 * (outgoing/response headers), %(c,xxx} (cookies).
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/3/16
 */
public interface ParameterizedFormatString extends FormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param tokens - we expect a parameterized format string to start the given string, but it is possible that other
     *               format strings follow, without any intermediary space. The "tokens" string must not include spaces.
     *
     * @return null if no known parameterized format string matches, or a valid ParameterizedFormatString instance if
     * the literal representation of that parameterized format string was found <b>at the beginning</b> of the argument.
     */
    static ParameterizedFormatString parameterizedFormatFromString(String tokens) {

        if (tokens.contains(" ")) {
            throw new IllegalArgumentException("'" + tokens + "' contains spaces and it should not");
        }

        if (tokens.startsWith(RequestHeaderFormatString.PREFIX)) {

            return new RequestHeaderFormatString(tokens);
        }
        else if (tokens.startsWith(ResponseHeaderFormatString.PREFIX)) {

            return new ResponseHeaderFormatString(tokens);
        }
        else if (tokens.startsWith(CookieFormatString.PREFIX)) {

            return new CookieFormatString(tokens);
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
