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

import java.util.ArrayList;
import java.util.List;

/**
 * A generic event
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class OldEventImpl extends OldEventBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<OldProperty> properties;

    // Constructors ----------------------------------------------------------------------------------------------------

    public OldEventImpl() {
        this.properties = new ArrayList<>();
    }

    // Event implementation --------------------------------------------------------------------------------------------

    @Override
    public List<OldProperty> getProperties() {
        return properties;
    }

    @Override
    public OldProperty getProperty(String name) {

        for(OldProperty p: properties) {
            if (p.getName().equals(name)) {
                return p;
            }
        }

        return null;
    }

    @Override
    public OldProperty getProperty(int index) {

        if (index < 0 || index >= properties.size()) {
            return null;
        }
        return properties.get(index);
    }

    @Override
    public OldProperty setProperty(int index, OldProperty property) {
        throw new RuntimeException("setProperty() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void addProperty(OldProperty property) {
        properties.add(property);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
