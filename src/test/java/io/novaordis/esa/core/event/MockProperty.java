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
 * @since 2/7/16
 */
public class MockProperty extends PropertyBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Integer priority;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockProperty(String name) {
        this(name, null, 0);
    }

    public MockProperty(String name, Object value) {
        this(name, value, 0);
    }

    public MockProperty(String name, Object value, int priority) {
        super(name, value);
        this.priority = priority;
    }

    // Comparable implementation ---------------------------------------------------------------------------------------

    public int compareTo(Property o) {

        if (!(o instanceof MockProperty)) {
            return super.compareTo(o);
        }

        return priority.compareTo(((MockProperty)o).priority);
    }

    // Property implementation -----------------------------------------------------------------------------------------

    @Override
    public Class getType() {
        throw new RuntimeException("getType() NOT YET IMPLEMENTED");
    }

    @Override
    public Property fromString(String s) throws IllegalArgumentException {
        throw new RuntimeException("fromString() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
