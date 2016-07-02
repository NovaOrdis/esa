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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/30/16
 */
public class UserAgentParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(UserAgentParserTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void identifyEnd() throws Exception {

        String line = "Mozilla/4.0 (compatible; MSIE 8.0) blah";
        int startFrom = 0;

        int result = UserAgentParser.identifyEnd(line, startFrom, null);
        assertEquals(34, result);
    }

    @Test
    public void identifyEnd2() throws Exception {

        String line =
                "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729) blah";
        int startFrom = 0;

        int result = UserAgentParser.identifyEnd(line, startFrom, null);
        assertEquals(133, result);
    }

    @Test
    public void identifyEnd_EndOfLine() throws Exception {

        String line = "Mozilla/4.0 (compatible; MSIE 8.0)";
        int startFrom = 0;

        int result = UserAgentParser.identifyEnd(line, startFrom, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_parsingFailure() throws Exception {

        String line = "blah";
        int startFrom = 0;

        try {

            UserAgentParser.identifyEnd(line, startFrom, 7L);
            fail("should have thrown exception");
        }
        catch(ParsingException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.startsWith("no known User-Agent pattern identified"));
            assertEquals(7L, e.getLineNumber().longValue());
            assertEquals(0, e.getPositionInLine().intValue());
        }
    }

    @Test
    public void production() throws Exception {

        String value = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 something";

        int result = UserAgentParser.identifyEnd(value, 0, null);
        assertEquals(value.length() - " something".length(), result);
    }

    @Test
    public void production2() throws Exception {

        String value =
                "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36 something";
        int result = UserAgentParser.identifyEnd(value, 0, null);
        assertEquals(value.length() - " something".length(), result);
    }

    @Test
    public void production3() throws Exception {

        String value =  "Java/1.7.0_51 ";
        int result = UserAgentParser.identifyEnd(value, 0, null);
        assertEquals(13, result);
    }

    @Test
    public void production4() throws Exception {

        String value =  "Mozilla/5.0 (compatible, MSIE 11, Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko";
        int result = UserAgentParser.identifyEnd(value, 0, null);
        assertEquals(-1, result);
    }

    @Test
    public void production5() throws Exception {

        String value =  "Mozilla_CA/4.79 [en] (X11; U; Linux 2.4.18-3d_epoll i686)";
        int result = UserAgentParser.identifyEnd(value, 0, null);
        assertEquals(-1, result);
    }


    // isUserAgentRequestHeader() --------------------------------------------------------------------------------------

    @Test
    public void isUserAgentRequestHeader() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{User-Agent}i").get(0);
        assertTrue(UserAgentParser.isUserAgentRequestHeader(fs));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
