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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public class GigabyteTest extends MemoryMeasureUnitTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void getLabel() throws Exception {

        String label = MemoryMeasureUnit.GIGABYTE.getLabel();
        assertEquals("GB", label);
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse_GiB() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("GiB");
        assertEquals(MemoryMeasureUnit.GIGABYTE, mmu);
    }

    @Test
    public void parse_KiB_CaseNotImportant() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("gib");
        assertEquals(MemoryMeasureUnit.GIGABYTE, mmu);
    }

    @Test
    public void parse_GB() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("GB");
        assertEquals(MemoryMeasureUnit.GIGABYTE, mmu);
    }

    @Test
    public void parse_G() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("G");
        assertEquals(MemoryMeasureUnit.GIGABYTE, mmu);
    }

    @Test
    public void parse_DeclaredUnit() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("GIGABYTE");
        assertEquals(MemoryMeasureUnit.GIGABYTE, mmu);
    }

    // getConversionFactor() -------------------------------------------------------------------------------------------

    @Test
    public void getConversionFactor_bytesToGigabytes() throws Exception {

        double d = MemoryMeasureUnit.GIGABYTE.getConversionFactor(MemoryMeasureUnit.BYTE);
        assertEquals(1d/1024/1024/1024, d, 0.000001);
    }

    @Test
    public void getConversionFactor_KilobytesToGigabytes() throws Exception {

        double d = MemoryMeasureUnit.GIGABYTE.getConversionFactor(MemoryMeasureUnit.KILOBYTE);
        assertEquals(1d/1024/1024, d, 0.000001);
    }

    @Test
    public void getConversionFactor_MegabytesToGigabytes() throws Exception {

        double d = MemoryMeasureUnit.GIGABYTE.getConversionFactor(MemoryMeasureUnit.MEGABYTE);
        assertEquals(1d/1024, d, 0.000001);
    }

    @Test
    public void getConversionFactor_GigabytesToGigabytes() throws Exception {

        double d = MemoryMeasureUnit.GIGABYTE.getConversionFactor(MemoryMeasureUnit.GIGABYTE);
        assertEquals(1, (int)d);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected MemoryMeasureUnit getMeasureUnitToTest() throws Exception {
        return MemoryMeasureUnit.GIGABYTE;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
