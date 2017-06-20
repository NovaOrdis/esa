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

import io.novaordis.events.core.LineFormatTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class CSVFormatTest extends LineFormatTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(CSVFormatTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor_InvalidFormat() throws Exception {

        try {

            new CSVFormat("something without commas");
            fail("should have thrown IllegalArgumentException");

        }
        catch(IllegalArgumentException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("does not contain commas"));
        }
    }

    @Test
    public void constructor1() throws Exception {

        CSVFormat csvFormat = new CSVFormat(",");
        List<CSVField> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        CSVField f = fields.get(0);
        assertNotNull(f);
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
        assertEquals("CSVField01", f.getName());
    }

    @Test
    public void constructor2() throws Exception {

        CSVFormat csvFormat = new CSVFormat("   ,");
        List<CSVField> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        CSVField f = fields.get(0);
        assertNotNull(f);
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
        assertEquals("CSVField01", f.getName());
    }

    @Test
    public void constructor3() throws Exception {

        CSVFormat csvFormat = new CSVFormat("  \t \t  ,");
        List<CSVField> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        CSVField f = fields.get(0);
        assertNotNull(f);
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
        assertEquals("CSVField01", f.getName());
    }

    @Test
    public void constructor4() throws Exception {

        CSVFormat csvFormat = new CSVFormat("field1, field2, field3,");

        List<CSVField> fields = csvFormat.getFields();

        assertEquals(3, fields.size());

        CSVField f = fields.get(0);
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
        assertEquals("field1", f.getName());

        CSVField f2 = fields.get(1);
        assertEquals(String.class, f2.getType());
        assertNull(f2.getValue());
        assertEquals("field2", f2.getName());

        CSVField f3 = fields.get(2);
        assertEquals(String.class, f3.getType());
        assertNull(f3.getValue());
        assertEquals("field3", f3.getName());
    }

    @Test
    public void constructor5() throws Exception {

        CSVFormat csvFormat = new CSVFormat("field1,  ");
        List<CSVField> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        CSVField f = fields.get(0);
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
        assertEquals("field1", f.getName());
    }

    @Test
    public void constructor6() throws Exception {

        CSVFormat csvFormat = new CSVFormat("a, b, c");

        List<CSVField> fields = csvFormat.getFields();

        assertEquals(3, fields.size());

        CSVField f = fields.get(0);
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
        assertEquals("a", f.getName());

        CSVField f2 = fields.get(1);
        assertEquals(String.class, f2.getType());
        assertNull(f2.getValue());
        assertEquals("b", f2.getName());

        CSVField f3 = fields.get(2);
        assertEquals(String.class, f3.getType());
        assertNull(f3.getValue());
        assertEquals("c", f3.getName());
    }

    @Test
    public void constructor_typed() throws Exception {

        CSVFormat csvFormat = new CSVFormat("timestamp(time:yy/MM/dd HH:mm:ss), count(int), duration(long), path");

        List<CSVField> fields = csvFormat.getFields();

        assertEquals(4, fields.size());

        CSVField f = fields.get(0);
        assertEquals(Date.class, f.getType());
        assertNull(f.getValue());
        assertEquals("timestamp", f.getName());

        CSVField f2 = fields.get(1);
        assertEquals(Integer.class, f2.getType());
        assertNull(f2.getValue());
        assertEquals("count", f2.getName());

        CSVField f3 = fields.get(2);
        assertEquals(Long.class, f3.getType());
        assertNull(f3.getValue());
        assertEquals("duration", f3.getName());

        CSVField f4 = fields.get(3);
        assertEquals(String.class, f4.getType());
        assertNull(f4.getValue());
        assertEquals("path", f4.getName());
    }

    @Test
    public void constructor_invalidType() throws Exception {

        try {
            new CSVFormat("duration(ms)");
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected CSVFormat getLineFormatToTest(String formatSpecification) throws Exception {
        return new CSVFormat(formatSpecification);
    }

    @Override
    protected String getFormatSpecificationForLineFormatToTest() throws Exception {

        return "field1, field2, field3,";
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
