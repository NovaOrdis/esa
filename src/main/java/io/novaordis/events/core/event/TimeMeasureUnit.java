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
public enum TimeMeasureUnit implements MeasureUnit {

    MILLISECOND("ms", 1000L),
    SECOND("sec", 1000L * 1000L);

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String label;
    private long microseconds;

    // Constructors ----------------------------------------------------------------------------------------------------

    TimeMeasureUnit(String label, long microseconds) {

        this.label = label;
        this.microseconds = microseconds;
    }

    // MeasureUnit implementation --------------------------------------------------------------------------------------

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Double getConversionFactor(MeasureUnit that) {

        if (that == null) {
            throw new IllegalArgumentException("null measure unit");
        }

        if (!(that instanceof TimeMeasureUnit)) {
            throw new IllegalArgumentException(that + " cannot be converted to " + this);
        }

        TimeMeasureUnit thatTimeMeasureUnit = (TimeMeasureUnit)that;

        return (double) thatTimeMeasureUnit.microseconds / microseconds;
    }

    // Public ----------------------------------------------------------------------------------------------------------

}
