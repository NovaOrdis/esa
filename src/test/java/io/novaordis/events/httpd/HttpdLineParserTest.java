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

import io.novaordis.events.ParsingException;
import io.novaordis.events.core.LineParserTest;
import io.novaordis.events.core.LineStreamParser;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.FaultEvent;
import io.novaordis.events.core.event.MockEvent;
import io.novaordis.events.core.event.LineEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class HttpdLineParserTest extends LineParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLineParserTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // constructors ----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_InvalidFormat() throws Exception {

        try {
            new HttpdLineParser("g4t872yt824t");
            fail("should throw IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void constructor() throws Exception {

        HttpdLineParser p = new HttpdLineParser("[%t] %h \"%{c,Some-Cookie}\"");

        HttpdLogFormat f = p.getHttpdLogFormat();

        List<FormatString> formatStrings = f.getFormatStrings();
        assertEquals(7, formatStrings.size());
        assertEquals(FormatStrings.OPENING_BRACKET, formatStrings.get(0));
        assertEquals(FormatStrings.TIMESTAMP, formatStrings.get(1));
        assertEquals(FormatStrings.CLOSING_BRACKET, formatStrings.get(2));
        assertEquals(FormatStrings.REMOTE_HOST, formatStrings.get(3));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(4));
        assertEquals("Some-Cookie", ((CookieFormatString)formatStrings.get(5)).getCookieName());
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(6));
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void emptyEnclosure_Brackets() throws Exception {

        String line = "[]";

        HttpdLineParser parser = new HttpdLineParser("[]");

        HttpEvent e = (HttpEvent)parser.parseLine(line);
        assertNotNull(e);
        assertNull(e.getTimestamp());
    }

    @Test
    public void emptyEnclosure_DoubleQuotes() throws Exception {

        String line = "\"  \"";

        HttpdLineParser parser = new HttpdLineParser("\"\"");

        HttpEvent e = (HttpEvent)parser.parseLine(line);
        assertNotNull(e);
        assertNull(e.getTimestamp());
    }

    @Test
    public void common1() throws Exception {

        String line = "127.0.0.1 - bob [10/Oct/2016:13:55:36 -0700] \"GET /test.gif HTTP/1.1\" 200 1024";

        HttpdLineParser parser = new HttpdLineParser(HttpdLogFormat.COMMON);

        HttpEvent e = (HttpEvent)parser.parseLine(line);
        assertEquals("127.0.0.1", e.getRemoteHost());
        assertNull(e.getRemoteLogname());
        assertEquals("bob", e.getRemoteUser());
        assertEquals(TestDate.create("10/10/16 13:55:36 -0700").getTime(), e.getTimestamp().longValue());
        assertEquals("GET /test.gif HTTP/1.1", e.getFirstRequestLine());
        assertEquals(200, e.getStatusCode().intValue());
        assertEquals(1024, e.getResponseEntityBodySize().longValue());
    }

    @Test
    public void common2() throws Exception {

        String line = "172.20.2.41 - - [09/Jan/2016:20:06:07 -0800] \"OPTIONS * HTTP/1.0\" 200 -";

        HttpdLineParser parser = new HttpdLineParser(HttpdLogFormat.COMMON);

        HttpEvent e = (HttpEvent)parser.parseLine(line);
        assertEquals("172.20.2.41", e.getRemoteHost());
        assertNull(e.getRemoteLogname());
        assertNull(e.getRemoteUser());
        assertEquals(TestDate.create("01/09/16 20:06:07 -0800").getTime(), e.getTimestamp().longValue());
        assertEquals("OPTIONS * HTTP/1.0", e.getFirstRequestLine());
        assertEquals(200, e.getStatusCode().intValue());
        assertNull(e.getResponseEntityBodySize());
    }

    @Test
    public void custom() throws Exception {

        String line = "127.0.0.1 bob [10/Oct/2016:13:55:36 -0700] \"GET /test.gif HTTP/1.1\" 200 2326";

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

        HttpdLineParser parser = new HttpdLineParser(format);

        HttpEvent e = (HttpEvent)parser.parseLine(line);
        assertEquals("127.0.0.1", e.getRemoteHost());
        assertNull(e.getRemoteLogname());
        assertEquals("bob", e.getRemoteUser());
        assertEquals(TestDate.create("10/10/16 13:55:36 -0700").getTime(), e.getTimestamp().longValue());
        assertEquals("GET /test.gif HTTP/1.1", e.getFirstRequestLine());
        assertEquals(200, e.getStatusCode().intValue());
        assertNull(e.getOriginalRequestStatusCode());
        assertEquals(2326, e.getResponseEntityBodySize().longValue());
    }

    @Test
    public void performance() throws Exception {

        String line = "\"default task-1\" 127.0.0.1 - [21/Jan/2016:09:32:56 -0800] \"GET /something HTTP/1.1\" \"a=b&c=d\" 404 74 27";

        HttpdLineParser parser = new HttpdLineParser(HttpdLogFormat.PERFORMANCE_ANALYSIS);

        HttpEvent e = (HttpEvent)parser.parseLine(line);

        assertEquals("default task-1", e.getThreadName());
        assertEquals("127.0.0.1", e.getRemoteHost());
        assertNull(e.getRemoteUser());
        assertEquals(TestDate.create("01/21/16 09:32:56 -0800").getTime(), e.getTimestamp().longValue());
        assertEquals("GET /something HTTP/1.1", e.getFirstRequestLine());
        assertEquals("a=b&c=d", e.getQueryString());
        assertEquals(404, e.getOriginalRequestStatusCode().intValue());
        assertNull(e.getStatusCode());
        assertEquals(74, e.getResponseEntityBodySize().longValue());
        assertEquals(27, e.getRequestDuration().longValue());
    }

    /**
     * The current implementation parses the following format wrongly, because it has (yet) no way to detect
     * that the thread name has spaces. In the future we might add more heuristics.
     *
     * @throws Exception
     */
    @Test
    public void threadNameHasNoSeparators() throws Exception {

        String line = "default task-1 127.0.0.1";

        HttpdLineParser parser = new HttpdLineParser(FormatStrings.THREAD_NAME, FormatStrings.REMOTE_HOST);

        HttpEvent e = (HttpEvent)parser.parseLine(line);

        assertEquals("default", e.getThreadName());
        assertEquals("task-1", e.getRemoteHost());
    }

    @Test
    public void timestampHasNoSeparators() throws Exception {

        String line = "127.0.0.1 - - 20/Jan/2016:03:42:11 -0800 \"GET /something HTTP/1.1\" 1024";

        HttpdLineParser parser = new HttpdLineParser(HttpdLogFormat.COMMON);

        try {
            parser.parseLine(line);
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

        HttpdLineParser parser = new HttpdLineParser(FormatStrings.REMOTE_HOST);

        HttpEvent e = (HttpEvent)parser.parseLine(line);

        assertEquals(1, e.getProperties().size());
        assertEquals("127.0.0.1", e.getRemoteHost());
        assertNull(e.getTimestamp());
        assertNull(e.getRemoteUser());
    }

    @Test
    public void logLineThatUsesSingleQuotes() throws Exception {

        String line = "'GET /test.gif HTTP/1.1'";

        HttpdLineParser parser = new HttpdLineParser(
                FormatStrings.SINGLE_QUOTE,
                FormatStrings.FIRST_REQUEST_LINE,
                FormatStrings.SINGLE_QUOTE);

        HttpEvent e = (HttpEvent)parser.parseLine(line);
        assertEquals("GET /test.gif HTTP/1.1", e.getFirstRequestLine());
    }

    // testing together with the owner LineStreamParser ----------------------------------------------------------------

    @Test
    public void notAStringEvent() throws Exception {

        LineStreamParser p = new LineStreamParser(new HttpdLineParser(HttpdLogFormat.COMMON));

        assertTrue(p.process(new MockEvent()));

        List<Event> outputEvents = p.getEvents();

        assertEquals(1, outputEvents.size());

        FaultEvent e = (FaultEvent)outputEvents.get(0);

        log.info(e.getMessage());
    }

    //    return new LineEvent("test-host test-remote-logname test-remote-user [31/Jan/2016:06:59:53 -0800] \"GET /test HTTP/1.1\" 200 1024");

    @Test
    public void notAHttpdLogLine() throws Exception {

        LineStreamParser p = new LineStreamParser(new HttpdLineParser(HttpdLogFormat.COMMON));

        String line = "definitely not a httpd log line";

        assertTrue(p.process(new LineEvent(1L, line)));

        List<Event> outputEvents = p.getEvents();

        assertEquals(1, outputEvents.size());

        FaultEvent e = (FaultEvent)outputEvents.get(0);

        log.info(e.toString());
    }

    @Test
    public void format() throws Exception {

        HttpdLineParser p = getLineParserToTest(HttpdLogFormat.COMMON.toString());

        assertEquals(HttpdLogFormat.COMMON.toString(), p.getHttpdLogFormat().toString());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected HttpdLineParser getLineParserToTest(String format) throws Exception {

        return new HttpdLineParser(format);
    }

    @Override
    protected String getValidFormatForLineParserToTest() throws Exception {

        return FormatStrings.REMOTE_HOST.getLiteral();
    }

    @Override
    protected String getValidLineForLineParserToTest() throws Exception {
        return "127.0.0.1";
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
