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

import io.novaordis.esa.LogEventFactoryTest;
import io.novaordis.esa.ParsingException;
import io.novaordis.esa.TestDate;
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
public class HttpdLogEventFactoryTest extends LogEventFactoryTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLogEventFactoryTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------
    @Test
    public void emptyEnclosure_Brackets() throws Exception {

        String commonPattern = "[]";

        HttpdLogEventFactory factory = new HttpdLogEventFactory(
                new HttpdLogFormat(HttpdFormatElement.OPENING_BRACKET, HttpdFormatElement.CLOSING_BRACKET));

        HttpdLogEvent le = factory.parse(commonPattern);
        assertNotNull(le);
        assertNull(le.getTimestamp());
    }

    @Test
    public void emptyEnclosure_DoubleQuotes() throws Exception {

        String commonPattern = "\"  \"";

        HttpdLogEventFactory factory = new HttpdLogEventFactory(
                new HttpdLogFormat(HttpdFormatElement.DOUBLE_QUOTES, HttpdFormatElement.DOUBLE_QUOTES));

        HttpdLogEvent le = factory.parse(commonPattern);
        assertNotNull(le);
        assertNull(le.getTimestamp());
    }

    @Test
    public void common1() throws Exception {

        String commonPattern = "127.0.0.1 - bob [10/Oct/2016:13:55:36 -0700] \"GET /test.gif HTTP/1.1\" 200 1024";

        HttpdLogEventFactory factory = new HttpdLogEventFactory(HttpdLogFormat.COMMON);

        HttpdLogEvent le = factory.parse(commonPattern);
        assertEquals("127.0.0.1", le.getRemoteHost());
        assertNull(le.getRemoteLogname());
        assertEquals("bob", le.getRemoteUser());
        assertEquals(TestDate.create("10/10/16 13:55:36 -0700"), le.getTimestamp());
        assertEquals("GET /test.gif HTTP/1.1", le.getRequestLine());
        assertEquals(200, le.getStatusCode().intValue());
        assertEquals(1024, le.getResponseEntityBodySize().longValue());
    }

    @Test
    public void common2() throws Exception {

        String line = "172.20.2.41 - - [09/Jan/2016:20:06:07 -0800] \"OPTIONS * HTTP/1.0\" 200 -";

        HttpdLogEventFactory factory = new HttpdLogEventFactory(HttpdLogFormat.COMMON);

        HttpdLogEvent le = factory.parse(line);
        assertEquals("172.20.2.41", le.getRemoteHost());
        assertNull(le.getRemoteLogname());
        assertNull(le.getRemoteUser());
        assertEquals(TestDate.create("01/09/16 20:06:07 -0800"), le.getTimestamp());
        assertEquals("OPTIONS * HTTP/1.0", le.getRequestLine());
        assertEquals(200, le.getStatusCode().intValue());
        assertNull(le.getResponseEntityBodySize());
    }

    @Test
    public void custom() throws Exception {

        String commonPattern = "127.0.0.1 bob [10/Oct/2016:13:55:36 -0700] \"GET /test.gif HTTP/1.1\" 200 2326";

        HttpdLogFormat format = new HttpdLogFormat(
                HttpdFormatElement.REMOTE_HOST,
                HttpdFormatElement.REMOTE_USER,
                HttpdFormatElement.OPENING_BRACKET,
                HttpdFormatElement.TIMESTAMP,
                HttpdFormatElement.CLOSING_BRACKET,
                HttpdFormatElement.DOUBLE_QUOTES,
                HttpdFormatElement.FIRST_REQUEST_LINE,
                HttpdFormatElement.DOUBLE_QUOTES,
                HttpdFormatElement.STATUS_CODE,
                HttpdFormatElement.RESPONSE_ENTITY_BODY_SIZE);

        HttpdLogEventFactory factory = new HttpdLogEventFactory(format);

        HttpdLogEvent le = factory.parse(commonPattern);
        assertEquals("127.0.0.1", le.getRemoteHost());
        assertNull(le.getRemoteLogname());
        assertEquals("bob", le.getRemoteUser());
        assertEquals(TestDate.create("10/10/16 13:55:36 -0700"), le.getTimestamp());
        assertEquals("GET /test.gif HTTP/1.1", le.getRequestLine());
        assertEquals(200, le.getStatusCode().intValue());
        assertNull(le.getOriginalRequestStatusCode());
        assertEquals(2326, le.getResponseEntityBodySize().longValue());
    }

    @Test
    public void performance() throws Exception {

        String line = "\"default task-1\" 127.0.0.1 - [21/Jan/2016:09:32:56 -0800] \"GET /something HTTP/1.1\" 404 74 27";

        HttpdLogEventFactory factory = new HttpdLogEventFactory(HttpdLogFormat.PERFORMANCE_ANALYSIS);

        HttpdLogEvent le = factory.parse(line);
        assertEquals("default task-1", le.getThreadName());
        assertEquals("127.0.0.1", le.getRemoteHost());
        assertNull(le.getRemoteUser());
        assertEquals(TestDate.create("01/21/16 09:32:56 -0800"), le.getTimestamp());
        assertEquals("GET /something HTTP/1.1", le.getRequestLine());
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

        HttpdLogFormat format = new HttpdLogFormat(HttpdFormatElement.THREAD_NAME, HttpdFormatElement.REMOTE_HOST);

        HttpdLogEventFactory factory = new HttpdLogEventFactory(format);

        HttpdLogEvent le = factory.parse(line);
        assertEquals("default", le.getThreadName());
        assertEquals("task-1", le.getRemoteHost());
    }

    @Test
    public void timestampHasNoSeparators() throws Exception {

        String line = "127.0.0.1 - - 20/Jan/2016:03:42:11 -0800 \"GET /something HTTP/1.1\" 1024";

        HttpdLogFormat format = HttpdLogFormat.COMMON;
        HttpdLogEventFactory factory = new HttpdLogEventFactory(format);

        try {
            factory.parse(line);
            fail("should have thrown exception");
        }
        catch(ParsingException e) {
            log.info(e.getMessage());
            assertEquals("expecting OPENING_BRACKET [ but got '2'", e.getMessage());
        }
    }

    @Test
    public void logLinesLongerThanThePatternAreOK() throws Exception {

        String line = "127.0.0.1 bob 200 2326";

        HttpdLogFormat format = new HttpdLogFormat(HttpdFormatElement.REMOTE_HOST);
        HttpdLogEventFactory factory = new HttpdLogEventFactory(format);

        HttpdLogEvent le = factory.parse(line);

        assertEquals(1, le.getPropertyCount());
        assertEquals("127.0.0.1", le.getRemoteHost());
        assertNull(le.getTimestamp());
        assertNull(le.getRemoteUser());
    }

    @Test
    public void logLineThatUsesSingleQuotes() throws Exception {

        String line = "'GET /test.gif HTTP/1.1'";

        HttpdLogEventFactory factory = new HttpdLogEventFactory(new HttpdLogFormat(
                HttpdFormatElement.SINGLE_QUOTE,
                HttpdFormatElement.FIRST_REQUEST_LINE,
                HttpdFormatElement.SINGLE_QUOTE));

        HttpdLogEvent le = factory.parse(line);
        assertEquals("GET /test.gif HTTP/1.1", le.getRequestLine());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * @see LogEventFactoryTest#getLogEventFactoryToTest()
     */
    @Override
    protected HttpdLogEventFactory getLogEventFactoryToTest()  {

        throw new RuntimeException("getLogFormatToTest() NOT YET IMPLEMENTED");
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
