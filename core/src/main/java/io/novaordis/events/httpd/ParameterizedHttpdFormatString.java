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
public interface ParameterizedHttpdFormatString extends HttpdFormatString {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param tokens - we expect a parameterized format string to start the given string, but it is possible that other
     *               format strings follow, without any intermediary space. The "tokens" string must not include spaces.
     *
     * @return null if no known parameterized format string matches, or a valid ParameterizedHttpdFormatString instance if
     * the literal representation of that parameterized format string was found <b>at the beginning</b> of the argument.
     */
    static ParameterizedHttpdFormatString parameterizedFormatFromString(String tokens) {

        if (tokens.contains(" ")) {
            throw new IllegalArgumentException("'" + tokens + "' contains spaces and it should not");
        }

        // TODO inefficient, we do matching twice, once here and once inside the instance. Refactor.

        if (tokens.startsWith(RequestHeaderHttpdFormatString.PREFIX) ||
                RequestHeaderHttpdFormatString.ALTERNATIVE_FORMAT_PATTERN.matcher(tokens).matches()) {

            return new RequestHeaderHttpdFormatString(tokens);
        }
        else if (tokens.startsWith(ResponseHeaderHttpdFormatString.PREFIX) ||
                ResponseHeaderHttpdFormatString.ALTERNATIVE_FORMAT_PATTERN.matcher(tokens).matches()) {

            return new ResponseHeaderHttpdFormatString(tokens);
        }
        else if (tokens.startsWith(CookieHttpdFormatString.PREFIX) ||
                CookieHttpdFormatString.ALTERNATIVE_FORMAT_PATTERN.matcher(tokens).matches()) {

            return new CookieHttpdFormatString(tokens);
        }

        return null;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @see ParameterizedHttpdFormatString#getParameter()
     */
    void setParameter(String parameter);

    /**
     * @return the parameterized format string parameter - for example, if the parameterized format string is the
     * request header %i{Some-Header}, then the parameter is "Some-Header"
     */
    String getParameter();



}
