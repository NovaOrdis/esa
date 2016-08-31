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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public abstract class MeasureUnitTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MeasureUnitTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void instance() throws Exception {

        MeasureUnit mu = getMeasureUnitToTest();
        assertNotNull(mu);
    }

    @Test
    public void labelIsNeverNull() throws Exception {

        MeasureUnit mu = getMeasureUnitToTest();

        String label = mu.getLabel();
        assertNotNull(label);
    }

    // conversion factor -----------------------------------------------------------------------------------------------

    @Test
    public void getConversionFactor_nullMeasureUnit() throws Exception {

        MeasureUnit m = getMeasureUnitToTest();

        try {
            m.getConversionFactor(null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("null measure unit", msg);
        }
    }

    @Test
    public void getConversionFactor_unmatchedMeasureUnit() throws Exception {

        MeasureUnit m = getMeasureUnitToTest();

        MockMeasureUnit mmu = new MockMeasureUnit();

        try {
            m.getConversionFactor(mmu);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("cannot be converted to"));
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract MeasureUnit getMeasureUnitToTest() throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
