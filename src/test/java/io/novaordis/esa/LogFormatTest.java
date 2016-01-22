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

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public abstract class LogFormatTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void createPatternsMakesDifferentInstances() throws Exception {

        LogFormat format = getLogFormatToTest();

        Pattern p = format.createPattern();
        assertNotNull(p);

        Pattern p2 = format.createPattern();
        assertNotNull(p2);

        assertNotEquals(p, p2);
    }

    @Test
    public void getFormatElements_ReturnsTheUnderlyingStorage() throws Exception {

        FormatElement e = getTestFormatElement();
        FormatElement e2 = getTestFormatElement();

        LogFormat format = getLogFormatToTest(e, e2);

        List<? extends FormatElement> fes = format.getFormatElements();

        assertEquals(2, fes.size());
        assertEquals(e, fes.get(0));
        assertEquals(e2, fes.get(1));

        fes.remove(0);

        List<? extends FormatElement> fes2 = format.getFormatElements();

        assertEquals(1, fes2.size());
        assertEquals(e2, fes.get(0));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract LogFormat getLogFormatToTest(FormatElement... e);

    protected abstract FormatElement getTestFormatElement();

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
