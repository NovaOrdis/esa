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

package io.novaordis.esa.csv;

import io.novaordis.esa.core.LineParser;
import io.novaordis.esa.core.LineParserTest;
import io.novaordis.esa.core.event.GenericEvent;
import io.novaordis.esa.core.event.Property;
import io.novaordis.esa.core.event.StringProperty;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class CsvLineParserTest extends LineParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(CsvLineParser.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // buildHeader() ---------------------------------------------------------------------------------------------------

    @Test
    public void buildHeader() throws Exception {

        CsvFormat format = new CsvFormat("a, b, c");
        List<Field> header = CsvLineParser.buildHeaders(format);
        assertEquals(3, header.size());

        Field h = header.get(0);
        assertEquals("a", h.getName());
        assertTrue(String.class.equals(h.getType()));

        Field h2 = header.get(1);
        assertEquals("b", h2.getName());
        assertTrue(String.class.equals(h2.getType()));

        Field h3 = header.get(2);
        assertEquals("c", h3.getName());
        assertTrue(String.class.equals(h3.getType()));
    }

    // constructors ----------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        CsvLineParser p = new CsvLineParser("a, ");

        CsvFormat format = (CsvFormat)p.getLineFormat();

        List<Field> fields = format.getFields();
        assertEquals(1, fields.size());
        assertEquals("a", fields.get(0).getName());
    }

    @Test
    public void constructor2() throws Exception {

        CsvLineParser p = new CsvLineParser("a, something, something-else,");

        CsvFormat format = (CsvFormat)p.getLineFormat();

        List<Field> fields = format.getFields();
        assertEquals(3, fields.size());
        assertEquals("a", fields.get(0).getName());
        assertEquals("something", fields.get(1).getName());
        assertEquals("something-else", fields.get(2).getName());
    }

    @Test
    public void constructor3_EmptyField() throws Exception {

        CsvLineParser p = new CsvLineParser(",");

        CsvFormat format = (CsvFormat)p.getLineFormat();

        List<Field> fields = format.getFields();
        assertEquals(1, fields.size());
        assertEquals("CSVField01", fields.get(0).getName());
    }

    @Test
    public void constructor3_EmptyFields() throws Exception {

        CsvLineParser p = new CsvLineParser(", ,");

        CsvFormat format = (CsvFormat)p.getLineFormat();

        List<Field> fields = format.getFields();
        assertEquals(2, fields.size());
        assertEquals("CSVField01", fields.get(0).getName());
        assertEquals("CSVField02", fields.get(1).getName());
    }

    @Test
    public void constructor4() throws Exception {

        CsvLineParser p = new CsvLineParser("a, b, c");
        CsvFormat format = (CsvFormat)p.getLineFormat();

        List<Field> fields = format.getFields();
        assertEquals(3, fields.size());
        assertEquals("a", fields.get(0).getName());
        assertEquals("b", fields.get(1).getName());
        assertEquals("c", fields.get(2).getName());
    }

    @Test
    public void constructor_InvalidFormat() throws Exception {

        try {
            new CsvLineParser("a");
            fail("should have thrown IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse() throws Exception {

        CsvLineParser parser = new CsvLineParser("a, b, c");

        GenericEvent event = (GenericEvent)parser.parseLine("A, B, C");
        assertNotNull(event);

        List<Property> properties = event.getPropertyList();

        assertEquals(3, properties.size());

        StringProperty p = (StringProperty)properties.get(0);
        assertEquals("a", p.getName());
        assertEquals("A", p.getValue());

        StringProperty p2 = (StringProperty)properties.get(1);
        assertEquals("b", p2.getName());
        assertEquals("B", p2.getValue());

        StringProperty p3 = (StringProperty)properties.get(2);
        assertEquals("c", p3.getName());
        assertEquals("C", p3.getValue());
    }

    @Test
    public void parse_LineLongerThanFormat() throws Exception {

        CsvLineParser parser = new CsvLineParser("a, b, c");

        GenericEvent event = (GenericEvent)parser.parseLine("A, B, C, D");
        assertNotNull(event);

        List<Property> properties = event.getPropertyList();

        assertEquals(3, properties.size());

        StringProperty p = (StringProperty)properties.get(0);
        assertEquals("a", p.getName());
        assertEquals("A", p.getValue());

        StringProperty p2 = (StringProperty)properties.get(1);
        assertEquals("b", p2.getName());
        assertEquals("B", p2.getValue());

        StringProperty p3 = (StringProperty)properties.get(2);
        assertEquals("c", p3.getName());
        assertEquals("C", p3.getValue());
    }

    @Test
    public void parse_LineShorterThanFormat() throws Exception {

        CsvLineParser parser = new CsvLineParser("a, b, c");

        GenericEvent event = (GenericEvent)parser.parseLine("A, B");
        assertNotNull(event);

        List<Property> properties = event.getPropertyList();

        assertEquals(2, properties.size());

        StringProperty p = (StringProperty)properties.get(0);
        assertEquals("a", p.getName());
        assertEquals("A", p.getValue());

        StringProperty p2 = (StringProperty)properties.get(1);
        assertEquals("b", p2.getName());
        assertEquals("B", p2.getValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected LineParser getLineParserToTest(String format) throws Exception {

        return new CsvLineParser(format);
    }

    @Override
    protected String getValidFormatForLineParserToTest() throws Exception {

        return "field-name,";
    }

    @Override
    protected String getValidLineForLineParserToTest() throws Exception {
        return "field-value,";
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
