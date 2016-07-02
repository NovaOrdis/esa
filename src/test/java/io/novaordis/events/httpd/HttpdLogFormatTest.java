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

import io.novaordis.events.core.LineFormat;
import io.novaordis.events.core.LineFormatTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogFormatTest extends LineFormatTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLogFormatTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // constructors ----------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        // duplicate format elements - this should be acceptable
        HttpdLogFormat f = new HttpdLogFormat(HttpdFormatStrings.REMOTE_HOST, HttpdFormatStrings.REMOTE_HOST);

        List<HttpdFormatString> httpdFormatStrings = f.getHttpdFormatStrings();
        assertEquals(2, httpdFormatStrings.size());
        assertEquals(HttpdFormatStrings.REMOTE_HOST, httpdFormatStrings.get(0));
        assertEquals(HttpdFormatStrings.REMOTE_HOST, httpdFormatStrings.get(1));
    }

    @Test
    public void constructor_FormatAsString() throws Exception {

        String s = HttpdLogFormat.COMMON.toString();
        HttpdLogFormat f = new HttpdLogFormat(s);

        List<HttpdFormatString> httpdFormatStrings = f.getHttpdFormatStrings();

        assertEquals(HttpdFormatStrings.REMOTE_HOST, httpdFormatStrings.get(0));
        assertEquals(HttpdFormatStrings.REMOTE_LOGNAME, httpdFormatStrings.get(1));
        assertEquals(HttpdFormatStrings.REMOTE_USER, httpdFormatStrings.get(2));
        assertEquals(HttpdFormatStrings.OPENING_BRACKET, httpdFormatStrings.get(3));
        assertEquals(HttpdFormatStrings.TIMESTAMP, httpdFormatStrings.get(4));
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, httpdFormatStrings.get(5));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(6));
        assertEquals(HttpdFormatStrings.FIRST_REQUEST_LINE, httpdFormatStrings.get(7));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(8));
        assertEquals(HttpdFormatStrings.STATUS_CODE, httpdFormatStrings.get(9));
        assertEquals(HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE, httpdFormatStrings.get(10));
    }

    @Test
    public void constructor_FormatAsString2() throws Exception {

        String s = HttpdLogFormat.PERFORMANCE_ANALYSIS.toString();
        HttpdLogFormat f = new HttpdLogFormat(s);

        List<HttpdFormatString> httpdFormatStrings = f.getHttpdFormatStrings();

        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(0));
        assertEquals(HttpdFormatStrings.THREAD_NAME, httpdFormatStrings.get(1));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(2));
        assertEquals(HttpdFormatStrings.REMOTE_HOST, httpdFormatStrings.get(3));
        assertEquals(HttpdFormatStrings.REMOTE_USER, httpdFormatStrings.get(4));
        assertEquals(HttpdFormatStrings.OPENING_BRACKET, httpdFormatStrings.get(5));
        assertEquals(HttpdFormatStrings.TIMESTAMP, httpdFormatStrings.get(6));
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, httpdFormatStrings.get(7));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(8));
        assertEquals(HttpdFormatStrings.FIRST_REQUEST_LINE, httpdFormatStrings.get(9));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(10));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(11));
        assertEquals(HttpdFormatStrings.QUERY_STRING, httpdFormatStrings.get(12));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(13));
        assertEquals(HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE, httpdFormatStrings.get(14));
        assertEquals(HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE, httpdFormatStrings.get(15));
        assertEquals(HttpdFormatStrings.REQUEST_PROCESSING_TIME_MS, httpdFormatStrings.get(16));
    }

    @Test
    public void constructor_SpecialHTMLCharacter() throws Exception {

        String s = "&quot;%I&quot; %h %u [%t] &quot;%r&quot; &quot;%q&quot; %s %b %D";

        HttpdLogFormat f = new HttpdLogFormat(s);

        List<HttpdFormatString> httpdFormatStrings = f.getHttpdFormatStrings();

        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(0));
        assertEquals(HttpdFormatStrings.THREAD_NAME, httpdFormatStrings.get(1));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(2));
        assertEquals(HttpdFormatStrings.REMOTE_HOST, httpdFormatStrings.get(3));
        assertEquals(HttpdFormatStrings.REMOTE_USER, httpdFormatStrings.get(4));
        assertEquals(HttpdFormatStrings.OPENING_BRACKET, httpdFormatStrings.get(5));
        assertEquals(HttpdFormatStrings.TIMESTAMP, httpdFormatStrings.get(6));
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, httpdFormatStrings.get(7));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(8));
        assertEquals(HttpdFormatStrings.FIRST_REQUEST_LINE, httpdFormatStrings.get(9));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(10));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(11));
        assertEquals(HttpdFormatStrings.QUERY_STRING, httpdFormatStrings.get(12));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(13));
        assertEquals(HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE, httpdFormatStrings.get(14));
        assertEquals(HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE, httpdFormatStrings.get(15));
        assertEquals(HttpdFormatStrings.REQUEST_PROCESSING_TIME_MS, httpdFormatStrings.get(16));
    }

    @Test
    public void unbalancedDoubleQuotes() throws Exception {

        try {
            new HttpdLogFormat(HttpdFormatStrings.DOUBLE_QUOTES);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void unbalancedDoubleQuotes2() throws Exception {

        try {
            new HttpdLogFormat(
                    HttpdFormatStrings.REMOTE_HOST,
                    HttpdFormatStrings.DOUBLE_QUOTES,
                    HttpdFormatStrings.TIMESTAMP);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void unbalancedSingleQuotes() throws Exception {

        try {
            new HttpdLogFormat(HttpdFormatStrings.SINGLE_QUOTE);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void constructor_BracketsAroundTimestampAreImplied() throws Exception {

        HttpdLogFormat f = new HttpdLogFormat(HttpdFormatStrings.TIMESTAMP);

        List<HttpdFormatString> fs = f.getHttpdFormatStrings();
        assertEquals(3, fs.size());

        assertEquals(HttpdFormatStrings.OPENING_BRACKET, fs.get(0));
        assertEquals(HttpdFormatStrings.TIMESTAMP, fs.get(1));
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, fs.get(2));
    }

    @Test
    public void constructor_BracketsAroundTimestampAreImplied_2() throws Exception {

        HttpdLogFormat f = new HttpdLogFormat(
                HttpdFormatStrings.LOCAL_SERVER_NAME,
                HttpdFormatStrings.TIMESTAMP,
                HttpdFormatStrings.LOCAL_IP_ADDRESS);

        List<HttpdFormatString> fs = f.getHttpdFormatStrings();
        assertEquals(5, fs.size());

        assertEquals(HttpdFormatStrings.LOCAL_SERVER_NAME, fs.get(0));
        assertEquals(HttpdFormatStrings.OPENING_BRACKET, fs.get(1));
        assertEquals(HttpdFormatStrings.TIMESTAMP, fs.get(2));
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, fs.get(3));
        assertEquals(HttpdFormatStrings.LOCAL_IP_ADDRESS, fs.get(4));
    }

    @Test
    public void constructor_BracketsAroundTimestampAreImplied_ParsingSpecificationFromString() throws Exception {

        HttpdLogFormat f = new HttpdLogFormat("%t");

        List<HttpdFormatString> fs = f.getHttpdFormatStrings();
        assertEquals(3, fs.size());

        assertEquals(HttpdFormatStrings.OPENING_BRACKET, fs.get(0));
        assertEquals(HttpdFormatStrings.TIMESTAMP, fs.get(1));
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, fs.get(2));
    }


    // getHttpdFormatStrings() ----------------------------------------------------------------------------------------------

    @Test
    public void getFormatStrings_ReturnsTheUnderlyingStorage() throws Exception {

        HttpdFormatString fs = new MockHttpdFormatString("A");
        HttpdFormatString fs2 = new MockHttpdFormatString("b");

        HttpdLogFormat logFormat = new HttpdLogFormat(fs, fs2);

        List<HttpdFormatString> fes = logFormat.getHttpdFormatStrings();

        assertEquals(2, fes.size());
        assertEquals(fs, fes.get(0));
        assertEquals(fs2, fes.get(1));

        // test mutability
        fes.set(0, null);

        List<HttpdFormatString> fes2 = logFormat.getHttpdFormatStrings();

        assertEquals(2, fes2.size());
        assertNull(fes.get(0));
        assertEquals(fs2, fes.get(1));
    }

    // replaceSpecialHTMLCharacters() ----------------------------------------------------------------------------------


    @Test
    public void replaceSpecialHTMLCharacters_NoSpecialCharacter() throws Exception {

        assertEquals("abc", HttpdLogFormat.replaceSpecialHTMLCharacters("abc"));
    }

    @Test
    public void replaceSpecialHTMLCharacters_Quot() throws Exception {

        assertEquals("\"a\" b \"c\"", HttpdLogFormat.replaceSpecialHTMLCharacters("&quot;a&quot; b &quot;c&quot;"));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected LineFormat getLineFormatToTest(String formatSpecification) throws Exception {

        return new HttpdLogFormat(formatSpecification);
    }

    @Override
    protected String getFormatSpecificationForLineFormatToTest() throws Exception {

        return "[%t] %h";
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
