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

package io.novaordis.events.httpd.microparsers.cookie;

import io.novaordis.events.ParsingException;
import io.novaordis.events.httpd.HttpdFormatString;
import io.novaordis.events.httpd.RequestHeaderHttpdFormatString;
import io.novaordis.events.httpd.ResponseHeaderHttpdFormatString;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/30/16
 */
public class CookieParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(CookieParserTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void identifyEnd() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "cookie1=value1; cookie2=value2; cookie3=value3 blah";

        int startFrom = 0;

        int result = CookieParser.identifyEnd(line, startFrom, fs, null);

        assertEquals(46, result);
    }

    @Test
    public void identifyEnd_NoValue() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "something - something else";

        int startFrom = 10;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(11 , result);
    }

    @Test
    public void identifyEnd_LeadingString() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "blah A=B; C=D blah";

        int startFrom = 5;

        int result = CookieParser.identifyEnd(line, startFrom, fs, null);

        assertEquals(13, result);
    }

    @Test
    public void identifyEnd2() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "cookie1=value1 blah";
        int startFrom = 0;

        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(14, result);
    }

    @Test
    public void identifyEnd_EndOfLine() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "cookie1=value1; cookie2=value2; cookie3=value3";
        int startFrom = 0;

        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_parsingFailure() throws Exception {

        String line = "blah";
        int startFrom = 0;

        RequestHeaderHttpdFormatString fs = new RequestHeaderHttpdFormatString("%{Something}i");

        try {

            CookieParser.identifyEnd(line, startFrom, fs, 7L);
            fail("should have thrown exception");
        }
        catch(ParsingException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.equals("%{Something}i missing"));
            assertEquals(7L, e.getLineNumber().longValue());
            assertEquals(0, e.getPositionInLine().intValue());
        }
    }

    @Test
    public void identifyEnd3() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "something.somethingelse=value1";

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd4() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "Expires=Thu,01-Jan-1970 00:00:10 GMT; something=something-else";

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(-1 , result);
    }

    @Test
    public void identifyEnd5() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "Secure,online_uid=121212; something=something-else";

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(-1 , result);
    }

    @Test
    public void identifyEnd6() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "A=B; C=D E=F";

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(8 , result);
    }

    @Test
    public void identifyEnd6_1() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "A=B; C=D NewField=Value; something=something";

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(8 , result);
    }

    @Test
    public void identifyEnd7() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Set-Cookie}o").get(0);

        String line = "Path=/,SignedIn=1; Domain=.example.com";

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(-1 , result);
    }

    @Test
    public void identifyEnd_DoubleBackspacesInString() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        String line = "A=\\\\; B=C D=E;";

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(9 , result);
    }

    /**
     * There are situations when the Cookie input header is not quoted, does not end with a non-semicolon space and
     * it is immediately followed by the Set-Cookie output header, so we can't say where one ends and the other begins.
     * We apply heuristics by looking for Domain/Path and we allocate that to the output Set-Cookie.
     */
    @Test
    public void identifyEnd_OutputCookieFollows() throws Exception {

        String line = "INPUT_COOKIE_KEY1=Value1; SET_COOKIE_KEY1=Value2; Domain=something; Path=/";

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(25 , result);
    }

    @Test
    public void identifyEnd_SemicolonTrailingAfterLastCookie_EndOfLine() throws Exception {

        String line = "A=B; C=D;";

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(-1 , result);
    }

    @Test
    public void identifyEnd_SemicolonTrailingAfterLastCookie_NoValue() throws Exception {

        String line = "A=B; C=D; -";

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(9 , result);
    }

    @Test
    public void identifyEnd_SemicolonTrailingAfterLastCookie_Digit() throws Exception {

        String line = "A=B; C=D; 7";

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(9 , result);
    }

    @Test
    public void identifyEnd_CommaSeparator() throws Exception {

        String line = "A=B, C=D, E=F ";

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(13 , result);
    }

    @Test
    public void identifyEnd_CommaSeparator_2() throws Exception {

        String line = "A=B, C=D, E=F";

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(-1 , result);
    }

    @Test
    public void identifyEnd_CommaSeparator_3() throws Exception {

        String line = "A=B, C=D, E=F G=H; Domain=something; ";

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);

        int startFrom = 0;
        int result = CookieParser.identifyEnd(line, startFrom, fs, null);
        assertEquals(13 , result);
    }

    // identifyEndOfTheCookieSeries() ----------------------------------------------------------------------------------

    @Test
    public void identifyEndOfTheCookieSeries() throws Exception {

        //
        // first space after the first equal sign
        //
        int i = CookieParser.identifyEndOfTheCookieSeries(" A=B C", 0, null, null);
        assertEquals(4, i);
    }

    @Test
    public void identifyEndOfTheCookieSeries_NoEqualSign() throws Exception {

        HttpdFormatString fs = new ResponseHeaderHttpdFormatString("%{Some-Header}o");

        try {

            CookieParser.identifyEndOfTheCookieSeries(" blah", 0, fs, 7L);
            fail("should have thrown exception");
        }
        catch(ParsingException e) {
            String msg = e.getMessage();
            assertTrue(msg.equals("%{Some-Header}o missing"));
            assertEquals(0, e.getPositionInLine().intValue());
            assertEquals(7L, e.getLineNumber().longValue());
        }
    }

    @Test
    public void identifyEndOfTheCookieSeries_NoSpaceAfterTheEqualSign() throws Exception {

        int i = CookieParser.identifyEndOfTheCookieSeries(" A=B", 0, null, null);
        assertEquals(-1, i);
    }

    @Test
    public void identifyEndOfTheCookieSeries_NoValueFollows() throws Exception {

        int i = CookieParser.identifyEndOfTheCookieSeries(" -", 0, null, null);
        assertEquals(0, i);
    }

    @Test
    public void identifyEndOfTheCookieSeries_NoWordBeginningFollows() throws Exception {

        int i = CookieParser.identifyEndOfTheCookieSeries(" 7", 0, null, null);
        assertEquals(0, i);
    }


    // identifyEndOfTheCookieSeries() ----------------------------------------------------------------------------------

    @Test
    public void identifyBoundaryBetweenSeries() throws Exception {

        int i = CookieParser.identifyBoundaryBetweenSeries(" C=D NewField=Value");
        assertEquals(4, i);
    }

    @Test
    public void identifyBoundaryBetweenSeries_NoBoundary() throws Exception {

        int i = CookieParser.identifyBoundaryBetweenSeries(" A=B blah blah");
        assertEquals(-1, i);
    }

    // isCookieHeader() ------------------------------------------------------------------------------------------------

    @Test
    public void isCookieHeader() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);
        assertTrue(CookieParser.isCookieHeader(fs));
    }

    @Test
    public void isCookieHeader_SetCookie() throws Exception {

        //
        // we also use the Cookie parser for Set-Cookie response headers values
        //
        HttpdFormatString fs = HttpdFormatString.fromString("%{SET-COOKIE}o").get(0);
        assertTrue(CookieParser.isCookieHeader(fs));
    }

    @Test
    public void isCookieHeader_SetCookie2() throws Exception {

        //
        // we also use the Cookie parser for Set-Cookie response headers values
        //
        HttpdFormatString fs = HttpdFormatString.fromString("%{Set-Cookie}o").get(0);
        assertTrue(CookieParser.isCookieHeader(fs));
    }

    // isCookieRequestHeader() -----------------------------------------------------------------------------------------

    @Test
    public void isCookieRequestHeader() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);
        assertTrue(CookieParser.isCookieRequestHeader(fs));
    }

    @Test
    public void isCookieRequestHeader_SetCookie() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Set-Cookie}o").get(0);
        assertFalse(CookieParser.isCookieRequestHeader(fs));
    }

    // isCookieResponseHeader() ----------------------------------------------------------------------------------------

    @Test
    public void isCookieResponseHeader() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Set-Cookie}o").get(0);
        assertTrue(CookieParser.isCookieResponseHeader(fs));
    }

    @Test
    public void isCookieResponseHeader_Cookie() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Cookie}i").get(0);
        assertFalse(CookieParser.isCookieResponseHeader(fs));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
