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
import io.novaordis.events.core.event.DoubleProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.MeasureUnit;
import io.novaordis.events.core.event.StringProperty;
import io.novaordis.events.core.event.TimeMeasureUnit;
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
public abstract class FormatStringTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(FormatStringTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void fromString() throws Exception {

        List<FormatString> formats = FormatString.fromString("%h %l %u [ %t ] \"%D\" %>s %s \" %D \"");

        assertEquals(14, formats.size());
    }

    @Test
    public void fromString_COMMON() throws Exception {

        List<FormatString> formats = FormatString.fromString("%h %l %u [ %t ] \"%r\" %>s %b ");

        assertEquals(11, formats.size());

        assertEquals(FormatStrings.REMOTE_HOST, formats.get(0));
        assertEquals(FormatStrings.REMOTE_LOGNAME, formats.get(1));
        assertEquals(FormatStrings.REMOTE_USER, formats.get(2));
        assertEquals(FormatStrings.OPENING_BRACKET, formats.get(3));
        assertEquals(FormatStrings.TIMESTAMP, formats.get(4));
        assertEquals(FormatStrings.CLOSING_BRACKET, formats.get(5));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formats.get(6));
        assertEquals(FormatStrings.FIRST_REQUEST_LINE, formats.get(7));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formats.get(8));
        assertEquals(FormatStrings.STATUS_CODE, formats.get(9));
        assertEquals(FormatStrings.RESPONSE_ENTITY_BODY_SIZE, formats.get(10));
    }

    @Test
    public void fromString_PERFORMANCE_ANALYSIS() throws Exception {

        List<FormatString> formats = FormatString.fromString("\"%I\" %h %u [%t] \"%r\" %s %b %D");

        assertEquals(14, formats.size());

        assertEquals(FormatStrings.DOUBLE_QUOTES, formats.get(0));
        assertEquals(FormatStrings.THREAD_NAME, formats.get(1));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formats.get(2));
        assertEquals(FormatStrings.REMOTE_HOST, formats.get(3));
        assertEquals(FormatStrings.REMOTE_USER, formats.get(4));
        assertEquals(FormatStrings.OPENING_BRACKET, formats.get(5));
        assertEquals(FormatStrings.TIMESTAMP, formats.get(6));
        assertEquals(FormatStrings.CLOSING_BRACKET, formats.get(7));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formats.get(8));
        assertEquals(FormatStrings.FIRST_REQUEST_LINE, formats.get(9));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formats.get(10));
        assertEquals(FormatStrings.ORIGINAL_REQUEST_STATUS_CODE, formats.get(11));
        assertEquals(FormatStrings.RESPONSE_ENTITY_BODY_SIZE, formats.get(12));
        assertEquals(FormatStrings.REQUEST_PROCESSING_TIME_MS, formats.get(13));
    }

    @Test
    public void fromString_QuotedParameterizedFormatString() throws Exception {

        List<FormatString> formatStrings = FormatString.fromString("\"%{c,Some-Cookie}\"");
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(0));
        assertEquals(FormatStrings.DOUBLE_QUOTES, formatStrings.get(2));

        CookieFormatString cfs = (CookieFormatString)formatStrings.get(1);
        assertEquals("Some-Cookie", cfs.getCookieName());
    }

    @Test
    public void fromString_PartiallyCorrectFormatString() throws Exception {

        try {

            FormatString.fromString("%h %l %u/>");
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

            FormatString.fromString("blah");
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

        List<FormatString> formats = FormatString.fromString("%A");

        assertEquals(1, formats.size());
        FormatString fs = formats.get(0);

        assertEquals("%A", fs.getLiteral());
        assertEquals("10.72.42.58", fs.parse("10.72.42.58"));
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

        List<FormatString> formats = FormatString.fromString("%v");

        assertEquals(1, formats.size());
        FormatString fs = formats.get(0);

        assertEquals("%v", fs.getLiteral());
        assertEquals("some.local.server.name", fs.parse("some.local.server.name"));
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

        List<FormatString> formats = FormatString.fromString("%S");

        assertEquals(1, formats.size());
        FormatString fs = formats.get(0);

        assertEquals("%S", fs.getLiteral());
        assertEquals(123L, fs.parse("123"));
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

        List<FormatString> formats = FormatString.fromString("%T");

        assertEquals(1, formats.size());
        FormatString fs = formats.get(0);

        assertEquals("%T", fs.getLiteral());
        assertEquals(0.001d, ((Double)fs.parse("0.001")).doubleValue(), 0.0001);
        assertEquals(Double.class, fs.getType());
        assertFalse(fs.isLeftEnclosure());
        assertFalse(fs.isRightEnclosure());
        assertNull(fs.getMatchingEnclosure());
        DoubleProperty p = (DoubleProperty)fs.toProperty(0.01);
        assertEquals(HttpEvent.REQUEST_DURATION, p.getName());
        assertEquals(0.01d, ((Double)p.getValue()).doubleValue(), 0.0001);
        assertEquals(TimeMeasureUnit.SECOND, p.getMeasureUnit());
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse_Dash() throws Exception {

        FormatString fs = getFormatStringToTest();

        assertNull(fs.parse("-"));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract FormatString getFormatStringToTest();

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
