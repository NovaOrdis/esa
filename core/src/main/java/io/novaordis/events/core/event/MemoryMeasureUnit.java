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
public enum MemoryMeasureUnit implements MeasureUnit {

    BYTE("bytes", 1),
    KILOBYTE("KB", 1024),
    MEGABYTE("MB", 1024 * 1024),
    GIGABYTE("GB", 1024 * 1024 * 1024);

    // Constants -------------------------------------------------------------------------------------------------------

    /**
     * Semantically equivalent with valueOf(), we just cannot override static functions.
     * If it cannot find a representation we know, it defaults to valueOf().
     */
    public static MemoryMeasureUnit parse(String s) {

        if (s == null) {
            throw new IllegalArgumentException("null memory measure unit string");
        }

        String lcs = s.toLowerCase();

        if ("b".equals(lcs) || "bytes".equals(lcs)) {
            return BYTE;
        }
        else if ("kb".equals(lcs) || "kib".equals(lcs) || "k".equals(lcs)) {
            return KILOBYTE;
        }
        else if ("mb".equals(lcs) || "mib".equals(lcs) || "m".equals(lcs)) {
            return MEGABYTE;
        }
        else if ("gb".equals(lcs) || "gib".equals(lcs) || "g".equals(lcs)) {
            return GIGABYTE;
        }

        return MemoryMeasureUnit.valueOf(s);
    }

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String label;
    private int bytes;

    // Constructors ----------------------------------------------------------------------------------------------------

    MemoryMeasureUnit(String label, int bytes) {

        this.label = label;
        this.bytes = bytes;
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

        if (!(that instanceof MemoryMeasureUnit)) {
            throw new IllegalArgumentException(that + " cannot be converted to " + this);
        }

        MemoryMeasureUnit thatMemoryMeasureUnit = (MemoryMeasureUnit)that;

        return (double)thatMemoryMeasureUnit.bytes / bytes;
    }

    // Public ----------------------------------------------------------------------------------------------------------

}
