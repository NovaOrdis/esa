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
public class CookieMicroParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(CookieMicroParserTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void identifyEnd() throws Exception {

        String line = "cookie1=value1; cookie2=value2; cookie3=value3 blah";
        int startFrom = 0;

        int result = CookieMicroParser.identifyEnd(line, startFrom);
        assertEquals(46, result);
    }

    @Test
    public void identifyEnd2() throws Exception {

        String line = "cookie1=value1 blah";
        int startFrom = 0;

        int result = CookieMicroParser.identifyEnd(line, startFrom);
        assertEquals(14, result);
    }


    @Test
    public void identifyEnd_EndOfLine() throws Exception {

        String line = "cookie1=value1; cookie2=value2; cookie3=value3";
        int startFrom = 0;

        int result = CookieMicroParser.identifyEnd(line, startFrom);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_parsingFailure() throws Exception {

        String line = "blah";
        int startFrom = 0;

        try {

            CookieMicroParser.identifyEnd(line, startFrom);
            fail("should have thrown exception");
        }
        catch(ParsingException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.startsWith("no known Cookie pattern identified starting with position"));
        }
    }

    // isCookieRequestHeader() -----------------------------------------------------------------------------------------

    @Test
    public void isCookieRequestHeader() throws Exception {

        FormatString fs = FormatString.fromString("%{Cookie}i").get(0);
        assertTrue(CookieMicroParser.isCookieRequestHeader(fs));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
