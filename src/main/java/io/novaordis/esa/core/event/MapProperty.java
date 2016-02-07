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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class MapProperty extends PropertyBase implements Property {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * Creates a new inner map.
     */
    public MapProperty(String name) {
        this(name, new HashMap<>());
    }

    public MapProperty(String name, Map<String, Object> value) {
        super(name, value);
    }

    // Property implementation -----------------------------------------------------------------------------------------

    @Override
    public Class getType() {
        return Map.class;
    }

    @Override
    public Property fromString(String s) throws IllegalArgumentException {

        //
        // if I ever need this, I should use the YAML notation
        //
        throw new RuntimeException("fromString() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Map<String, Object> getMap() {

        //noinspection unchecked
        return (Map<String, Object>)getValue();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
