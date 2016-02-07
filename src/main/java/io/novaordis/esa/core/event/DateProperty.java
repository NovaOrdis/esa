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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class DateProperty extends PropertyBase implements Property {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public DateProperty(String name) {
        this(name, null);
    }

    public DateProperty(String name, Date value) {
        super(name, value);
        setFormat(new SimpleDateFormat("yy/MM/dd HH:mm:ss"));
    }

    // Property implementation -----------------------------------------------------------------------------------------

    @Override
    public Class getType() {
        return Date.class;
    }

    @Override
    public Property fromString(String s) throws IllegalArgumentException {

        try {
            Date d = ((DateFormat)getFormat()).parse(s);
            return new DateProperty(getName(), d);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("\"" + s + "\" cannot be converted to a DateProperty value");
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Date getDate() {

        return (Date)getValue();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
