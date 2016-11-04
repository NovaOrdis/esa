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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class BooleanProperty extends PropertyBase implements Property {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public BooleanProperty(String name) {
        this(name, true);
    }

    public BooleanProperty(String name, Boolean value) {
        super(name, value);
    }

    // Property implementation -----------------------------------------------------------------------------------------

    @Override
    public Class getType() {
        return Boolean.class;
    }

    @Override
    public Property fromString(String s) throws IllegalArgumentException {

        if (s == null) {
            throw new IllegalArgumentException("null string");
        }

        if (!"TRUE".equals(s.toUpperCase()) && !"FALSE".equals(s.toUpperCase())) {
            throw new IllegalArgumentException("\"" + s + "\" cannot be converted to an BooleanProperty value");
        }

        boolean b = Boolean.valueOf(s);
        return new BooleanProperty(getName(), b);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Boolean getBoolean() {

        return (Boolean)getValue();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
