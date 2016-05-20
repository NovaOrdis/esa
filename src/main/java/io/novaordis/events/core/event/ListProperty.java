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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A ListProperty maintains the relative order amongst a set of related Objects of the same type.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class ListProperty<V> extends PropertyBase implements Property {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    @SafeVarargs
    public ListProperty(String name, V... elements) {

        super(name, Arrays.asList(elements));
    }

    public ListProperty(String name, List<V> elements) {

        super(name, elements);
    }

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    public String externalizeValue() {

        Format format = getFormat();

        if (format != null) {
            throw new RuntimeException("externalizeValue() NOT YET IMPLEMENTED for non-null format");
        }

        List<V> value = getList();

        String s = "[";

        for(Iterator<V> i = value.iterator(); i.hasNext(); ) {

            V v = i.next();
            if (v instanceof String) {
                s += "\"" + v + "\"";
            }
            else {
                s += v;
            }
            if (i.hasNext()) {
                s += ", ";
            }
        }

        s += "]";

        return s;
    }

    @Override
    public void setValue(Object value) {

        if (value == null) {
            this.value = Collections.emptyList();
        }
        else {
            super.setValue(value);
        }
    }

    // Property implementation -----------------------------------------------------------------------------------------

    @Override
    public Class getType() {
        return List.class;
    }

    @Override
    public Property fromString(String s) throws IllegalArgumentException {

        //
        // if I ever need this, I should use the YAML notation
        //
        throw new RuntimeException("fromString() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return May return an empty list, never null.
     */
    public List<V> getList() {

        //noinspection unchecked
        return (List<V>)getValue();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------


    // Inner classes ---------------------------------------------------------------------------------------------------

}
