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

import io.novaordis.esa.FormatElement;
import io.novaordis.esa.LogFormat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogFormat implements LogFormat {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final HttpdLogFormat COMMON = new HttpdLogFormat(
            HttpdFormatElement.REMOTE_HOST,
            HttpdFormatElement.REMOTE_LOGNAME,
            HttpdFormatElement.REMOTE_USER,
            HttpdFormatElement.TIMESTAMP);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<HttpdFormatElement> formatElements;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param formatElements duplicate format elements are acceptable.
     */
    public HttpdLogFormat(HttpdFormatElement... formatElements) {

        this.formatElements = Arrays.asList(formatElements);
    }

    // LogFormat implementation ----------------------------------------------------------------------------------------

    /**
     * @see LogFormat#getFormatElements()
     */
    @Override
    public List<HttpdFormatElement> getFormatElements() {
        return formatElements;
    }

    @Override
    public Pattern createPattern() {
        throw new RuntimeException("createPattern() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        String s = "";

        for(Iterator<HttpdFormatElement> i = formatElements.iterator(); i.hasNext(); ) {

            s += i.next().getLiteral();
            if (i.hasNext()) {
                s += " ";
            }
        }

        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
