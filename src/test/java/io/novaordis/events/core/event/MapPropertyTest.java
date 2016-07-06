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

package io.novaordis.events.core.event;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class MapPropertyTest extends PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MapPropertyTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void value() throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("test-key", "test-value");

        MapProperty sp = new MapProperty("test-name", map);

        assertEquals("test-name", sp.getName());

        Map map2 = (Map)sp.getValue();
        Map map3 = sp.getMap();
        assertEquals(map2, map3);
        assertEquals(1, map3.size());
        assertEquals("test-value", map3.get("test-key"));

        assertEquals(Map.class, sp.getType());
    }

    @Test
    public void fromString() throws Exception {

        //
        // noop - fromString() currently not implemented for MapProperty
        //
    }

    @Test
    public void externalizeValue_MapProperty() throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 7);
        MapProperty mp = new MapProperty("test-name", map);

        String externalizedValue = mp.externalizeValue();

        log.info("externalized value: " + externalizedValue);

        assertTrue(externalizedValue.matches("\\{.*=.*,.*=.*\\}"));
        assertTrue(externalizedValue.contains("key1=value1"));
        assertTrue(externalizedValue.contains("key2=7"));
    }

    @Test
    public void externalizeValue_Key() throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 7);
        MapProperty mp = new MapProperty("test-name", map);

        String externalizedValue = mp.externalizeValue(null);
        assertNull(externalizedValue);

        externalizedValue = mp.externalizeValue("no-such-key");
        assertNull(externalizedValue);

        externalizedValue = mp.externalizeValue("key1");
        assertEquals("value1", externalizedValue);

        externalizedValue = mp.externalizeValue("key2");
        assertEquals("7", externalizedValue);
    }

    @Test
    public void externalizeType_MapProperty() throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        MapProperty mp = new MapProperty("test-name", map);
        assertEquals("test-name", mp.externalizeType());
    }

    // getMap() --------------------------------------------------------------------------------------------------------

    @Test
    public void getMap_emptyMapNeverNull() throws Exception {

        MapProperty mp = new MapProperty("test", null);

        Map<String, Object> map = mp.getMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected MapProperty getPropertyToTest(String name) {

        Map<String, Object> map = new HashMap<>();
        map.put("test-key", "test-value");
        return new MapProperty(name, map);
    }

    @Override
    protected Map getAppropriateValueForPropertyToTest() {

        Map m = new HashMap<>();
        //noinspection unchecked
        m.put("test-key", "test-value");
        return m;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
