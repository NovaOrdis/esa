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
public class KilobyteTest extends MemoryMeasureUnitTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void getLabel() throws Exception {

        String label = MemoryMeasureUnit.KILOBYTE.getLabel();
        assertEquals("KB", label);
    }

    // parse() ---------------------------------------------------------------------------------------------------------

    @Test
    public void parse_KiB() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("KiB");
        assertEquals(MemoryMeasureUnit.KILOBYTE, mmu);
    }

    @Test
    public void parse_KiB_CaseNotImportant() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("kib");
        assertEquals(MemoryMeasureUnit.KILOBYTE, mmu);
    }

    @Test
    public void parse_KB() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("KB");
        assertEquals(MemoryMeasureUnit.KILOBYTE, mmu);
    }

    @Test
    public void parse_K() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("K");
        assertEquals(MemoryMeasureUnit.KILOBYTE, mmu);
    }

    @Test
    public void parse_DeclaredUnit() throws Exception {

        MemoryMeasureUnit mmu = MemoryMeasureUnit.parse("KILOBYTE");
        assertEquals(MemoryMeasureUnit.KILOBYTE, mmu);
    }

    // getConversionFactor() -------------------------------------------------------------------------------------------

    @Test
    public void getConversionFactor_bytesToKilobytes() throws Exception {

        double d = MemoryMeasureUnit.KILOBYTE.getConversionFactor(MemoryMeasureUnit.BYTE);
        assertEquals(1d/1024, d, 0.000001);
    }

    @Test
    public void getConversionFactor_KilobytesToKilobytes() throws Exception {

        double d = MemoryMeasureUnit.KILOBYTE.getConversionFactor(MemoryMeasureUnit.KILOBYTE);
        assertEquals(1, (int)d);
    }

    @Test
    public void getConversionFactor_MegabytesToKilobytes() throws Exception {

        double d = MemoryMeasureUnit.KILOBYTE.getConversionFactor(MemoryMeasureUnit.MEGABYTE);
        assertEquals(1024, (int)d);
    }

    @Test
    public void getConversionFactor_GigabytesToKilobytes() throws Exception {

        double d = MemoryMeasureUnit.KILOBYTE.getConversionFactor(MemoryMeasureUnit.GIGABYTE);
        assertEquals(1024 * 1024, (int)d);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected MemoryMeasureUnit getMeasureUnitToTest() throws Exception {
        return MemoryMeasureUnit.KILOBYTE;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
