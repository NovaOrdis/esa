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

import java.util.Date;
import java.util.List;

/**
 * A generic timed event.
 *
 * It has a timestamp and a list of properties. The list is sparse, in that it could contain nulls.
 *
 * Each property has a name, and no two properties of an event can have the same name.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public interface Event {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return may return null
     */
    Date getTimestamp();

    /**
     * Check with the implementation to find out whether the underlying storage or a copy is returned.
     *
     * @return the list of properties for this event in the "preferred" order.
     */
    List<Property> getProperties();

    /**
     * @return may return null if there is no property with this name.
     */
    Property getProperty(String name);

    /**
     * The property list is a zero-indexed list. The timestamp is not part of the list.
     *
     * @return may return null if the index is out of bounds.
     *
     */
    Property getProperty(int index);

    /**
     * Any positive index can be used. If an index that goes beyond the current list boundary is used, the list
     * is extended and the intermediate values are null.
     *
     * @return the previous Property instance stored at the given index, or null.
     *
     * @exception IllegalArgumentException on negative index.
     */
    Property setProperty(int index, Property property);

}
