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

import java.text.Format;

/**
 * A typed key/value pair. Optionally, it can have a measure unit, a format, etc.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public interface Property extends Comparable<Property> {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * A human readable, possibly multi-word, name. Example: "Post-Collection Old Generation Size". When processed
     * for display, the name may optionally be followed by the measure unit, if present. Example:
     * "Post-Collection Old Generation Size (MB)".
     *
     * Case sensitive.
     */
    String getName();

    Object getValue();

    /**
     * Null is acceptable.
     */
    void setValue(Object value);

    Class getType();

    /**
     * May return null if the property is non-dimensional (a path, for example)
     */
    MeasureUnit getMeasureUnit();

    /**
     * May return null.
     */
    Format getFormat();

    /**
     * Builds a property matching this from the given string.
     *
     * @exception IllegalArgumentException if the string cannot be converted into a property of the same kind.
     */
    Property fromString(String s) throws IllegalArgumentException;

    /**
     * @return the externalized value of the property's value - how it would appear in a text file - or null if the
     * value is null.
     */
    String externalizeValue();

    String externalizeType();

}
