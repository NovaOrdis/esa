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

import io.novaordis.esa.ParsingException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLogParserTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------
    @Test
    public void emptyEnclosure_Brackets() throws Exception {

        String commonPattern = "[]";

        HttpdLogParser factory = new HttpdLogParser(
                new HttpdLogFormat(FormatStrings.OPENING_BRACKET, FormatStrings.CLOSING_BRACKET));

        HttpdLogLine le = factory.parse(commonPattern);
        assertNotNull(le);
        assertNull(le.getTimestamp());
    }

    @Test
    public void emptyEnclosure_DoubleQuotes() throws Exception {

        String commonPattern = "\"  \"";

        HttpdLogParser factory = new HttpdLogParser(
                new HttpdLogFormat(FormatStrings.DOUBLE_QUOTES, FormatStrings.DOUBLE_QUOTES));

        HttpdLogLine le = factory.parse(commonPattern);
        assertNotNull(le);
        assertNull(le.getTimestamp());
    }

    @Test
    public void common1() throws Exception {

        String commonPattern = "127.0.0.1 - bob [10/Oct/2016:13:55:36 -0700] \"GET /test.gif HTTP/1.1\" 200 1024";

        HttpdLogParser factory = new HttpdLogParser(HttpdLogFormat.COMMON);

        HttpdLogLine le = factory.parse(commonPattern);
        assertEquals("127.0.0.1", le.getRemoteHost());
        assertNull(le.getRemoteLogname());
        assertEquals("bob", le.getRemoteUser());
        assertEquals(TestDate.create("10/10/16 13:55:36 -0700").getTime(), le.getTimestamp().longValue());
        assertEquals("GET /test.gif HTTP/1.1", le.getFirstRequestLine());
        assertEquals(200, le.getStatusCode().intValue());
        assertEquals(1024, le.getResponseEntityBodySize().longValue());
    }

    @Test
    public void common2() throws Exception {

        String line = "172.20.2.41 - - [09/Jan/2016:20:06:07 -0800] \"OPTIONS * HTTP/1.0\" 200 -";

        HttpdLogParser factory = new HttpdLogParser(HttpdLogFormat.COMMON);

        HttpdLogLine le = factory.parse(line);
        assertEquals("172.20.2.41", le.getRemoteHost());
        assertNull(le.getRemoteLogname());
        assertNull(le.getRemoteUser());
        assertEquals(TestDate.create("01/09/16 20:06:07 -0800").getTime(), le.getTimestamp().longValue());
        assertEquals("OPTIONS * HTTP/1.0", le.getFirstRequestLine());
        assertEquals(200, le.getStatusCode().intValue());
        assertNull(le.getResponseEntityBodySize());
    }

    @Test
    public void custom() throws Exception {

        String commonPattern = "127.0.0.1 bob [10/Oct/2016:13:55:36 -0700] \"GET /test.gif HTTP/1.1\" 200 2326";

        HttpdLogFormat format = new HttpdLogFormat(
                FormatStrings.REMOTE_HOST,
                FormatStrings.REMOTE_USER,
                FormatStrings.OPENING_BRACKET,
                FormatStrings.TIMESTAMP,
                FormatStrings.CLOSING_BRACKET,
                FormatStrings.DOUBLE_QUOTES,
                FormatStrings.FIRST_REQUEST_LINE,
                FormatStrings.DOUBLE_QUOTES,
                FormatStrings.STATUS_CODE,
                FormatStrings.RESPONSE_ENTITY_BODY_SIZE);

        HttpdLogParser factory = new HttpdLogParser(format);

        HttpdLogLine le = factory.parse(commonPattern);
        assertEquals("127.0.0.1", le.getRemoteHost());
        assertNull(le.getRemoteLogname());
        assertEquals("bob", le.getRemoteUser());
        assertEquals(TestDate.create("10/10/16 13:55:36 -0700").getTime(), le.getTimestamp().longValue());
        assertEquals("GET /test.gif HTTP/1.1", le.getFirstRequestLine());
        assertEquals(200, le.getStatusCode().intValue());
        assertNull(le.getOriginalRequestStatusCode());
        assertEquals(2326, le.getResponseEntityBodySize().longValue());
    }

    @Test
    public void performance() throws Exception {

        String line = "\"default task-1\" 127.0.0.1 - [21/Jan/2016:09:32:56 -0800] \"GET /something HTTP/1.1\" \"a=b&c=d\" 404 74 27";

        HttpdLogParser factory = new HttpdLogParser(HttpdLogFormat.PERFORMANCE_ANALYSIS);

        HttpdLogLine le = factory.parse(line);
        assertEquals("default task-1", le.getThreadName());
        assertEquals("127.0.0.1", le.getRemoteHost());
        assertNull(le.getRemoteUser());
        assertEquals(TestDate.create("01/21/16 09:32:56 -0800").getTime(), le.getTimestamp().longValue());
        assertEquals("GET /something HTTP/1.1", le.getFirstRequestLine());
        assertEquals("a=b&c=d", le.getQueryString());
        assertEquals(404, le.getOriginalRequestStatusCode().intValue());
        assertNull(le.getStatusCode());
        assertEquals(74, le.getResponseEntityBodySize().longValue());
        assertEquals(27, le.getRequestProcessingTimeMs().longValue());
    }

    /**
     * The current implementation parses the following format wrongly, because it has (yet) no way to detect
     * that the thread name has spaces. In the future I might add more heuristics.
     * @throws Exception
     */
    @Test
    public void threadNameHasNoSeparators() throws Exception {

        String line = "default task-1 127.0.0.1";

        HttpdLogFormat format = new HttpdLogFormat(FormatStrings.THREAD_NAME, FormatStrings.REMOTE_HOST);

        HttpdLogParser factory = new HttpdLogParser(format);

        HttpdLogLine le = factory.parse(line);
        assertEquals("default", le.getThreadName());
        assertEquals("task-1", le.getRemoteHost());
    }

    @Test
    public void timestampHasNoSeparators() throws Exception {

        String line = "127.0.0.1 - - 20/Jan/2016:03:42:11 -0800 \"GET /something HTTP/1.1\" 1024";

        HttpdLogFormat format = HttpdLogFormat.COMMON;
        HttpdLogParser factory = new HttpdLogParser(format);

        try {
            factory.parse(line);
            fail("should have thrown exception");
        }
        catch(ParsingException e) {
            log.info(e.getMessage());
            assertEquals("expecting OPENING_BRACKET [ but got '2'", e.getMessage());
        }
    }

//    @Test
//    public void logLinesLongerThanThePatternAreOK() throws Exception {
//
//        String line = "127.0.0.1 bob 200 2326";
//
//        LogFormat format = new LogFormat(FormatStrings.REMOTE_HOST);
//        HttpdLogParser factory = new HttpdLogParser(format);
//
//        HttpdLogLine le = factory.parse(line);
//
//        assertEquals(1, le.getPropertyCount());
//        assertEquals("127.0.0.1", le.getRemoteHost());
//        assertNull(le.timestamp);
//        assertNull(le.getRemoteUser());
//    }

    @Test
    public void logLineThatUsesSingleQuotes() throws Exception {

        String line = "'GET /test.gif HTTP/1.1'";

        HttpdLogParser factory = new HttpdLogParser(new HttpdLogFormat(
                FormatStrings.SINGLE_QUOTE,
                FormatStrings.FIRST_REQUEST_LINE,
                FormatStrings.SINGLE_QUOTE));

        HttpdLogLine le = factory.parse(line);
        assertEquals("GET /test.gif HTTP/1.1", le.getFirstRequestLine());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
