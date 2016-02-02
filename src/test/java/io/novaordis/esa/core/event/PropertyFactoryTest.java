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

package io.novaordis.esa.core.event;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/2/16
 */
public class PropertyFactoryTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(PropertyFactoryTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // createInstance() ------------------------------------------------------------------------------------------------

    @Test
    public void createInstance_String() throws Exception {

        StringProperty sp = (StringProperty)PropertyFactory.createInstance("test", String.class, "something", null);

        assertEquals("test", sp.getName());
        assertEquals(String.class, sp.getType());
        assertEquals("something", sp.getString());
    }

    @Test
    public void createInstance_String_Null() throws Exception {

        StringProperty sp = (StringProperty)PropertyFactory.createInstance("test", String.class, null, null);
        assertEquals("test", sp.getName());
        assertEquals(String.class, sp.getType());
        assertNull(sp.getString());
    }

    @Test
    public void createInstance_String_TypeMismatch() throws Exception {

        try {
            PropertyFactory.createInstance("test", String.class, 1, null);
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void createInstance_Integer() throws Exception {

        IntegerProperty ip = (IntegerProperty)PropertyFactory.createInstance("test", Integer.class, 1, null);

        assertEquals("test", ip.getName());
        assertEquals(Integer.class, ip.getType());
        assertEquals(1, ip.getInteger().intValue());
    }

    @Test
    public void createInstance_Integer_Null() throws Exception {

        IntegerProperty ip = (IntegerProperty)PropertyFactory.createInstance("test", Integer.class, null, null);

        assertEquals("test", ip.getName());
        assertEquals(Integer.class, ip.getType());
        assertNull(ip.getInteger());
    }

    @Test
    public void createInstance_Integer_TypeMismatch() throws Exception {

        try {
            PropertyFactory.createInstance("test", Integer.class, "1", null);
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void createInstance_Long() throws Exception {

        LongProperty ip = (LongProperty)PropertyFactory.createInstance("test", Long.class, 1L, null);

        assertEquals("test", ip.getName());
        assertEquals(Long.class, ip.getType());
        assertEquals(1L, ip.getLong().longValue());
    }

    @Test
    public void createInstance_Long_Null() throws Exception {

        LongProperty ip = (LongProperty)PropertyFactory.createInstance("test", Long.class, null, null);

        assertEquals("test", ip.getName());
        assertEquals(Long.class, ip.getType());
        assertNull(ip.getLong());
    }

    @Test
    public void createInstance_Long_TypeMismatch() throws Exception {

        try {
            PropertyFactory.createInstance("test", Long.class, "1", null);
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void createInstance_Map() throws Exception {

        Map<String, String> map = new HashMap<>();
        MapProperty mp = (MapProperty)PropertyFactory.createInstance("test", Map.class, map, null);

        assertEquals("test", mp.getName());
        assertEquals(Map.class, mp.getType());
        assertEquals(map, mp.getMap());
    }

    @Test
    public void createInstance_Map_Null() throws Exception {

        MapProperty mp = (MapProperty)PropertyFactory.createInstance("test", Map.class, null, null);

        assertEquals("test", mp.getName());
        assertEquals(Map.class, mp.getType());
        assertNull(mp.getMap());
    }

    @Test
    public void createInstance_Map_TypeMismatch() throws Exception {

        try {
            PropertyFactory.createInstance("test", Map.class, "1", null);
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
