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
     * @see PropertyFactory#createInstance(String, Class, Object, Double, MeasureUnit)
     */
    public static Property createInstance(String name, Class type, Object value, MeasureUnit measureUnit) {
        return createInstance(name, type, value, null, measureUnit);
    }

    /**
     * Creates a property instance with the given name, based on the given value and type. If the value and the type
     * match, the property is created right away. If the value is a String, and the type is not a String.class, a
     * conversion is attempted first, and if the conversion is successful, the property is created. Otherwise, an
     * IllegalArgumentException is thrown.
     *
     * @param measureUnit null is acceptable
     *
     * @param conversionFactor the double to multiply the given value to obtain the value to write into the property.
     *                         May be null, in which case it is ignored.
     *
     * @exception IllegalArgumentException if the value and the type do not match, or a conversion from String to the
     * type in question fails.
     */
    public static Property createInstance(
            String name, Class type, Object value, Double conversionFactor, MeasureUnit measureUnit) {

        PropertyBase result;

        if(String.class.equals(type)) {

            if (value != null && !(value instanceof String)) {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            result = new StringProperty(name, (String)value);
        }
        else if(Integer.class.equals(type)) {

            Integer i;

            if (value == null || value instanceof Integer) {
                i = (Integer)value;
            }
            else if (value instanceof String) {
                try {
                    i = Integer.parseInt((String) value);
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("cannot convert \"" + value + "\" to an integer");
                }
            }
            else {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            if (conversionFactor != null && i != null) {
                i = (int)(i * conversionFactor);
            }

            result = new IntegerProperty(name, i);
        }
        else if(Long.class.equals(type)) {

            Long l;

            if (value == null || value instanceof Long) {
                l = (Long)value;
            }
            else if (value instanceof String) {
                try {
                    l = Long.parseLong((String) value);
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("cannot convert \"" + value + "\" to a long");
                }
            }
            else {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            if (conversionFactor != null && l != null) {
                l = (long)(l * conversionFactor);
            }

            result = new LongProperty(name, l);
        }
        else if(Double.class.equals(type)) {

            Double d;

            if (value == null || value instanceof Double) {
                d = (Double)value;
            }
            else if (value instanceof String) {
                try {
                    d = Double.parseDouble((String) value);
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("cannot convert \"" + value + "\" to a double");
                }
            }
            else {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            if (conversionFactor != null && d != null) {
                d = d * conversionFactor;
            }

            result = new DoubleProperty(name, d);
        }
        else if(Float.class.equals(type)) {

            Float f;

            if (value == null || value instanceof Float) {
                f = (Float)value;
            }
            else if (value instanceof String) {
                try {
                    f = Float.parseFloat((String) value);
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("cannot convert \"" + value + "\" to a float");
                }
            }
            else {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            if (conversionFactor != null && f != null) {
                f = (float)(f * conversionFactor);
            }

            result = new FloatProperty(name, f);
        }
        else if(Map.class.equals(type)) {

            if (value != null && !(value instanceof Map)) {
                throw new IllegalArgumentException(
                        "cannot create a " + type + " property with a " + value.getClass().getSimpleName() + " value");
            }

            //noinspection unchecked
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
