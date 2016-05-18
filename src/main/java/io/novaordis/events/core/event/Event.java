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

import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public interface Event {

    // Constants -------------------------------------------------------------------------------------------------------

    // where it makes sense, it is maintained as a LongProperty
    String LINE_NUMBER_PROPERTY_NAME = "line_number";

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    Set<Property> getProperties();

    /**
     * @return null if there is no such property.
     */
    Property getProperty(String name);

    /**
     * @return the corresponding StringProperty or null if there is no such StringProperty. Note that the method will
     * return null if a property with the given name exists, but it is not a StringProperty.
     */
    StringProperty getStringProperty(String stringPropertyName);

    /**
     * @return the corresponding LongProperty or null if there is no such LongProperty. Note that the method will
     * return null if a property with the given name exists, but it is not a LongProperty.
     */
    LongProperty getLongProperty(String longPropertyName);

    /**
     * @return the corresponding IntegerProperty or null if there is no such IntegerProperty. Note that the method will
     * return null if a property with the given name exists, but it is not a IntegerProperty.
     */
    IntegerProperty getIntegerProperty(String integerPropertyName);

    /**
     * @return the corresponding BooleanProperty or null if there is no such BooleanProperty. Note that the method will
     * return null if a property with the given name exists, but it is not a BooleanProperty.
     */
    BooleanProperty getBooleanProperty(String booleanPropertyName);

    /**
     * @return the corresponding MapProperty or null if there is no such MapProperty. Note that the method will
     * return null if a property with the given name exists, but it is not a MapProperty.
     */
    MapProperty getMapProperty(String mapPropertyName);

    /**
     * Stores the property, replacing the old one if exists.
     *
     * If it is a map and the map already exists, the contents will be merged.
     *
     * If the implementation supports it, the order in which the properties are set is remembered, so methods
     * like getPropertyList() make sense for those.
     *
     * @return the old property with the same name, or null.
     *
     * @exception IllegalArgumentException on null argument.
     */
    Property setProperty(Property property);

}
