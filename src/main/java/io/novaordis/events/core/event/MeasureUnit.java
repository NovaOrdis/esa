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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/23/16
 */
public interface MeasureUnit {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * A human readable string, possibly space separated, that is used to represent the measure in user-facing
     * representations, such as a CSV file headers. Example: "bytes", "MB", "GB", "%", etc.
     */
    String getLabel();

    /**
     * Returns the value that can be used to convert 'that' measure unit into this measure unit. For example, if
     * 'that' measure unit is BYTE and this is KILOBYTE, the conversion factor is 1024.
     *
     * May return null if conversion does not make sense for the given measure units (example: percentage).
     *
     * @exception IllegalArgumentException if the measure units cannot converted from one another.
     */
    Double getConversionFactor(MeasureUnit that);

}
