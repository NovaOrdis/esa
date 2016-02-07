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

package io.novaordis.esa;

import io.novaordis.esa.core.LineFormat;
import io.novaordis.esa.core.LineParser;
import io.novaordis.esa.csv.CsvFormat;
import io.novaordis.esa.csv.CsvLineParser;
import io.novaordis.esa.csv.Field;
import io.novaordis.esa.httpd.FormatString;
import io.novaordis.esa.httpd.FormatStrings;
import io.novaordis.esa.httpd.HttpdLineParser;
import io.novaordis.esa.httpd.HttpdLogFormat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class LineParserFactoryTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(LineParserFactoryTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void getInstance_nullFormat() throws Exception {

        try {
            LineParserFactory.getInstance(null);
            fail("should have thrown IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void getInstance_NoKnownParser() throws Exception {

        assertNull(LineParserFactory.getInstance(")7t97ty283gy289y3"));
    }

    @Test
    public void getInstance_HttpdLineParser() throws Exception {

        LineParser parser = LineParserFactory.getInstance("[%t] %h");
        assertTrue(parser instanceof HttpdLineParser);
        HttpdLineParser httpdLineParser = (HttpdLineParser)parser;
        LineFormat f = httpdLineParser.getLineFormat();
        HttpdLogFormat httpdLogFormat = (HttpdLogFormat)f;
        List<FormatString> formatStrings = httpdLogFormat.getFormatStrings();
        assertEquals(4, formatStrings.size());
        assertEquals(FormatStrings.OPENING_BRACKET, formatStrings.get(0));
        assertEquals(FormatStrings.TIMESTAMP, formatStrings.get(1));
        assertEquals(FormatStrings.CLOSING_BRACKET, formatStrings.get(2));
        assertEquals(FormatStrings.REMOTE_HOST, formatStrings.get(3));
    }

    @Test
    public void getInstance_CsvLineParser() throws Exception {

        LineParser parser = LineParserFactory.getInstance("something,");
        assertTrue(parser instanceof CsvLineParser);
        CsvLineParser csvLineParser = (CsvLineParser)parser;
        LineFormat f = csvLineParser.getLineFormat();
        CsvFormat csvFormat = (CsvFormat)f;
        List<Field> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        Field field = fields.get(0);
        assertEquals("something", field.getName());
        assertEquals(String.class, field.getType());
        assertNull(field.getValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
