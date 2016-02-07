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

import io.novaordis.esa.core.LineFormatTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class CsvFormatTest extends LineFormatTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(CsvFormatTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor_InvalidFormat() throws Exception {

        try {

            new CsvFormat("something without commas");
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

        CsvFormat csvFormat = new CsvFormat(",");
        List<String> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        assertEquals("", fields.get(0));
    }

    @Test
    public void constructor2() throws Exception {

        CsvFormat csvFormat = new CsvFormat("   ,");
        List<String> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        assertEquals("", fields.get(0));
    }

    @Test
    public void constructor3() throws Exception {

        CsvFormat csvFormat = new CsvFormat("  \t \t  ,");
        List<String> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        assertEquals("", fields.get(0));
    }

    @Test
    public void constructor4() throws Exception {

        CsvFormat csvFormat = new CsvFormat("field1, field2, field3,");
        List<String> fields = csvFormat.getFields();
        assertEquals(3, fields.size());
        assertEquals("field1", fields.get(0));
        assertEquals("field2", fields.get(1));
        assertEquals("field3", fields.get(2));
    }

    @Test
    public void constructor5() throws Exception {

        CsvFormat csvFormat = new CsvFormat("field1,  ");
        List<String> fields = csvFormat.getFields();
        assertEquals(1, fields.size());
        assertEquals("field1", fields.get(0));
    }

    @Test
    public void constructor6() throws Exception {

        CsvFormat csvFormat = new CsvFormat("a, b, c");
        List<String> fields = csvFormat.getFields();
        assertEquals(3, fields.size());
        assertEquals("a", fields.get(0));
        assertEquals("b", fields.get(1));
        assertEquals("c", fields.get(2));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected CsvFormat getLineFormatToTest(String formatSpecification) throws Exception {
        return new CsvFormat(formatSpecification);
    }

    @Override
    protected String getFormatSpecificationForLineFormatToTest() throws Exception {

        return "field1, field2, field3,";
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
