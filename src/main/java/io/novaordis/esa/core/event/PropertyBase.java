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

import java.text.Format;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public abstract class PropertyBase implements Property, Comparable<Property> {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String name;
    private Object value;
    private MeasureUnit measureUnit;
    private Format format;

    // Constructors ----------------------------------------------------------------------------------------------------

    protected PropertyBase(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    // Comparable implementation ---------------------------------------------------------------------------------------

    public int compareTo(Property o) {

        if (o == null) {
            throw new NullPointerException("null property");
        }

        return name.compareTo(o.getName());
    }

    // Property implementation -----------------------------------------------------------------------------------------

    @Override
    public String getName() {

        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public MeasureUnit getMeasureUnit() {
        return measureUnit;
    }

    @Override
    public Format getFormat() {
        return format;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setMeasureUnit(MeasureUnit measureUnit) {
        this.measureUnit = measureUnit;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    @Override
    public String toString() {

        if (value == null) {
            return name;
        }

        return name + "=" + value;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
