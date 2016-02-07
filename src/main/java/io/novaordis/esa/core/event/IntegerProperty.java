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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class IntegerProperty extends PropertyBase implements Property {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public IntegerProperty(String name) {
        this(name, null);
    }

    public IntegerProperty(String name, Integer value) {
        super(name, value);
    }

    // Property implementation -----------------------------------------------------------------------------------------

    @Override
    public Class getType() {
        return Integer.class;
    }

    @Override
    public Property fromString(String s) throws IllegalArgumentException {

        try {
            int i = Integer.valueOf(s);
            return new IntegerProperty(getName(), i);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("\"" + s + "\" cannot be converted to an IntegerProperty value");
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Integer getInteger() {

        return (Integer)getValue();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
