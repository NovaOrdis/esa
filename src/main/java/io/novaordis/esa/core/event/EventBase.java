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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class EventBase implements Event {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Map<String, Property> properties;

    // Constructors ----------------------------------------------------------------------------------------------------

    protected EventBase() {

        this.properties = new HashMap<>();
    }

    // Event implementation --------------------------------------------------------------------------------------------

    @Override
    public Set<Property> getProperties() {

        HashSet<Property> result = new HashSet<>();

        for(Property p: properties.values()) {
            if (!result.add(p)) {
                throw new IllegalStateException(this + " property map contains duplicate values");
            }
        }

        return result;
    }

    @Override
    public Property getProperty(String name) {

        return properties.get(name);
    }

    @Override
    public LongProperty getLongProperty(String name) {

        Property p = properties.get(name);

        if (p != null && p instanceof LongProperty) {
            return (LongProperty)p;
        }

        return null;
    }

    @Override
    public IntegerProperty getIntegerProperty(String name) {

        Property p = properties.get(name);

        if (p != null && p instanceof IntegerProperty) {
            return (IntegerProperty)p;
        }

        return null;
    }

    @Override
    public MapProperty getMapProperty(String name) {

        Property p = properties.get(name);

        if (p != null && p instanceof MapProperty) {
            return (MapProperty)p;
        }

        return null;
    }

    @Override
    public Property setProperty(Property property) {

        if (property == null) {
            throw new IllegalArgumentException("null property");
        }

        String propertyName = property.getName();

        //
        // if it is a MapProperty, merge contents
        //

        if (property instanceof MapProperty) {

            Property existent = properties.get(propertyName);

            if (existent instanceof MapProperty) {

                //
                // merge instead of replacing
                //

                MapProperty existentMapProperty = (MapProperty) existent;
                existentMapProperty.getMap().putAll(((MapProperty) property).getMap());
                return existentMapProperty;
            }
        }

        //
        // replace
        //

        return properties.put(propertyName, property);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
