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

import io.novaordis.events.core.event.MapProperty;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public abstract class ParameterizedFormatStringTest extends FormatStringTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ParameterizedFormatStringTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void parameterizedFormatFromString_StringContainsSpaces() throws Exception {

        try {
            ParameterizedHttpdFormatString.parameterizedFormatFromString("this string contains spaces");
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void parameterizedFormatFromString_RequestHeader() throws Exception {

        ParameterizedHttpdFormatString pfs = ParameterizedHttpdFormatString.parameterizedFormatFromString("%{i,Test-Header}");
        assertNotNull(pfs);
        RequestHeaderHttpdFormatString i = (RequestHeaderHttpdFormatString)pfs;
        assertEquals("Test-Header", i.getHeaderName());
    }

    //
    // Alternative format: %{Referer}i
    //

    @Test
    public void parameterizedFormatFromString_RequestHeader_AlternativeFormat() throws Exception {

        ParameterizedHttpdFormatString pfs = ParameterizedHttpdFormatString.parameterizedFormatFromString("%{Test-Header}i");
        assertNotNull(pfs);
        RequestHeaderHttpdFormatString i = (RequestHeaderHttpdFormatString)pfs;
        assertEquals("Test-Header", i.getHeaderName());
    }

    @Test
    public void parameterizedFormatFromString_ResponseHeader() throws Exception {

        ParameterizedHttpdFormatString pfs = ParameterizedHttpdFormatString.parameterizedFormatFromString("%{o,Test-Header}");
        assertNotNull(pfs);
        ResponseHeaderHttpdFormatString o = (ResponseHeaderHttpdFormatString)pfs;
        assertEquals("Test-Header", o.getHeaderName());
    }

    @Test
    public void parameterizedFormatFromString_ResponseHeader_AlternativeFormat() throws Exception {

        ParameterizedHttpdFormatString pfs = ParameterizedHttpdFormatString.parameterizedFormatFromString("%{Test-Header}o");
        assertNotNull(pfs);
        ResponseHeaderHttpdFormatString o = (ResponseHeaderHttpdFormatString)pfs;
        assertEquals("Test-Header", o.getHeaderName());
    }

    @Test
    public void parameterizedFormatFromString_Cookie() throws Exception {

        ParameterizedHttpdFormatString pfs = ParameterizedHttpdFormatString.parameterizedFormatFromString("%{c,something}");
        assertNotNull(pfs);
        CookieHttpdFormatString o = (CookieHttpdFormatString)pfs;
        assertEquals("something", o.getCookieName());
    }

    @Test
    public void parameterizedFormatFromString_Cookie_AlternativeFormat() throws Exception {

        ParameterizedHttpdFormatString pfs = ParameterizedHttpdFormatString.parameterizedFormatFromString("%{something}c");
        assertNotNull(pfs);
        CookieHttpdFormatString o = (CookieHttpdFormatString)pfs;
        assertEquals("something", o.getCookieName());
    }

    @Test
    public void parameterizedFormatFromString_ParameterizedFormatStringFollowedByQuote() throws Exception {

        ParameterizedHttpdFormatString pfs = ParameterizedHttpdFormatString.parameterizedFormatFromString("%{c,something}\"");
        assertNotNull(pfs);
        CookieHttpdFormatString o = (CookieHttpdFormatString)pfs;
        assertEquals("something", o.getCookieName());
    }

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_LiteralDoesNotStartWithPrefix() throws Exception {

        try {
            getFormatStringToTest("this-is-surely-invalid");
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException iae) {
            String msg = iae.getMessage();
            log.info(msg);
            assertTrue(msg.contains("cannot be parsed into a parameterized format string"));
        }
    }

    @Test
    public void constructor_LiteralDoesNotEndWithPrefix() throws Exception {

        ParameterizedHttpdFormatString civilian = getFormatStringToTest();

        String doesNotEndWithBrace = ((ParameterizedHttpdFormatStringBase)civilian).getPrefix() + "something";

        try {
            getFormatStringToTest(doesNotEndWithBrace);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException iae) {
            String msg = iae.getMessage();
            log.info(msg);
            assertTrue(msg.contains("does not end with '}'"));
        }
    }

    // getType() -------------------------------------------------------------------------------------------------------

    @Test
    public void getType() throws Exception {

        ParameterizedHttpdFormatString pfs = getFormatStringToTest();
        assertEquals(String.class, pfs.getType());
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse() throws Exception {

        ParameterizedHttpdFormatString pfs = getFormatStringToTest();

        String s = (String)pfs.parse("something", null, null);
        assertEquals("something", s);
    }

    // toProperty() ----------------------------------------------------------------------------------------------------

    @Test
    public void toProperty() throws Exception {

        ParameterizedHttpdFormatString pfs = getFormatStringToTest();

        MapProperty mapProperty = (MapProperty)pfs.toProperty("something");

        assertEquals(((ParameterizedHttpdFormatStringBase)pfs).getHttpEventMapName(), mapProperty.getName());
        Map<String, Object> map = mapProperty.getMap();
        assertEquals(1, map.size());
        assertEquals("something", map.get(pfs.getParameter()));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected ParameterizedHttpdFormatString getFormatStringToTest() {
        return getFormatStringToTest(null);
    }

    protected abstract ParameterizedHttpdFormatString getFormatStringToTest(String literal);

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
