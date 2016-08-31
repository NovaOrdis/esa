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
public class MegabyteTest extends MemoryMeasureUnitTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void getLabel() throws Exception {

        String label = MemoryMeasureUnit.MEGABYTE.getLabel();
        assertEquals("MB", label);
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse_MiB() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("MiB");
        assertEquals(MemoryMeasureUnit.MEGABYTE, mmu);
    }

    @Test
    public void parse_MiB_CaseNotImportant() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("mib");
        assertEquals(MemoryMeasureUnit.MEGABYTE, mmu);
    }

    @Test
    public void parse_MB() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("MB");
        assertEquals(MemoryMeasureUnit.MEGABYTE, mmu);
    }

    @Test
    public void parse_M() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("M");
        assertEquals(MemoryMeasureUnit.MEGABYTE, mmu);
    }

    @Test
    public void parse_DeclaredUnit() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("MEGABYTE");
        assertEquals(MemoryMeasureUnit.MEGABYTE, mmu);
    }

    // getConversionFactor() -------------------------------------------------------------------------------------------

    @Test
    public void getConversionFactor_bytesToMegabytes() throws Exception {

        double d = MemoryMeasureUnit.MEGABYTE.getConversionFactor(MemoryMeasureUnit.BYTE);
        assertEquals(1d/1024/1024, d, 0.000001);
    }

    @Test
    public void getConversionFactor_KilobytesToMegabytes() throws Exception {

        double d = MemoryMeasureUnit.MEGABYTE.getConversionFactor(MemoryMeasureUnit.KILOBYTE);
        assertEquals(1d/1024, d, 0.000001);
    }

    @Test
    public void getConversionFactor_MegabytesToMegabytes() throws Exception {

        double d = MemoryMeasureUnit.MEGABYTE.getConversionFactor(MemoryMeasureUnit.MEGABYTE);
        assertEquals(1, (int)d);
    }

    @Test
    public void getConversionFactor_GigabytesToMegabytes() throws Exception {

        double d = MemoryMeasureUnit.MEGABYTE.getConversionFactor(MemoryMeasureUnit.GIGABYTE);
        assertEquals(1024, (int)d);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected MemoryMeasureUnit getMeasureUnitToTest() throws Exception {
        return MemoryMeasureUnit.MEGABYTE;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
