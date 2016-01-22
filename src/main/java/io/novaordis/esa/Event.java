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

import java.util.Date;

/**
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
     * @return the value corresponding to the specified format element or null if there is no corresponding value
     * in the log event ("-" in the httpd logs, or whether the log format does contain the given format element.
     *
     * TODO: maybe this belongs to a "LogEvent" sub-interface, as we may have events that do not come from logs,
     *       so they cannot be associated with format elements.
     */
    Object getValue(FormatElement e);

    /**
     * @param value the value associated with the given FormatElement. Can be null, and this has the semantics of
     *              "erasing" the old value, if any.
     *
     * @return the previous value, if any, or null
     *
     * TODO: maybe this belongs to a "LogEvent" sub-interface, as we may have events that do not come from logs,
     *       so they cannot be associated with format elements.
     *
     * @exception IllegalArgumentException if the value's type does not match the format element.
     */
    Object setValue(FormatElement e, Object value);

    /**
     * @return the number of non-null values for this event. Non-null time stamp counts as a value.
     */
    int getValueCount();

}
