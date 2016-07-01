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

import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.MapProperty;
import io.novaordis.events.core.event.MeasureUnit;
import io.novaordis.events.core.event.MemoryMeasureUnit;
import io.novaordis.events.core.event.StringProperty;
import io.novaordis.events.core.event.TimeMeasureUnit;
import io.novaordis.events.ParsingException;
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
public class FormatStringsTest extends FormatStringTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(FormatStringsTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void doubleQuotes() throws Exception {

        FormatString e = FormatStrings.DOUBLE_QUOTES;
        assertTrue(e.isLeftEnclosure());
        assertTrue(e.isRightEnclosure());
        assertEquals(FormatStrings.DOUBLE_QUOTES, e.getMatchingEnclosure());
        assertNull(e.toProperty("does not matter"));
    }

    @Test
    public void singleQuote() throws Exception {

        FormatString e = FormatStrings.SINGLE_QUOTE;
        assertTrue(e.isLeftEnclosure());
        assertTrue(e.isRightEnclosure());
        assertEquals(FormatStrings.SINGLE_QUOTE, e.getMatchingEnclosure());
        assertNull(e.toProperty("does not matter"));
    }

    @Test
    public void openingBracket() throws Exception {

        FormatString e = FormatStrings.OPENING_BRACKET;
        assertTrue(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertEquals(FormatStrings.CLOSING_BRACKET, e.getMatchingEnclosure());
        assertNull(e.toProperty("does not matter"));
    }

    @Test
    public void closingBracket() throws Exception {

        FormatString e = FormatStrings.CLOSING_BRACKET;
        assertFalse(e.isLeftEnclosure());
        assertTrue(e.isRightEnclosure());
        assertEquals(FormatStrings.OPENING_BRACKET, e.getMatchingEnclosure());
        assertNull(e.toProperty("does not matter"));
    }

    @Test
    public void remoteHost() throws Exception {

        FormatString e = FormatStrings.REMOTE_HOST;
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
    public void remoteLogname() throws Exception {

        FormatString e = FormatStrings.REMOTE_LOGNAME;
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
    public void remoteUser() throws Exception {

        FormatString e = FormatStrings.REMOTE_USER;
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
    public void timestamp() throws Exception {

        FormatString e = FormatStrings.TIMESTAMP;
        assertEquals("%t", e.getLiteral());
        Date d = (Date)e.parse("18/Sep/2016:19:18:28 -0400", null, null);
        assertEquals(TestDate.create("09/18/16 19:18:28 -0400"), d);
        assertNull(e.parse("-", null, null));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void timestamp_InvalidStringRepresentationFormat() throws Exception {

        FormatString e = FormatStrings.TIMESTAMP;
        try {
            e.parse("something that is not a date", null, null);
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            log.info(pe.getMessage());
            assertTrue(pe.getCause() instanceof ParseException);
        }
    }

    @Test
    public void originalRequestStatusCode() throws Exception {

        FormatString e = FormatStrings.ORIGINAL_REQUEST_STATUS_CODE;
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
    public void statusCode() throws Exception {

        FormatString e = FormatStrings.STATUS_CODE;
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
    public void responseEntityBodySize() throws Exception {

        FormatString e = FormatStrings.RESPONSE_ENTITY_BODY_SIZE;
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
    public void threadName() throws Exception {

        FormatString e = FormatStrings.THREAD_NAME;
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
    public void requestProcessingTimeMs() throws Exception {

        FormatString e = FormatStrings.REQUEST_PROCESSING_TIME_MS;
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
    public void queryString() throws Exception {

        FormatString e = FormatStrings.QUERY_STRING;
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

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected FormatStrings getFormatStringToTest()  {

        return FormatStrings.REMOTE_HOST;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
