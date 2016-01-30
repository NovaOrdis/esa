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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/22/16
 */
public abstract class FormatStringTest {

    // Constants -------------------------------------------------------------------------------------------------------

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

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract FormatString getFormatElementToTest();

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
