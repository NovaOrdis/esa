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

package io.novaordis.esa.logs.httpd;

import io.novaordis.esa.logs.ParsingException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

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

        FormatStrings e = FormatStrings.DOUBLE_QUOTES;
        assertTrue(e.isLeftEnclosure());
        assertTrue(e.isRightEnclosure());
        assertEquals(FormatStrings.DOUBLE_QUOTES, e.getMatchingEnclosure());
    }

    @Test
    public void singleQuote() throws Exception {

        FormatStrings e = FormatStrings.SINGLE_QUOTE;
        assertTrue(e.isLeftEnclosure());
        assertTrue(e.isRightEnclosure());
        assertEquals(FormatStrings.SINGLE_QUOTE, e.getMatchingEnclosure());
    }

    @Test
    public void openingBracket() throws Exception {

        FormatStrings e = FormatStrings.OPENING_BRACKET;
        assertTrue(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertEquals(FormatStrings.CLOSING_BRACKET, e.getMatchingEnclosure());
    }

    @Test
    public void closingBracket() throws Exception {

        FormatStrings e = FormatStrings.CLOSING_BRACKET;
        assertFalse(e.isLeftEnclosure());
        assertTrue(e.isRightEnclosure());
        assertEquals(FormatStrings.OPENING_BRACKET, e.getMatchingEnclosure());
    }

    @Test
    public void remoteHost() throws Exception {

        FormatStrings e = FormatStrings.REMOTE_HOST;
        assertEquals("%h", e.getLiteral());
        assertEquals("127.0.0.1", e.parse("127.0.0.1"));
        assertNull(e.parse("-"));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void remoteLogname() throws Exception {

        FormatStrings e = FormatStrings.REMOTE_LOGNAME;
        assertEquals("%l", e.getLiteral());
        assertEquals("blah", e.parse("blah"));
        assertNull(e.parse("-"));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void remoteUser() throws Exception {

        FormatStrings e = FormatStrings.REMOTE_USER;
        assertEquals("%u", e.getLiteral());
        assertEquals("blah", e.parse("blah"));
        assertNull(e.parse("-"));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void timestamp() throws Exception {

        FormatStrings e = FormatStrings.TIMESTAMP;
        assertEquals("%t", e.getLiteral());
        Date d = (Date)e.parse("18/Sep/2016:19:18:28 -0400");
        assertEquals(TestDate.create("09/18/16 19:18:28 -0400"), d);
        assertNull(e.parse("-"));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void timestamp_InvalidStringRepresentationFormat() throws Exception {

        FormatStrings e = FormatStrings.TIMESTAMP;
        try {
            e.parse("something that is not a date");
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            log.info(pe.getMessage());
            assertTrue(pe.getCause() instanceof ParseException);
        }
    }

    @Test
    public void originalRequestStatusCode() throws Exception {

        FormatStrings e = FormatStrings.ORIGINAL_REQUEST_STATUS_CODE;
        assertEquals("%s", e.getLiteral());
        Integer i = (Integer)e.parse("200");
        assertNotNull(i);
        assertEquals(200, i.intValue());
        assertNull(e.parse("-"));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void statusCode() throws Exception {

        FormatStrings e = FormatStrings.STATUS_CODE;
        assertEquals("%>s", e.getLiteral());
        Integer i = (Integer)e.parse("400");
        assertNotNull(i);
        assertEquals(400, i.intValue());
        assertNull(e.parse("-"));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void responseEntityBodySide() throws Exception {

        FormatStrings e = FormatStrings.RESPONSE_ENTITY_BODY_SIZE;
        assertEquals("%b", e.getLiteral());
        Long l = (Long)e.parse("12345");
        assertNotNull(l);
        assertEquals(12345L, l.longValue());
        assertNull(e.parse("-"));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void threadName() throws Exception {

        FormatStrings e = FormatStrings.THREAD_NAME;
        assertEquals("%I", e.getLiteral());
        String s = (String)e.parse("some thread name");
        assertEquals("some thread name", s);
        assertNull(e.parse("-"));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    @Test
    public void requestProcessingTimeMs() throws Exception {

        FormatStrings e = FormatStrings.REQUEST_PROCESSING_TIME_MS;
        assertEquals("%D", e.getLiteral());
        Long l = (Long)e.parse("12345");
        assertNotNull(l);
        assertEquals(12345L, l.longValue());
        assertNull(e.parse("-"));

        assertFalse(e.isLeftEnclosure());
        assertFalse(e.isRightEnclosure());
        assertNull(e.getMatchingEnclosure());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected FormatStrings getFormatElementToTest()  {

        return FormatStrings.REMOTE_HOST;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
