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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A generic event, contains an arbitrary number of properties.
 *
 * It can be used as such, or it can be subclassed by more specialized events.
 *
 * It also maintains the relative orders of its properties, hence getPropertyList().
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class GenericEvent implements Event {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Map<String, Property> properties;
    private List<String> orderedPropertyNames;

    // Constructors ----------------------------------------------------------------------------------------------------

    public GenericEvent() {

        this.properties = new HashMap<>();
        this.orderedPropertyNames = new ArrayList<>();
    }

    /**
     * @param properties the order matters, and it will be preserved as the event is processed downstream. The
     *                   implementation makes an internal shallow copy.

     */
    public GenericEvent(List<Property> properties) {

        this();

        //noinspection Convert2streamapi
        for(Property p: properties) {
            setProperty(p);
        }
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

        if (properties == null) {

            return null;
        }

        return properties.get(name);
    }

    @Override
    public StringProperty getStringProperty(String name) {

        Property p = getProperty(name);

        if (p != null && p instanceof StringProperty) {
            return (StringProperty)p;
        }

        return null;
    }


    @Override
    public LongProperty getLongProperty(String name) {

        Property p = getProperty(name);

        if (p != null && p instanceof LongProperty) {
            return (LongProperty)p;
        }

        return null;
    }

    @Override
    public IntegerProperty getIntegerProperty(String name) {

        Property p = getProperty(name);

        if (p != null && p instanceof IntegerProperty) {
            return (IntegerProperty)p;
        }

        return null;
    }

    @Override
    public BooleanProperty getBooleanProperty(String name) {

        Property p = getProperty(name);

        if (p != null && p instanceof BooleanProperty) {
            return (BooleanProperty)p;
        }

        return null;
    }

    @Override
    public MapProperty getMapProperty(String name) {

        Property p = getProperty(name);

        if (p != null && p instanceof MapProperty) {
            return (MapProperty)p;
        }

        return null;
    }

    @Override
    public ListProperty getListProperty(String name) {

        Property p = getProperty(name);

        if (p != null && p instanceof ListProperty) {
            return (ListProperty)p;
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
        // only add it to the ordered property name list if it does not exist already
        //

        if (!orderedPropertyNames.contains(propertyName)) {
            orderedPropertyNames.add(propertyName);
        }

        //
        // replace
        //

        return properties.put(propertyName, property);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public List<Property> getPropertyList() {

        List<Property> result = new ArrayList<>();
        //noinspection Convert2streamapi
        for(String name: orderedPropertyNames) {
            result.add(properties.get(name));
        }

        return result;
    }

    public void setStringProperty(String name, String value) {

        if (value == null) {
            removeStringProperty(name);
        }
        else {
            setProperty(new StringProperty(name, value));
        }
    }

    public void removeStringProperty(String name) {

        if (properties.get(name) instanceof StringProperty) {
            properties.remove(name);
        }
    }

    public void setLongProperty(String name, long value) {
        setProperty(new LongProperty(name, value));
    }

    public void removeLongProperty(String name) {

        if (properties.get(name) instanceof LongProperty) {
            properties.remove(name);
        }
    }

    public void setIntegerProperty(String name, int value) {
        setProperty(new IntegerProperty(name, value));
    }

    public void removeIntegerProperty(String name) {

        if (properties.get(name) instanceof IntegerProperty) {
            properties.remove(name);
        }
    }

    public void setBooleanProperty(String name, boolean value) {
        setProperty(new BooleanProperty(name, value));
    }

    public void removeBooleanProperty(String name) {

        if (properties.get(name) instanceof BooleanProperty) {
            properties.remove(name);
        }
    }

    public <T> void setListProperty(String name, List<T> value) {

        setProperty(new ListProperty<>(name, value));
    }

    public void removeListProperty(String name) {
        throw new RuntimeException("NOT YET IMPLEMENTED e58237");
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
