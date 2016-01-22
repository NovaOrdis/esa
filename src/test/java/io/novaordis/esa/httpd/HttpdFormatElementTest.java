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

import io.novaordis.esa.FormatElementTest;
import io.novaordis.esa.ParsingException;
import io.novaordis.esa.TestDate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdFormatElementTest extends FormatElementTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdFormatElementTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void remoteHost() throws Exception {

        HttpdFormatElement e = HttpdFormatElement.REMOTE_HOST;
        assertEquals("%h", e.getLiteral());
        assertEquals("127.0.0.1", e.parse("127.0.0.1"));
        assertNull(e.parse("-"));
    }

    @Test
    public void remoteLogname() throws Exception {

        HttpdFormatElement e = HttpdFormatElement.REMOTE_LOGNAME;
        assertEquals("%l", e.getLiteral());
        assertEquals("blah", e.parse("blah"));
        assertNull(e.parse("-"));
    }

    @Test
    public void remoteUser() throws Exception {

        HttpdFormatElement e = HttpdFormatElement.REMOTE_USER;
        assertEquals("%u", e.getLiteral());
        assertEquals("blah", e.parse("blah"));
        assertNull(e.parse("-"));
    }

    @Test
    public void timestamp() throws Exception {

        HttpdFormatElement e = HttpdFormatElement.TIMESTAMP;
        assertEquals("%t", e.getLiteral());
        Date d = (Date)e.parse("18/Sep/2016:19:18:28 -0400");
        assertEquals(TestDate.create("09/18/16 19:18:28 -0400"), d);
        assertNull(e.parse("-"));
    }

    @Test
    public void timestamp_InvalidStringRepresentationFormat() throws Exception {

        HttpdFormatElement e = HttpdFormatElement.TIMESTAMP;
        try {
            e.parse("something that is not a date");
            fail("should have thrown exception");
        }
        catch(ParsingException pe) {

            log.info(pe.getMessage());
            assertTrue(pe.getCause() instanceof ParseException);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected HttpdFormatElement getFormatElementToTest()  {

        return HttpdFormatElement.REMOTE_HOST;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
