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

import io.novaordis.events.core.event.DateProperty;
import io.novaordis.events.core.event.DoubleProperty;
import io.novaordis.events.core.event.FloatProperty;
import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.StringProperty;
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
public class FieldTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(FieldTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void unbalancedParentheses() throws Exception {

        try {
            new Field("a)");
            fail("should throw exception");
        }
        catch(InvalidFieldException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("unbalanced"));
        }
    }

    @Test
    public void stringField() throws Exception {

        Field f = new Field("some-string", String.class);

        assertEquals("some-string", f.getName());
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void stringField2() throws Exception {

        Field f = new Field("some-string(string)");

        assertEquals("some-string", f.getName());
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_SimpleString() throws Exception {

        Field f = new Field("some-string");

        assertEquals("some-string", f.getName());
        assertEquals(String.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_Time() throws Exception {

        Field f = new Field("timestamp(time:yy/MM/dd HH:mm:ss)");

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
            new Field("timestamp(time:blah)");
        }
        catch(InvalidFieldException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("invalid timestamp format \"blah\""));
            IllegalArgumentException cause = (IllegalArgumentException)e.getCause();
            assertNotNull(cause);
        }
    }

    @Test
    public void fieldSpecificationParsing_Integer() throws Exception {

        Field f = new Field("a(int)");

        assertEquals("a", f.getName());
        assertEquals(Integer.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_Long() throws Exception {

        Field f = new Field("a(long)");

        assertEquals("a", f.getName());
        assertEquals(Long.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_Float() throws Exception {

        Field f = new Field("a(float)");

        assertEquals("a", f.getName());
        assertEquals(Float.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_Double() throws Exception {

        Field f = new Field("a(double)");

        assertEquals("a", f.getName());
        assertEquals(Double.class, f.getType());
        assertNull(f.getValue());
    }

    @Test
    public void fieldSpecificationParsing_InvalidType() throws Exception {

        try {
            new Field("fieldA(ms)");
            fail("should throw exception");
        }
        catch(InvalidFieldException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("invalid field type specification \"ms\""));
        }
    }

    // toProperty() ----------------------------------------------------------------------------------------------------

    @Test
    public void toProperty_String() throws Exception {

        Field f = new Field("test", String.class);
        StringProperty sp = (StringProperty)f.toProperty("blah");
        assertEquals("test", sp.getName());
        assertEquals("blah", sp.getValue());
    }

    @Test
    public void toProperty_Integer() throws Exception {

        Field f = new Field("test", Integer.class);
        IntegerProperty ip = (IntegerProperty)f.toProperty("1");
        assertEquals("test", ip.getName());
        assertEquals(1, ip.getInteger().intValue());
    }

    @Test
    public void toProperty_Integer_InvalidValue() throws Exception {

        Field f = new Field("test", Integer.class);

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

        Field f = new Field("test", Long.class);
        LongProperty lp = (LongProperty)f.toProperty("1");
        assertEquals("test", lp.getName());
        assertEquals(1, lp.getLong().longValue());
    }

    @Test
    public void toProperty_Long_InvalidValue() throws Exception {

        Field f = new Field("test", Long.class);

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

        Field f = new Field("test", Float.class);
        FloatProperty fp = (FloatProperty)f.toProperty("1.1");
        assertEquals("test", fp.getName());
        assertEquals(1.1f, fp.getFloat().floatValue(), 0.0001);
    }

    @Test
    public void toProperty_Float_InvalidValue() throws Exception {

        Field f = new Field("test", Float.class);

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

        Field f = new Field("test", Double.class);
        DoubleProperty fp = (DoubleProperty)f.toProperty("1.1");
        assertEquals("test", fp.getName());
        assertEquals(1.1d, fp.getDouble().doubleValue(), 0.0001);
    }

    @Test
    public void toProperty_Double_InvalidValue() throws Exception {

        Field f = new Field("test", Double.class);

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

        Field f = new Field("test", Date.class);
        f.setFormat(new SimpleDateFormat("yyyy"));
        DateProperty dp = (DateProperty)f.toProperty("2016");
        assertEquals("test", dp.getName());
        long time = dp.getDate().getTime();
        long reference = new SimpleDateFormat("yyyy").parse("2016").getTime();
        assertEquals(time, reference);
    }

    @Test
    public void toProperty_Date_InvalidValue() throws Exception {

        Field f = new Field("test", Date.class);
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

        Field field = new Field("test", Integer.class);
        field.setValue(null);
        assertNull(field.getValue());
    }

    @Test
    public void setValue() throws Exception {

        Field field = new Field("test", Integer.class);
        field.setValue(1);
        assertEquals(1, field.getValue());
    }

    @Test
    public void setValue_IllegalType() throws Exception {

        Field field = new Field("test", Integer.class);
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