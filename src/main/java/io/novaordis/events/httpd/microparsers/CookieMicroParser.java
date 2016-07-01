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

package io.novaordis.events.httpd.microparsers;

import io.novaordis.events.ParsingException;
import io.novaordis.events.httpd.FormatString;
import io.novaordis.events.httpd.RequestHeaderFormatString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A micro-parser is useful for particular fields that contain spaces and that were written in the log without being
 * embedded by quotes. It applies field-specific heuristics to figure out the end of the field.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/30/16
 */
public class CookieMicroParser {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String HEADER_NAME = "Cookie";

    //
    // Known user agent patterns
    //

    public static final Pattern[] COOKIE_PATTERNS = new Pattern[] {

            // "cookie1=value1; cookie2=value2; cookie3=value3 "
            Pattern.compile("^([\\w\\.]+=[^;]+; )*([\\w\\.]+=[^; ]+ {0,1})")
    };

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @param startFrom the index of the first character of the field
     * @throws ParsingException in case the content on the line does not make sense for this type of field.
     *
     * @return the index of the character immediately following the field. The character could be a space. If the
     * line ends, -1 is returned.
     *
     * @exception  ParsingException if no known pattern was identified
     */
    public static int identifyEnd(String line, int startFrom) throws ParsingException {

        //
        // no quotes, and the value includes multiple spaces
        //

        String interestingSection = line.substring(startFrom);

        for(Pattern p: COOKIE_PATTERNS) {

            Matcher m = p.matcher(interestingSection);

            if (m.find()) {

                int end = startFrom + m.end();

                if (end >= line.length()) {

                    return -1;
                }

                //
                // the regular expression catches the trailing space, if present; to maintain the method's semantics
                // we "decrement" the end if this is the case
                //
                if (line.charAt(end - 1) == ' ') {
                    end --;
                }

                return end;
            }
        }

        throw new ParsingException("no known Cookie pattern \"" + line.substring(startFrom) + "\"");
    }

    // TODO identical with UserAgentMicroParser.isUserAgentRequestHeader()
    public static boolean isCookieRequestHeader(FormatString fs) {

        if (fs == null) {
            return false;
        }

        if (!(fs instanceof RequestHeaderFormatString)) {
            return false;
        }

        RequestHeaderFormatString rhfs = (RequestHeaderFormatString)fs;
        return HEADER_NAME.equalsIgnoreCase(rhfs.getHeaderName());
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private CookieMicroParser() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
