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

import java.util.Date;
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

    // parseLine() -----------------------------------------------------------------------------------------------------

    @Test
    public void emptyEnclosure_Brackets() throws Exception {

        String line = "[]";

        HttpdLineParser parser = new HttpdLineParser("[]");

        HttpEvent e = (HttpEvent)parser.parseLine(1L, line);
        assertNotNull(e);
        assertNull(e.getTimestamp());
    }

    @Test
    public void emptyEnclosure_DoubleQuotes() throws Exception {

        String line = "\"  \"";

        HttpdLineParser parser = new HttpdLineParser("\"\"");

        HttpEvent e = (HttpEvent)parser.parseLine(7L, line);
        assertNotNull(e);
        assertNull(e.getTimestamp());
        assertEquals(7L, e.getLongProperty(Event.LINE_NUMBER_PROPERTY_NAME).getLong().longValue());
    }

    @Test
    public void common1() throws Exception {

        String line = "127.0.0.1 - bob [10/Oct/2016:13:55:36 -0700] \"GET /test.gif HTTP/1.1\" 200 1024";

        HttpdLineParser parser = new HttpdLineParser(HttpdLogFormat.COMMON);

        HttpEvent e = (HttpEvent)parser.parseLine(7L, line);
        assertEquals("127.0.0.1", e.getRemoteHost());
        assertNull(e.getRemoteLogname());
        assertEquals("bob", e.getRemoteUser());
        assertEquals(TestDate.create("10/10/16 13:55:36 -0700").getTime(), e.getTimestamp().longValue());
        assertEquals("GET /test.gif HTTP/1.1", e.getFirstRequestLine());
        assertEquals(200, e.getStatusCode().intValue());
        assertEquals(1024, e.getResponseEntityBodySize().longValue());
        assertEquals(7L, e.getLongProperty(Event.LINE_NUMBER_PROPERTY_NAME).getLong().longValue());
    }

    @Test
    public void common2() throws Exception {

        String line = "172.20.2.41 - - [09/Jan/2016:20:06:07 -0800] \"OPTIONS * HTTP/1.0\" 200 -";

        HttpdLineParser parser = new HttpdLineParser(HttpdLogFormat.COMMON);

        HttpEvent e = (HttpEvent)parser.parseLine(7L, line);
        assertEquals("172.20.2.41", e.getRemoteHost());
        assertNull(e.getRemoteLogname());
        assertNull(e.getRemoteUser());
        assertEquals(TestDate.create("01/09/16 20:06:07 -0800").getTime(), e.getTimestamp().longValue());
        assertEquals("OPTIONS * HTTP/1.0", e.getFirstRequestLine());
        assertEquals(200, e.getStatusCode().intValue());
        assertNull(e.getResponseEntityBodySize());
        assertEquals(7L, e.getLongProperty(Event.LINE_NUMBER_PROPERTY_NAME).getLong().longValue());

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

        HttpEvent e = (HttpEvent)parser.parseLine(1L, line);
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

        HttpEvent e = (HttpEvent)parser.parseLine(1L, line);

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

        HttpEvent e = (HttpEvent)parser.parseLine(1L, line);

        assertEquals("default", e.getThreadName());
        assertEquals("task-1", e.getRemoteHost());
    }

    @Test
    public void timestampHasNoSeparators() throws Exception {

        String line = "127.0.0.1 - - 20/Jan/2016:03:42:11 -0800 \"GET /something HTTP/1.1\" 1024";

        HttpdLineParser parser = new HttpdLineParser(HttpdLogFormat.COMMON);

        try {
            parser.parseLine(1L, line);
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

        HttpEvent e = (HttpEvent)parser.parseLine(1L, line);

        assertEquals(2, e.getProperties().size());
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

        HttpEvent e = (HttpEvent)parser.parseLine(1L, line);
        assertEquals("GET /test.gif HTTP/1.1", e.getFirstRequestLine());
    }

    //
    // timestamp with and without brackets
    //

    @Test
    public void timestamp_FormatStringHasExplicitBrackets() throws Exception {

        HttpdLineParser parser = new HttpdLineParser(
                FormatStrings.OPENING_BRACKET,
                FormatStrings.TIMESTAMP,
                FormatStrings.CLOSING_BRACKET);

        String line = "[20/Jun/2016:00:00:00 -0400]";

        HttpEvent e = (HttpEvent)parser.parseLine(1L, line);

        Date expected = FormatStrings.TIMESTAMP_FORMAT.parse(line.substring(1, line.length() - 1));

        Long timestamp = e.getTimestamp();

        assertEquals(expected.getTime(), timestamp.longValue());
    }

    @Test
    public void timestamp_FormatStringHasNoExplicitBrackets() throws Exception {

        HttpdLineParser parser = new HttpdLineParser(FormatStrings.TIMESTAMP);

        String line = "[20/Jun/2016:00:00:00 -0400]";

        HttpEvent e = (HttpEvent)parser.parseLine(1L, line);

        Date expected = FormatStrings.TIMESTAMP_FORMAT.parse(line.substring(1, line.length() - 1));

        Long timestamp = e.getTimestamp();

        assertEquals(expected.getTime(), timestamp.longValue());
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

    // Production ------------------------------------------------------------------------------------------------------

    @Test
    public void production_CorruptedHttpFormat() throws Exception {

        String format =
                "&quot;%I&quot; %h %u [%t] &quot;%r&quot; &quot;%q&quot; %s %b %D %{i,Business-Scenario-Start-Marker} " +
                        "%{i,Business-Scenario-Stop-Marker} %{i,Business-Scenario-Request-Sequence-ID} " +
                        "%{i,Business-Scenario-Iteration-ID} %{c,JSESSIONID}\"/>";

        try {
            new HttpdLineParser(format);
            fail("should throw exception");
        }
        catch(CorruptedHttpdFormatStringException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("/>"));
            log.info(msg);
        }
    }

    // nextToken() -----------------------------------------------------------------------------------------------------

    @Test
    public void nextToken() throws Exception {

        String line = "blah blah.com blah blah";
        int cursor = 5;
        FormatString crt = FormatStrings.LOCAL_SERVER_NAME;

        HttpdLineParser.Token token = HttpdLineParser.nextToken(line, cursor, crt, null, null);

        assertEquals("blah.com", token.getValue());
        assertEquals(14, token.getCursor());
    }

    @Test
    public void nextToken_FirstRequestLine_NoQuotes() throws Exception {

        String line = "blah GET /account/login?something=something_else&other_thing=true HTTP/1.1 blah";
        int cursor = 5;
        FormatString crt = FormatStrings.FIRST_REQUEST_LINE;

        HttpdLineParser.Token token = HttpdLineParser.nextToken(line, cursor, crt, null, null);

        assertEquals("GET /account/login?something=something_else&other_thing=true HTTP/1.1", token.getValue());
        assertEquals(75, token.getCursor());
    }

    @Test
    public void nextToken_FirstRequestLine_Quotes() throws Exception {

        String line = "blah \"GET /account/login?something=something_else&other_thing=true HTTP/1.1\" blah";
        int cursor = 6;
        FormatString crt = FormatStrings.FIRST_REQUEST_LINE;
        FormatString expectedRightEnclosure = FormatStrings.DOUBLE_QUOTES;

        HttpdLineParser.Token token = HttpdLineParser.nextToken(line, cursor, crt, expectedRightEnclosure, null);

        assertEquals("GET /account/login?something=something_else&other_thing=true HTTP/1.1", token.getValue());
        assertEquals(75, token.getCursor());
    }

    @Test
    public void nextToken_UserAgent_NoQuotes() throws Exception {

        String line = "blah Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; .NET CLR 1.1.4322) blah";
        int cursor = 5;
        FormatString userAgent = FormatString.fromString("%{User-Agent}i").get(0);

        HttpdLineParser.Token token = HttpdLineParser.nextToken(line, cursor, userAgent, null, null);

        assertEquals("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; .NET CLR 1.1.4322)", token.getValue());
        assertEquals(75, token.getCursor());
    }

    @Test
    public void nextToken_UserAgent_Quotes() throws Exception {

        String line = "blah \"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; .NET CLR 1.1.4322)\" blah";
        int cursor = 6;
        FormatString expectedRightEnclosure = FormatStrings.DOUBLE_QUOTES;
        FormatString userAgent = FormatString.fromString("%{User-Agent}i").get(0);

        HttpdLineParser.Token token = HttpdLineParser.nextToken(line, cursor, userAgent, expectedRightEnclosure, null);

        assertEquals("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; .NET CLR 1.1.4322)", token.getValue());
        assertEquals(75, token.getCursor());
    }

    @Test
    public void nextToken_Cookie_NoQuotes() throws Exception {

        String line = "blah cookie1=value1.something; cookie2=value2; cookie3=value3 blah";
        int cursor = 5;
        FormatString cookie = FormatString.fromString("%{Cookie}i").get(0);

        HttpdLineParser.Token token = HttpdLineParser.nextToken(line, cursor, cookie, null, null);

        assertEquals("cookie1=value1.something; cookie2=value2; cookie3=value3", token.getValue());
        assertEquals(62, token.getCursor());
    }

    @Test
    public void nextToken_Cookie_Quotes() throws Exception {

        String line = "blah \"cookie1=value1=something; cookie2=value2; cookie3=value3\" blah";
        int cursor = 6;
        FormatString expectedRightEnclosure = FormatStrings.DOUBLE_QUOTES;
        FormatString cookie = FormatString.fromString("%{Cookie}i").get(0);

        HttpdLineParser.Token token = HttpdLineParser.nextToken(line, cursor, cookie, expectedRightEnclosure, null);

        assertEquals("cookie1=value1=something; cookie2=value2; cookie3=value3", token.getValue());
        assertEquals(62, token.getCursor());
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
