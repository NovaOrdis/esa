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
import io.novaordis.events.api.event.DoubleProperty;
import io.novaordis.events.api.event.LongProperty;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.events.api.measure.TimeMeasureUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public abstract class HttpdFormatStringTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdFormatStringTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void fromString() throws Exception {

        List<HttpdFormatString> formats = HttpdFormatString.fromString("%h %l %u [ %t ] \"%D\" %>s %s \" %D \"");

        assertEquals(14, formats.size());
    }

    @Test
    public void fromString_COMMON() throws Exception {

        List<HttpdFormatString> formats = HttpdFormatString.fromString("%h %l %u [ %t ] \"%r\" %>s %b ");

        assertEquals(11, formats.size());

        assertEquals(HttpdFormatStrings.REMOTE_HOST, formats.get(0));
        assertEquals(HttpdFormatStrings.REMOTE_LOGNAME, formats.get(1));
        assertEquals(HttpdFormatStrings.REMOTE_USER, formats.get(2));
        assertEquals(HttpdFormatStrings.OPENING_BRACKET, formats.get(3));
        assertEquals(HttpdFormatStrings.TIMESTAMP, formats.get(4));
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, formats.get(5));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, formats.get(6));
        assertEquals(HttpdFormatStrings.FIRST_REQUEST_LINE, formats.get(7));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, formats.get(8));
        assertEquals(HttpdFormatStrings.STATUS_CODE, formats.get(9));
        assertEquals(HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE, formats.get(10));
    }

    @Test
    public void fromString_PERFORMANCE_ANALYSIS() throws Exception {

        List<HttpdFormatString> formats = HttpdFormatString.fromString("\"%I\" %h %u [%t] \"%r\" %s %b %D");

        assertEquals(14, formats.size());

        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, formats.get(0));
        assertEquals(HttpdFormatStrings.THREAD_NAME, formats.get(1));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, formats.get(2));
        assertEquals(HttpdFormatStrings.REMOTE_HOST, formats.get(3));
        assertEquals(HttpdFormatStrings.REMOTE_USER, formats.get(4));
        assertEquals(HttpdFormatStrings.OPENING_BRACKET, formats.get(5));
        assertEquals(HttpdFormatStrings.TIMESTAMP, formats.get(6));
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, formats.get(7));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, formats.get(8));
        assertEquals(HttpdFormatStrings.FIRST_REQUEST_LINE, formats.get(9));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, formats.get(10));
        assertEquals(HttpdFormatStrings.ORIGINAL_REQUEST_STATUS_CODE, formats.get(11));
        assertEquals(HttpdFormatStrings.RESPONSE_ENTITY_BODY_SIZE, formats.get(12));
        assertEquals(HttpdFormatStrings.REQUEST_PROCESSING_TIME_MS, formats.get(13));
    }

    @Test
    public void fromString_QuotedParameterizedFormatString() throws Exception {

        List<HttpdFormatString> httpdFormatStrings = HttpdFormatString.fromString("\"%{c,Some-Cookie}\"");
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(0));
        assertEquals(HttpdFormatStrings.DOUBLE_QUOTES, httpdFormatStrings.get(2));

        CookieHttpdFormatString cfs = (CookieHttpdFormatString) httpdFormatStrings.get(1);
        assertEquals("Some-Cookie", cfs.getCookieName());
    }

    @Test
    public void fromString_PartiallyCorrectFormatString() throws Exception {

        try {

            HttpdFormatString.fromString("%h %l %u/>");
            fail("should throw exception");
        }
        catch(CorruptedHttpdFormatStringException e) {

            String msg = e.getMessage();
            assertTrue(msg.matches("invalid httpd log format token \"/>\""));
            log.info(msg);
        }
    }

    @Test
    public void fromString_InvalidTokenIsTheFirstToken() throws Exception {

        try {

            HttpdFormatString.fromString("blah");
            fail("should throw exception");
        }
        catch(ParsingException e) {

            String msg = e.getMessage();
            assertTrue(msg.matches("unknown httpd format element 'blah'"));
            log.info(msg);
        }
    }

    @Test
    public void fromString_A() throws Exception {

        List<HttpdFormatString> formats = HttpdFormatString.fromString("%A");

        assertEquals(1, formats.size());
        HttpdFormatString fs = formats.get(0);

        assertEquals("%A", fs.getLiteral());
        assertEquals("10.72.42.58", fs.parse("10.72.42.58", null, null));
        assertEquals(String.class, fs.getType());
        assertFalse(fs.isLeftEnclosure());
        assertFalse(fs.isRightEnclosure());
        assertNull(fs.getMatchingEnclosure());
        StringProperty p = (StringProperty)fs.toProperty("blah");
        assertEquals(HttpEvent.LOCAL_IP_ADDRESS, p.getName());
        assertEquals("blah", p.getValue());
    }

    @Test
    public void fromString_v() throws Exception {

        List<HttpdFormatString> formats = HttpdFormatString.fromString("%v");

        assertEquals(1, formats.size());
        HttpdFormatString fs = formats.get(0);

        assertEquals("%v", fs.getLiteral());
        assertEquals("some.local.server.name", fs.parse("some.local.server.name", null, null));
        assertEquals(String.class, fs.getType());
        assertFalse(fs.isLeftEnclosure());
        assertFalse(fs.isRightEnclosure());
        assertNull(fs.getMatchingEnclosure());
        StringProperty p = (StringProperty)fs.toProperty("blah");
        assertEquals(HttpEvent.LOCAL_SERVER_NAME, p.getName());
        assertEquals("blah", p.getValue());
    }

    @Test
    public void fromString_S() throws Exception {

        List<HttpdFormatString> formats = HttpdFormatString.fromString("%S");

        assertEquals(1, formats.size());
        HttpdFormatString fs = formats.get(0);

        assertEquals("%S", fs.getLiteral());
        assertEquals(123L, fs.parse("123", null, null));
        assertEquals(Long.class, fs.getType());
        assertFalse(fs.isLeftEnclosure());
        assertFalse(fs.isRightEnclosure());
        assertNull(fs.getMatchingEnclosure());
        LongProperty p = (LongProperty)fs.toProperty(123L);
        assertEquals(HttpEvent.BYTES_TRANSFERRED, p.getName());
        assertEquals(123L, p.getValue());
    }

    @Test
    public void fromString_T() throws Exception {

        List<HttpdFormatString> formats = HttpdFormatString.fromString("%T");

        assertEquals(1, formats.size());
        HttpdFormatString fs = formats.get(0);

        assertEquals("%T", fs.getLiteral());
        assertEquals(0.001d, ((Double)fs.parse("0.001", null, null)).doubleValue(), 0.0001);
        assertEquals(Double.class, fs.getType());
        assertFalse(fs.isLeftEnclosure());
        assertFalse(fs.isRightEnclosure());
        assertNull(fs.getMatchingEnclosure());
        DoubleProperty p = (DoubleProperty)fs.toProperty(0.01);
        assertEquals(HttpEvent.REQUEST_DURATION, p.getName());
        assertEquals(0.01d, ((Double)p.getValue()).doubleValue(), 0.0001);
        assertEquals(TimeMeasureUnit.SECOND, p.getMeasureUnit());
    }

    @Test
    public void fromString_Ignore() throws Exception {

        List<HttpdFormatString> formats = HttpdFormatString.fromString("%?");

        assertEquals(1, formats.size());
        HttpdFormatString fs = formats.get(0);

        assertEquals("%?", fs.getLiteral());
        assertNull(fs.parse("something", null, null));
        assertEquals(String.class, fs.getType());
        assertFalse(fs.isLeftEnclosure());
        assertFalse(fs.isRightEnclosure());
        assertNull(fs.getMatchingEnclosure());
        assertNull(fs.toProperty("something"));
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse_Dash() throws Exception {

        HttpdFormatString fs = getFormatStringToTest();

        assertNull(fs.parse("-", null, null));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract HttpdFormatString getFormatStringToTest();

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
