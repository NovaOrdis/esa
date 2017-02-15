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

import io.novaordis.events.api.parser.ParsingException;
import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.api.event.LongProperty;
import io.novaordis.events.api.event.MapProperty;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.events.api.measure.MeasureUnit;
import io.novaordis.events.api.measure.MemoryMeasureUnit;
import io.novaordis.events.api.measure.TimeMeasureUnit;
import io.novaordis.utilities.time.Timestamp;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdFormatStringsTest extends HttpdFormatStringTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdFormatStringsTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void doubleQuotes() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.DOUBLE_QUOTES;
        assertTrue(e.isLeftEnclosure());
        assertTrue(e.isRightEnclosure());
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, e.getMatchingEnclosure());
        assertNull(e.toProperty("does not matter"));
    }

    @Test
    public void singleQuote() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.SINGLE_QUOTE;
        assertTrue(e.isLeftEnclosure());
        assertTrue(e.isRightEnclosure());
        assertEquals(HttpdFormatStrings.SINGLE_QUOTE, e.getMatchingEnclosure());
        assertNull(e.toProperty("does not matter"));
    }

    @Test
    public void openingBracket() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.OPENING_BRACKET;
        assertTrue(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, e.getMatchingEnclosure());
        assertNull(e.toProperty("does not matter"));
    }

    @Test
    public void closingBracket() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.CLOSING_BRACKET;
        assertFalse(e.isLeftEnclosure());
        assertTrue(e.isRightEnclosure());
        assertEquals(HttpdFormatStrings.OPENING_BRACKET, e.getMatchingEnclosure());
        assertNull(e.toProperty("does not matter"));
    }

    @Test
    public void remoteHost() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.REMOTE_HOST;
        assertEquals("%h", e.getLiteral());
        assertEquals("127.0.0.1", e.parse("127.0.0.1", null, null));
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        StringProperty property = (StringProperty)e.toProperty("127.0.0.1");
        assertEquals(HttpEvent.REMOTE_HOST, property.getName());
        assertEquals("127.0.0.1", property.getString());
    }

    @Test
    public void remoteHost_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.REMOTE_HOST;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    @Test
    public void remoteLogname() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.REMOTE_LOGNAME;
        assertEquals("%l", e.getLiteral());
        assertEquals("blah", e.parse("blah", null, null));
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        StringProperty property = (StringProperty)e.toProperty("something");
        assertEquals(HttpEvent.REMOTE_LOGNAME, property.getName());
        assertEquals("something", property.getString());
    }

    @Test
    public void remoteLogname_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.REMOTE_LOGNAME;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    @Test
    public void remoteUser() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.REMOTE_USER;
        assertEquals("%u", e.getLiteral());
        assertEquals("blah", e.parse("blah", null, null));
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        StringProperty property = (StringProperty)e.toProperty("something");
        assertEquals(HttpEvent.REMOTE_USER, property.getName());
        assertEquals("something", property.getString());
    }

    @Test
    public void remoteUser_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.REMOTE_USER;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    // TIMESTAMP -------------------------------------------------------------------------------------------------------

    @Test
    public void timestamp() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.TIMESTAMP;
        assertEquals("%t", e.getLiteral());
        Timestamp t = (Timestamp)e.parse("18/Sep/2016:19:18:28 -0400", null, null);
        assertEquals(TestDate.create("09/18/16 19:18:28 -0400"), new Date(t.getTime()));
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void timestamp_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.TIMESTAMP;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    @Test
    public void timestamp_InvalidStringRepresentationFormat() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.TIMESTAMP;
        try {
            e.parse("something that is not a date", null, null);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            log.info(pe.getMessage());
            assertTrue(pe.getCause() instanceof ParseException);
        }
    }

    // END of TIMESTAMP ------------------------------------------------------------------------------------------------

    @Test
    public void originalRequestStatusCode() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE;
        assertEquals("%s", e.getLiteral());
        Integer i = (Integer)e.parse("200", null, null);
        assertNotNull(i);
        assertEquals(200, i.intValue());
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        IntegerProperty property = (IntegerProperty)e.toProperty(403);
        assertEquals(HttpEvent.ORIGINAL_REQUEST_STATUS_CODE, property.getName());
        assertEquals(403, property.getInteger().intValue());
    }

    @Test
    public void originalRequestStatusCode_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    @Test
    public void statusCode() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.STATUS_CODE;
        assertEquals("%>s", e.getLiteral());
        Integer i = (Integer)e.parse("400", null, null);
        assertNotNull(i);
        assertEquals(400, i.intValue());
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        IntegerProperty property = (IntegerProperty)e.toProperty(402);
        assertEquals(HttpEvent.STATUS_CODE, property.getName());
        assertEquals(402, property.getInteger().intValue());
    }

    @Test
    public void statusCode_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.STATUS_CODE;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    @Test
    public void responseEntityBodySize() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE;
        assertEquals("%b", e.getLiteral());
        Long l = (Long)e.parse("12345", null, null);
        assertNotNull(l);
        assertEquals(12345L, l.longValue());
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        LongProperty property = (LongProperty)e.toProperty(1024L);
        assertEquals(HttpEvent.RESPONSE_ENTITY_BODY_SIZE, property.getName());
        assertEquals(1024L, property.getLong().longValue());
        MeasureUnit m = property.getMeasureUnit();
        assertEquals(MemoryMeasureUnit.BYTE, m);
    }

    @Test
    public void responseEntityBodySIze_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    @Test
    public void threadName() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.THREAD_NAME;
        assertEquals("%I", e.getLiteral());
        String s = (String)e.parse("some thread name", null, null);
        assertEquals("some thread name", s);
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        StringProperty property = (StringProperty)e.toProperty("Thread 001");
        assertEquals(HttpEvent.THREAD_NAME, property.getName());
        assertEquals("Thread 001", property.getString());
    }

    @Test
    public void threadName_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.THREAD_NAME;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    @Test
    public void requestProcessingTimeMs() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.REQUEST_PROCESSING_TIME_MS;
        assertEquals("%D", e.getLiteral());
        Long l = (Long)e.parse("12345", null, null);
        assertNotNull(l);
        assertEquals(12345L, l.longValue());
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        LongProperty property = (LongProperty)e.toProperty(1L);
        assertEquals(HttpEvent.REQUEST_DURATION, property.getName());
        assertEquals(1L, property.getLong().longValue());
        assertEquals(TimeMeasureUnit.MILLISECOND, property.getMeasureUnit());
    }

    @Test
    public void requestProcessingTimeMs_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.REQUEST_PROCESSING_TIME_MS;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    @Test
    public void queryString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.QUERY_STRING;
        assertEquals("%q", e.getLiteral());
        String s = (String)e.parse("attr1=val1&attr2=Val2&attr3=1&attr4=1.1", null, null);
        assertNotNull(s);
        assertEquals("attr1=val1&attr2=Val2&attr3=1&attr4=1.1", s);
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        MapProperty property = (MapProperty)e.toProperty("attr1=val1&attr2=Val2&attr3=1&attr4=1.1");
        assertEquals(HttpEvent.QUERY, property.getName());

        //noinspection unchecked
        Map<String, Object> queryAttributes = property.getMap();

        assertEquals(4, queryAttributes.size());
        assertEquals("val1", queryAttributes.get("attr1"));
        assertEquals("Val2", queryAttributes.get("attr2"));
        assertEquals("1", queryAttributes.get("attr3"));
        assertEquals("1.1", queryAttributes.get("attr4"));
        assertNull(property.getMeasureUnit());
    }

    @Test
    public void queryString_EmptyString() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.QUERY_STRING;

        try {
            e.parse("", 7L, 11);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            String msg = pe.getMessage();
            log.info(msg);
            assertEquals(7L, pe.getLineNumber().longValue());
            assertEquals(11, pe.getPositionInLine().intValue());
            assertTrue(msg.contains("empty string"));
        }
    }

    @Test
    public void pid() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.PID;
        assertEquals("%P", e.getLiteral());
        Integer i = (Integer)e.parse("12000", null, null);
        assertNotNull(i);
        assertEquals(12000, i.intValue());
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());

        IntegerProperty property = (IntegerProperty)e.toProperty(12001);
        assertEquals(HttpEvent.PID, property.getName());
        assertEquals(12001, property.getInteger().intValue());
    }

    @Test
    public void pid_NotAnInteger() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.PID;

        try {
            e.parse("blah", null, null);
            fail("should throw exception");
        }
        catch(ParsingException pe) {
            String msg = pe.getMessage();
            log.info(msg);
        }
    }

    @Test
    public void ignore() throws Exception {

        HttpdFormatString e = HttpdFormatStrings.IGNORE;
        assertEquals("%?", e.getLiteral());
        assertNull(e.parse("-", null, null));
        assertNull(e.parse("something", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected HttpdFormatStrings getFormatStringToTest()  {

        return HttpdFormatStrings.REMOTE_HOST;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
