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

import java.util.Map;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/2/16
 */
public class PropertyFactory {

    // Constants -------------------------------------------------------------------------------------------------------

    /**
     * @param measureUnit null is acceptable
     */
    public static Property createInstance(String name, Class type, Object value, MeasureUnit measureUnit) {

        PropertyBase result = null;

        if(String.class.equals(type)) {

            if (value != null && !(value instanceof String)) {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            result = new StringProperty(name, (String)value);
        }
        else if(Integer.class.equals(type)) {

            if (value != null && !(value instanceof Integer)) {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            result = new IntegerProperty(name, (Integer)value);
        }
        else if(Long.class.equals(type)) {

            if (value != null && !(value instanceof Long)) {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            result = new LongProperty(name, (Long)value);
        }
        else if(Double.class.equals(type)) {

            if (value != null && !(value instanceof Double)) {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            result = new DoubleProperty(name, (Double)value);
        }
        else if(Map.class.equals(type)) {

            if (value != null && !(value instanceof Map)) {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            result = new MapProperty(name, (Map)value);
        }
        else {
            throw new RuntimeException("createInstance() for " + type + " NOT YET IMPLEMENTED");
        }

        result.setMeasureUnit(measureUnit);
        return result;
    }

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
