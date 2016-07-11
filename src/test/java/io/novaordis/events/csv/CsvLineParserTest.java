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

package io.novaordis.events.csv;

import io.novaordis.events.core.LineParser;
import io.novaordis.events.core.LineParserTest;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.GenericEvent;
import io.novaordis.events.core.event.GenericTimedEvent;
import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.StringProperty;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Test
    public void constructor_Headers_NoTimestamp() throws Exception {

        CsvLineParser p = new CsvLineParser("a, b, c");

        List<Field> headers = p.getHeaders();
        assertEquals(3, headers.size());

        Field h = headers.get(0);
        assertEquals("a", h.getName());
        assertTrue(String.class.equals(h.getType()));

        Field h2 = headers.get(1);
        assertEquals("b", h2.getName());
        assertTrue(String.class.equals(h2.getType()));

        Field h3 = headers.get(2);
        assertEquals("c", h3.getName());
        assertTrue(String.class.equals(h3.getType()));

        assertEquals(-1, p.getTimestampFieldIndex());
    }

    @Test
    public void constructor_Headers_Timestamp() throws Exception {

        CsvLineParser p = new CsvLineParser("T(time:yyyy), b, c");

        List<Field> headers = p.getHeaders();
        assertEquals(3, headers.size());

        Field h = headers.get(0);
        assertEquals("T", h.getName());
        assertTrue(Date.class.equals(h.getType()));
        assertTrue(h.getFormat() instanceof SimpleDateFormat);

        Field h2 = headers.get(1);
        assertEquals("b", h2.getName());
        assertTrue(String.class.equals(h2.getType()));

        Field h3 = headers.get(2);
        assertEquals("c", h3.getName());
        assertTrue(String.class.equals(h3.getType()));

        assertEquals(0, p.getTimestampFieldIndex());
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse() throws Exception {

        CsvLineParser parser = new CsvLineParser("a, b, c");

        GenericEvent event = (GenericEvent)parser.parseLine(7L, "A, B, C");
        assertNotNull(event);

        List<Property> properties = event.getPropertyList();

        assertEquals(4, properties.size());

        LongProperty p0 = (LongProperty)properties.get(0);
        assertEquals(Event.LINE_NUMBER_PROPERTY_NAME, p0.getName());
        assertEquals(7L, p0.getValue());

        StringProperty p = (StringProperty)properties.get(1);
        assertEquals("a", p.getName());
        assertEquals("A", p.getValue());

        StringProperty p2 = (StringProperty)properties.get(2);
        assertEquals("b", p2.getName());
        assertEquals("B", p2.getValue());

        StringProperty p3 = (StringProperty)properties.get(3);
        assertEquals("c", p3.getName());
        assertEquals("C", p3.getValue());
    }

    @Test
    public void parse_LineLongerThanFormat() throws Exception {

        CsvLineParser parser = new CsvLineParser("a, b, c");

        GenericEvent event = (GenericEvent)parser.parseLine(7L, "A, B, C, D");
        assertNotNull(event);

        List<Property> properties = event.getPropertyList();

        assertEquals(4, properties.size());

        LongProperty p0 = (LongProperty)properties.get(0);
        assertEquals(Event.LINE_NUMBER_PROPERTY_NAME, p0.getName());
        assertEquals(7L, p0.getValue());

        StringProperty p = (StringProperty)properties.get(1);
        assertEquals("a", p.getName());
        assertEquals("A", p.getValue());

        StringProperty p2 = (StringProperty)properties.get(2);
        assertEquals("b", p2.getName());
        assertEquals("B", p2.getValue());

        StringProperty p3 = (StringProperty)properties.get(3);
        assertEquals("c", p3.getName());
        assertEquals("C", p3.getValue());
    }

    @Test
    public void parse_LineShorterThanFormat() throws Exception {

        CsvLineParser parser = new CsvLineParser("a, b, c");

        GenericEvent event = (GenericEvent)parser.parseLine(1L, "A, B");
        assertNotNull(event);

        List<Property> properties = event.getPropertyList();

        assertEquals(3, properties.size());

        LongProperty p0 = (LongProperty)properties.get(0);
        assertEquals(Event.LINE_NUMBER_PROPERTY_NAME, p0.getName());
        assertEquals(1L, p0.getValue());

        StringProperty p = (StringProperty)properties.get(1);
        assertEquals("a", p.getName());
        assertEquals("A", p.getValue());

        StringProperty p2 = (StringProperty)properties.get(2);
        assertEquals("b", p2.getName());
        assertEquals("B", p2.getValue());
    }

    @Test
    public void parse_UntimedEvent() throws Exception {

        CsvLineParser parser = new CsvLineParser("brand(string), count(int)");

        GenericEvent event = (GenericEvent)parser.parseLine(5L, "Audi, 5");

        assertNotNull(event);

        List<Property> properties = event.getPropertyList();

        assertEquals(3, properties.size());

        LongProperty p0 = (LongProperty)properties.get(0);
        assertEquals(Event.LINE_NUMBER_PROPERTY_NAME, p0.getName());
        assertEquals(5L, p0.getValue());

        StringProperty p = (StringProperty)properties.get(1);
        assertEquals("brand", p.getName());
        assertEquals("Audi", p.getValue());

        IntegerProperty p2 = (IntegerProperty)properties.get(2);
        assertEquals("count", p2.getName());
        assertEquals(5, p2.getValue());
    }

    @Test
    public void parse_TimedEvent_TimestampFirstInLine() throws Exception {

        CsvLineParser parser = new CsvLineParser("T(time:MMM-dd yyyy HH:mm:ss), brand(string), count(int)");

        GenericTimedEvent event = (GenericTimedEvent)parser.parseLine(1L, "Jan-01 2016 12:01:01, BMW, 7");
        assertNotNull(event);

        Long timestamp = event.getTimestampGMT();
        assertEquals(timestamp.longValue(),
                new SimpleDateFormat("yy/MM/dd HH:mm:ss").parse("16/01/01 12:01:01").getTime());

        List<Property> properties = event.getPropertyList();

        assertEquals(3, properties.size());

        LongProperty p0 = (LongProperty)properties.get(0);
        assertEquals(Event.LINE_NUMBER_PROPERTY_NAME, p0.getName());
        assertEquals(1L, p0.getValue());

        StringProperty p = (StringProperty)properties.get(1);
        assertEquals("brand", p.getName());
        assertEquals("BMW", p.getValue());

        IntegerProperty p2 = (IntegerProperty)properties.get(2);
        assertEquals("count", p2.getName());
        assertEquals(7, p2.getValue());
    }

    @Test
    public void parse_TimedEvent_TimestampNotFirstInLine() throws Exception {

        CsvLineParser parser = new CsvLineParser("brand(string), T(time:MMM-dd yyyy HH:mm:ss), count(int)");

        GenericTimedEvent event = (GenericTimedEvent)parser.parseLine(1L, "BMW, Jan-01 2016 12:01:01, 7");
        assertNotNull(event);

        Long timestamp = event.getTimestampGMT();
        assertEquals(timestamp.longValue(),
                new SimpleDateFormat("yy/MM/dd HH:mm:ss").parse("16/01/01 12:01:01").getTime());

        List<Property> properties = event.getPropertyList();

        assertEquals(3, properties.size());

        LongProperty p0 = (LongProperty)properties.get(0);
        assertEquals(Event.LINE_NUMBER_PROPERTY_NAME, p0.getName());
        assertEquals(1L, p0.getValue());

        StringProperty p = (StringProperty)properties.get(1);
        assertEquals("brand", p.getName());
        assertEquals("BMW", p.getValue());

        IntegerProperty p2 = (IntegerProperty)properties.get(2);
        assertEquals("count", p2.getName());
        assertEquals(7, p2.getValue());
    }

    @Test
    public void parse_FieldContainsComma() throws Exception {

        CsvLineParser parser = new CsvLineParser("a, b");

        String line = "something, \"something, else\"";

        GenericEvent e = (GenericEvent)parser.parseLine(77L, line);

        List<Property> props = e.getPropertyList();

        assertEquals(3, props.size());

        LongProperty p = (LongProperty)props.get(0);
        assertEquals(77L, p.getLong().longValue());

        StringProperty p2 = (StringProperty)props.get(1);
        assertEquals("something", p2.getString());

        StringProperty p3 = (StringProperty)props.get(2);
        assertEquals("something, else", p3.getString());
    }

    @Test
    public void parse_QuotedFields() throws Exception {

        CsvLineParser parser = new CsvLineParser("a, b");

        String line = "\"blah\", \"blah blah\"";

        GenericEvent e = (GenericEvent)parser.parseLine(77L, line);

        List<Property> props = e.getPropertyList();

        assertEquals(3, props.size());

        LongProperty p = (LongProperty)props.get(0);
        assertEquals(77L, p.getLong().longValue());

        StringProperty p2 = (StringProperty)props.get(1);
        assertEquals("blah", p2.getString());

        StringProperty p3 = (StringProperty)props.get(2);
        assertEquals("blah blah", p3.getString());
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
