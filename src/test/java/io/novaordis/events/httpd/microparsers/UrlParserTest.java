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
public class UrlParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(UrlParserTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void identifyEnd_NoProtocolPathSeparator() throws Exception {

        String line = "something";
        int startFrom = 0;

        HttpdFormatString fs = HttpdFormatString.fromString("%{Something}i").get(0);

        try {

            UrlParser.identifyEnd(line, startFrom, fs, 77L);
            fail("should have throw exception");
        }
        catch(ParsingException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("'://' missing from %{Something}i URL representation", msg);
            assertEquals(77L, e.getLineNumber().longValue());
            assertEquals(0, e.getPositionInLine().intValue());
        }
    }

    @Test
    public void identifyEnd_NoValue() throws Exception {

        String line = "- ";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(1, result);
    }

    @Test
    public void identifyEnd_NoValue_EndOfTheLine() throws Exception {

        String line = "-";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_Host() throws Exception {

        String line = "http://something";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_Host2() throws Exception {

        String line = "http://something/";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_Path() throws Exception {

        String line = "http://example.com/a/b/c";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_SpaceInPath() throws Exception {

        //
        // space in paths are NOT allowed - a space in path indicates the end of the URL
        //
        String line = "http://a.com/a/b c/d";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(16, result);
    }

    @Test
    public void identifyEnd_EmptyQuery() throws Exception {

        String line = "http://example.com/a?";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_QueryWithoutSeparator() throws Exception {

        String line = "http://a.com/b?k1=v1 ";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(20, result);
    }

    @Test
    public void identifyEnd_RegularQuery() throws Exception {

        String line = "http://a.com/b?k1=v1&k2=v2";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_RegularQuery_LeadingSpace() throws Exception {

        String line = " http://a.com/b?k1=v1&k2=v2";
        int startFrom = 1;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_RegularQuery2() throws Exception {

        String line = "http://a.com/b?k1=v1&k2=v2 ";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(26, result);
    }

    @Test
    public void identifyEnd_SpaceInQueryElement() throws Exception {

        String line = "http://a.com/b?k1=v v v v&k2=v2";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_SpaceInQueryElement2() throws Exception {

        String line = "http://a.com/b?k1=v1 v1&k2=v2 v2&k3=v3";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_Query_OtherUrl() throws Exception {

        String line = "http://a.com/b?k1=v1&k2=v2 http://b.com?k1=v1&k2=v2";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(26, result);
    }

    @Test
    public void identifyEnd_Query_OtherFragment() throws Exception {

        String line = "http://a.com/b?k1=v1&k2=v2 something;&";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(26, result);
    }

    @Test
    public void identifyEnd_SpaceInQueryElement3() throws Exception {

        String line =
                "http://www.example.com/a/b/c?ID=1&Category=2&Key1=Value1&Key2=value contains spaces&Key3=Value3 OtherFieldStartsHere";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(line.length() - "OtherFieldStartsHere".length() - 1, result);
    }

    @Test
    public void identifyEnd_PortNumber() throws Exception {

        String line = "http://something:80/";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }

    @Test
    public void identifyEnd_PortNumber2() throws Exception {

        String line = "http://something:80";
        int startFrom = 0;

        int result = UrlParser.identifyEnd(line, startFrom, null, null);
        assertEquals(-1, result);
    }


    // isUrl() ---------------------------------------------------------------------------------------------------------

    @Test
    public void isUrl_Referer() throws Exception {

        HttpdFormatString fs = HttpdFormatString.fromString("%{Referer}i").get(0);
        assertTrue(UrlParser.isUrl(fs));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
