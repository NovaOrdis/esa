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
        HttpdLogFormat f = new HttpdLogFormat(FormatStrings.REMOTE_HOST, FormatStrings.REMOTE_HOST);

        List<FormatString> formatStrings = f.getFormatStrings();
        assertEquals(2, formatStrings.size());
        assertEquals(FormatStrings.REMOTE_HOST, formatStrings.get(0));
        assertEquals(FormatStrings.REMOTE_HOST, formatStrings.get(1));
    }

    @Test
    public void constructor_FormatAsString() throws Exception {

        String s = HttpdLogFormat.COMMON.toString();
        HttpdLogFormat f = new HttpdLogFormat(s);

        List<FormatString> formatStrings = f.getFormatStrings();

        assertEquals(FormatStrings.REMOTE_HOST, formatStrings.get(0));
        assertEquals(FormatStrings.REMOTE_LOGNAME, formatStrings.get(1));
        assertEquals(FormatStrings.REMOTE_USER, formatStrings.get(2));
        assertEquals(FormatStrings.OPENING_BRACKET, formatStrings.get(3));
        assertEquals(FormatStrings.TIMESTAMP, formatStrings.get(4));
        assertEquals(FormatStrings.CLOSING_BRACKET, formatStrings.get(5));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(6));
        assertEquals(FormatStrings.FIRST_REQUEST_LINE, formatStrings.get(7));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(8));
        assertEquals(FormatStrings.STATUS_CODE, formatStrings.get(9));
        assertEquals(FormatStrings.RESPONSE_ENTITY_BODY_SIZE, formatStrings.get(10));
    }

    @Test
    public void constructor_FormatAsString2() throws Exception {

        String s = HttpdLogFormat.PERFORMANCE_ANALYSIS.toString();
        HttpdLogFormat f = new HttpdLogFormat(s);

        List<FormatString> formatStrings = f.getFormatStrings();

        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(0));
        assertEquals(FormatStrings.THREAD_NAME, formatStrings.get(1));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(2));
        assertEquals(FormatStrings.REMOTE_HOST, formatStrings.get(3));
        assertEquals(FormatStrings.REMOTE_USER, formatStrings.get(4));
        assertEquals(FormatStrings.OPENING_BRACKET, formatStrings.get(5));
        assertEquals(FormatStrings.TIMESTAMP, formatStrings.get(6));
        assertEquals(FormatStrings.CLOSING_BRACKET, formatStrings.get(7));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(8));
        assertEquals(FormatStrings.FIRST_REQUEST_LINE, formatStrings.get(9));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(10));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(11));
        assertEquals(FormatStrings.QUERY_STRING, formatStrings.get(12));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(13));
        assertEquals(FormatStrings.ORIGINAL_REQUEST_STATUS_CODE, formatStrings.get(14));
        assertEquals(FormatStrings.RESPONSE_ENTITY_BODY_SIZE, formatStrings.get(15));
        assertEquals(FormatStrings.REQUEST_PROCESSING_TIME_MS, formatStrings.get(16));
    }

    @Test
    public void constructor_SpecialHTMLCharacter() throws Exception {

        String s = "&quot;%I&quot; %h %u [%t] &quot;%r&quot; &quot;%q&quot; %s %b %D";

        HttpdLogFormat f = new HttpdLogFormat(s);

        List<FormatString> formatStrings = f.getFormatStrings();

        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(0));
        assertEquals(FormatStrings.THREAD_NAME, formatStrings.get(1));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(2));
        assertEquals(FormatStrings.REMOTE_HOST, formatStrings.get(3));
        assertEquals(FormatStrings.REMOTE_USER, formatStrings.get(4));
        assertEquals(FormatStrings.OPENING_BRACKET, formatStrings.get(5));
        assertEquals(FormatStrings.TIMESTAMP, formatStrings.get(6));
        assertEquals(FormatStrings.CLOSING_BRACKET, formatStrings.get(7));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(8));
        assertEquals(FormatStrings.FIRST_REQUEST_LINE, formatStrings.get(9));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(10));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(11));
        assertEquals(FormatStrings.QUERY_STRING, formatStrings.get(12));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(13));
        assertEquals(FormatStrings.ORIGINAL_REQUEST_STATUS_CODE, formatStrings.get(14));
        assertEquals(FormatStrings.RESPONSE_ENTITY_BODY_SIZE, formatStrings.get(15));
        assertEquals(FormatStrings.REQUEST_PROCESSING_TIME_MS, formatStrings.get(16));
    }

    @Test
    public void unbalancedDoubleQuotes() throws Exception {

        try {
            new HttpdLogFormat(FormatStrings.DOUBLE_QUOTES);
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
                    FormatStrings.REMOTE_HOST,
                    FormatStrings.DOUBLE_QUOTES,
                    FormatStrings.TIMESTAMP);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void unbalancedSingleQuotes() throws Exception {

        try {
            new HttpdLogFormat(FormatStrings.SINGLE_QUOTE);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void constructor_BracketsAroundTimestampAreImplied() throws Exception {

        HttpdLogFormat f = new HttpdLogFormat(FormatStrings.TIMESTAMP);

        List<FormatString> fs = f.getFormatStrings();
        assertEquals(3, fs.size());

        assertEquals(FormatStrings.OPENING_BRACKET, fs.get(0));
        assertEquals(FormatStrings.TIMESTAMP, fs.get(1));
        assertEquals(FormatStrings.CLOSING_BRACKET, fs.get(2));
    }

    @Test
    public void constructor_BracketsAroundTimestampAreImplied_2() throws Exception {

        HttpdLogFormat f = new HttpdLogFormat(
                FormatStrings.LOCAL_SERVER_NAME,
                FormatStrings.TIMESTAMP,
                FormatStrings.LOCAL_IP_ADDRESS);

        List<FormatString> fs = f.getFormatStrings();
        assertEquals(5, fs.size());

        assertEquals(FormatStrings.LOCAL_SERVER_NAME, fs.get(0));
        assertEquals(FormatStrings.OPENING_BRACKET, fs.get(1));
        assertEquals(FormatStrings.TIMESTAMP, fs.get(2));
        assertEquals(FormatStrings.CLOSING_BRACKET, fs.get(3));
        assertEquals(FormatStrings.LOCAL_IP_ADDRESS, fs.get(4));
    }

    @Test
    public void constructor_BracketsAroundTimestampAreImplied_ParsingSpecificationFromString() throws Exception {

        HttpdLogFormat f = new HttpdLogFormat("%t");

        List<FormatString> fs = f.getFormatStrings();
        assertEquals(3, fs.size());

        assertEquals(FormatStrings.OPENING_BRACKET, fs.get(0));
        assertEquals(FormatStrings.TIMESTAMP, fs.get(1));
        assertEquals(FormatStrings.CLOSING_BRACKET, fs.get(2));
    }


    // getFormatStrings() ----------------------------------------------------------------------------------------------

    @Test
    public void getFormatStrings_ReturnsTheUnderlyingStorage() throws Exception {

        FormatString fs = new MockFormatString("A");
        FormatString fs2 = new MockFormatString("b");

        HttpdLogFormat logFormat = new HttpdLogFormat(fs, fs2);

        List<FormatString> fes = logFormat.getFormatStrings();

        assertEquals(2, fes.size());
        assertEquals(fs, fes.get(0));
        assertEquals(fs2, fes.get(1));

        // test mutability
        fes.set(0, null);

        List<FormatString> fes2 = logFormat.getFormatStrings();

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
