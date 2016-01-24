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

package io.novaordis.esa.event;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/23/16
 */
public class MockProperty implements Property {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String name;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockProperty(String name) {
        this.name = name;
    }

    // Property implementation -----------------------------------------------------------------------------------------

    @Override
    public String getName() {

        return name;
    }

    @Override
    public Class getType() {
        throw new RuntimeException("getType() NOT YET IMPLEMENTED");
    }

    @Override
    public Object getValue() {
        throw new RuntimeException("getValue() NOT YET IMPLEMENTED");
    }

    @Override
    public MeasureUnit getMeasureUnit() {
        throw new RuntimeException("getMeasureUnit() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
