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

package io.novaordis.esa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public abstract class EventBase implements Event {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Date timestamp;

    private SortedMap<String, Object> properties;

    // Constructors ----------------------------------------------------------------------------------------------------

    protected EventBase() {
        this.properties = new TreeMap<>();
    }

    // Event implementation --------------------------------------------------------------------------------------------

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public int getPropertyCount() {
        return timestamp == null ? 0 : 1;
    }

    @Override
    public List<String> getPropertyNames() {

        // the sorted map returns its keys in ascending order
        return new ArrayList<>(properties.keySet());
    }

    @Override
    public Object getPropertyValue(String name) {
        return properties.get(name);
    }

    @Override
    public Object setPropertyValue(String name, Object value) {

        if (value == null) {
            return properties.remove(name);
        }

        return properties.put(name, value);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the old timestamp value or null if none was set
     */
    public Date setTimestamp(Date d) {

        Date old = this.timestamp;
        this.timestamp = d;
        return old;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
