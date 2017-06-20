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

package io.novaordis.events;

import io.novaordis.events.core.LineFormat;
import io.novaordis.events.core.LineParser;
import io.novaordis.events.csv.CSVField;
import io.novaordis.events.csv.CSVFormat;
import io.novaordis.events.httpd.HttpdFormatString;
import io.novaordis.events.httpd.HttpdFormatStrings;
import io.novaordis.events.httpd.HttpdLineParser;
import io.novaordis.events.httpd.HttpdLogFormat;
import io.novaordis.utilities.UserErrorException;
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
        List<HttpdFormatString> httpdFormatStrings = httpdLogFormat.getHttpdFormatStrings();
        assertEquals(4, httpdFormatStrings.size());
        assertEquals(HttpdFormatStrings.OPENING_BRACKET, httpdFormatStrings.get(0));
        assertEquals(HttpdFormatStrings.TIMESTAMP, httpdFormatStrings.get(1));
        assertEquals(HttpdFormatStrings.CLOSING_BRACKET, httpdFormatStrings.get(2));
        assertEquals(HttpdFormatStrings.REMOTE_HOST, httpdFormatStrings.get(3));
    }

    @Test
    public void getInstance_HttpdLineParser_CorruptedHttpdFormat() throws Exception {

        try {
            LineParserFactory.getInstance("[%t] %h/>");
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("/>"));
        }
    }

    @Test
    public void getInstance_CsvLineParser() throws Exception {

        LineParser parser = LineParserFactory.getInstance("something,");
        assertTrue(parser instanceof LineParser);
        LineParser csvLineParser = (LineParser)parser;
        LineFormat f = csvLineParser.getLineFormat();
        CSVFormat csvFormat = (CSVFormat)f;
        List<CSVField> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        CSVField field = fields.get(0);
        assertEquals("something", field.getName());
        assertEquals(String.class, field.getType());
        assertNull(field.getValue());
    }

    @Test
    public void getInstance_CsvLineParser_WeKnowItIsACSVParserButItIsBroken() throws Exception {

        try {

            LineParserFactory.getInstance("something(blah),");
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(e.getMessage());
            assertTrue(msg.contains("invalid"));
            assertTrue(msg.contains("specification"));
            assertTrue(msg.contains("blah"));
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
