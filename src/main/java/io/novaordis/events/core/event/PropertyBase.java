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
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public abstract class PropertyBase implements Property, Comparable<Property> {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String name;
    private MeasureUnit measureUnit;
    private Format format;

    protected Object value;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param name the property name
     */
    protected PropertyBase(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    // Comparable implementation ---------------------------------------------------------------------------------------

    public int compareTo(@SuppressWarnings("NullableProblems") Property o) {

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
    public void setValue(Object value) {

        if (value == null) {
            this.value = null;
        }
        else {
            this.value = null;
        }
    }

    @Override
    public MeasureUnit getMeasureUnit() {
        return measureUnit;
    }

    @Override
    public Format getFormat() {
        return format;
    }

    @Override
    public String externalizeValue() {

        if (value == null) {
            return null;
        }

        Format format = getFormat();
        return format == null ? value.toString() : format.format(value);
    }

    @Override
    public String externalizeType() {
        return getName();
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
