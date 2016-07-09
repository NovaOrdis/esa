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
import io.novaordis.events.httpd.HttpdFormatString;
import io.novaordis.events.httpd.RequestHeaderHttpdFormatString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser is useful for particular fields that contain spaces and that were written in the log without being
 * embedded by quotes. It applies field-specific heuristics to figure out the end of the field.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/30/16
 */
public class UserAgentParser {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String HEADER_NAME = "User-Agent";

    //
    // Known user agent patterns
    //

    public static final Pattern[] USER_AGENT_PATTERNS = new Pattern[] {

            //
            // order is important, do not modify
            //

            // ... Mobile Safari/534.11+
            // ... Mobile/12A405"
            Pattern.compile("^.+ Mobile(/\\w+){0,1}( Safari/[\\d\\.\\+]+){0,1}"),

            // "Mozilla/5.0 (...) like Gecko"
            Pattern.compile("^\\w+/[\\d\\.\\+]+ \\(.+\\) like Gecko"),

            // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.55.3         (KHTML, like Gecko) Version/5.1.5 Safari/534.55.3"
            Pattern.compile("^Mozilla.*Safari/[\\d\\.\\+]+( \\w+/[\\d\\.]+)*"),

            // "Mozilla/4.0 (compatible; MSIE 8.0; ...; ...; ...) Firefox/3.0.11 ..."
            // "Mozilla_CA/4.79 [en] (...)";
            // "check_http/v2.0.3 (nagios-plugins 2.0.3)"
            Pattern.compile("^(\\w+/v{0,1}[\\d\\.\\+]+( \\[.+\\]){0,1} (\\([^\\)]+\\)){0,1} {0,1})+"),

            // Java/1.7.0_51
            Pattern.compile("^\\w+/[\\d\\._]+"),


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
    public static int identifyEnd(String line, int startFrom, Long lineNumber) throws ParsingException {

        //
        // no quotes, and the value includes multiple spaces
        //

        String interestingSection = line.substring(startFrom);

        for(Pattern p: USER_AGENT_PATTERNS) {

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

        int intendedFragmentLength = 60;
        int fragmentLength = line.length();
        fragmentLength = fragmentLength < intendedFragmentLength ? fragmentLength : intendedFragmentLength;

        throw new ParsingException(
                "no known User-Agent pattern identified in \"" +
                        line.substring(startFrom, startFrom + fragmentLength) + "...\"",
                lineNumber, startFrom);
    }

    public static boolean isUserAgentRequestHeader(HttpdFormatString fs) {

        if (fs == null) {
            return false;
        }

        if (!(fs instanceof RequestHeaderHttpdFormatString)) {
            return false;
        }

        RequestHeaderHttpdFormatString rhfs = (RequestHeaderHttpdFormatString)fs;
        return HEADER_NAME.equalsIgnoreCase(rhfs.getHeaderName());
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private UserAgentParser() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
