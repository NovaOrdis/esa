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

import io.novaordis.events.api.event.DateProperty;
import io.novaordis.events.api.event.DoubleProperty;
import io.novaordis.events.api.event.FloatProperty;
import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.api.event.LongProperty;
import io.novaordis.events.api.event.StringProperty;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class CSVFieldTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(CSVFieldTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void unbalancedParentheses() throws Exception {

        try {
            new CSVField("a)");
            fail("should throw exception");
        }
        catch(InvalidCSVFieldException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("unbalanced"));
        }
    }

    @Test
    public void stringField() throws Exception {

        CSVField f = new CSVField("some-string", String.class);

        assertEquals("some-string", f.getName());
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void stringField2() throws Exception {

        CSVField f = new CSVField("some-string(string)");

        assertEquals("some-string", f.getName());
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_SimpleString() throws Exception {

        CSVField f = new CSVField("some-string");

        assertEquals("some-string", f.getName());
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_Time() throws Exception {

        CSVField f = new CSVField("timestamp(time:yy/MM/dd HH:mm:ss)");

        assertEquals("timestamp", f.getName());
        assertEquals(Date.class, f.getType());
        assertNull(f.getValue());

        Format format = f.getFormat();
        assertTrue(format instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat)format;

        assertEquals(sdf.parse("16/01/01 01:01:01"),
                new SimpleDateFormat("MM/dd/yy hh:mm:ss a").parse("01/01/16 01:01:01 AM"));
    }

    @Test
    public void fieldSpecificationParsing_Time_InvalidTimeFormatSpecification() throws Exception {

        try {
            new CSVField("timestamp(time:blah)");
        }
        catch(InvalidCSVFieldException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("invalid timestamp format \"blah\""));
            IllegalArgumentException cause = (IllegalArgumentException)e.getCause();
            assertNotNull(cause);
        }
    }

    @Test
    public void fieldSpecificationParsing_Integer() throws Exception {

        CSVField f = new CSVField("a(int)");

        assertEquals("a", f.getName());
        assertEquals(Integer.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_Long() throws Exception {

        CSVField f = new CSVField("a(long)");

        assertEquals("a", f.getName());
        assertEquals(Long.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_Float() throws Exception {

        CSVField f = new CSVField("a(float)");

        assertEquals("a", f.getName());
        assertEquals(Float.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_Double() throws Exception {

        CSVField f = new CSVField("a(double)");

        assertEquals("a", f.getName());
        assertEquals(Double.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_InvalidType() throws Exception {

        try {
            new CSVField("fieldA(ms)");
            fail("should throw exception");
        }
        catch(InvalidCSVFieldException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("invalid field type specification \"ms\""));
        }
    }

    // toProperty() ----------------------------------------------------------------------------------------------------

    @Test
    public void toProperty_String() throws Exception {

        CSVField f = new CSVField("test", String.class);
        StringProperty sp = (StringProperty)f.toProperty("blah");
        assertEquals("test", sp.getName());
        assertEquals("blah", sp.getValue());
    }

    @Test
    public void toProperty_Integer() throws Exception {

        CSVField f = new CSVField("test", Integer.class);
        IntegerProperty ip = (IntegerProperty)f.toProperty("1");
        assertEquals("test", ip.getName());
        assertEquals(1, ip.getInteger().intValue());
    }

    @Test
    public void toProperty_Integer_InvalidValue() throws Exception {

        CSVField f = new CSVField("test", Integer.class);

        try {
            f.toProperty("blah");
            fail("Should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void toProperty_Long() throws Exception {

        CSVField f = new CSVField("test", Long.class);
        LongProperty lp = (LongProperty)f.toProperty("1");
        assertEquals("test", lp.getName());
        assertEquals(1, lp.getLong().longValue());
    }

    @Test
    public void toProperty_Long_InvalidValue() throws Exception {

        CSVField f = new CSVField("test", Long.class);

        try {
            f.toProperty("blah");
            fail("Should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void toProperty_Float() throws Exception {

        CSVField f = new CSVField("test", Float.class);
        FloatProperty fp = (FloatProperty)f.toProperty("1.1");
        assertEquals("test", fp.getName());
        assertEquals(1.1f, fp.getFloat().floatValue(), 0.0001);
    }

    @Test
    public void toProperty_Float_InvalidValue() throws Exception {

        CSVField f = new CSVField("test", Float.class);

        try {
            f.toProperty("blah");
            fail("Should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void toProperty_Double() throws Exception {

        CSVField f = new CSVField("test", Double.class);
        DoubleProperty fp = (DoubleProperty)f.toProperty("1.1");
        assertEquals("test", fp.getName());
        assertEquals(1.1d, fp.getDouble().doubleValue(), 0.0001);
    }

    @Test
    public void toProperty_Double_InvalidValue() throws Exception {

        CSVField f = new CSVField("test", Double.class);

        try {
            f.toProperty("blah");
            fail("Should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void toProperty_Date() throws Exception {

        CSVField f = new CSVField("test", Date.class);
        f.setFormat(new SimpleDateFormat("yyyy"));
        DateProperty dp = (DateProperty)f.toProperty("2016");
        assertEquals("test", dp.getName());
        long time = dp.getDate().getTime();
        long reference = new SimpleDateFormat("yyyy").parse("2016").getTime();
        assertEquals(time, reference);
    }

    @Test
    public void toProperty_Date_InvalidValue() throws Exception {

        CSVField f = new CSVField("test", Date.class);
        f.setFormat(new SimpleDateFormat("yyyy"));

        try {
            f.toProperty("blah");
            fail("Should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    // setValue() ------------------------------------------------------------------------------------------------------

    @Test
    public void setValue_Null() throws Exception {

        CSVField field = new CSVField("test", Integer.class);
        field.setValue(null);
        assertNull(field.getValue());
    }

    @Test
    public void setValue() throws Exception {

        CSVField field = new CSVField("test", Integer.class);
        field.setValue(1);
        assertEquals(1, field.getValue());
    }

    @Test
    public void setValue_IllegalType() throws Exception {

        CSVField field = new CSVField("test", Integer.class);
        try {
            field.setValue("blah");
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
