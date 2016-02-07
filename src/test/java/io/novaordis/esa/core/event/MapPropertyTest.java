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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class MapPropertyTest extends PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

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
