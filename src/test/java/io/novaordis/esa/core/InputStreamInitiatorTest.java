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

package io.novaordis.esa.core;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class InputStreamInitiatorTest extends InitiatorTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(InputStreamInitiatorTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    @Test
    public void conversionLogic() throws Exception {

        //
        // input stream initiators only accept input stream conversion logic
        //

        Initiator initiator = getComponentToTest("test");

        assertNull(initiator.getConversionLogic());

        MockConversionLogic conversionLogic = new MockConversionLogic();

        try {
            initiator.setConversionLogic(conversionLogic);
            fail("should throw IllegalArgumentException because we're feeding a non-InputStreamConversionLogic");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());

        }

        assertNull(initiator.getConversionLogic());

        InputStreamConversionLogic inputStreamConversionLogic = new MockInputStreamConversionLogic();

        initiator.setConversionLogic(inputStreamConversionLogic);

        assertEquals(inputStreamConversionLogic, initiator.getConversionLogic());
    }

    @Test
    public void toStringWithNoName() {

        InputStreamInitiator initiator = new InputStreamInitiator();

        String s = initiator.toString();

        log.info(s);

        assertTrue(s.matches("InputStreamInitiator\\[.*\\]"));
    }

    @Test
    public void inputStream() throws Exception {

        InputStreamInitiator initiator = getComponentToTest("test");

        assertNull(initiator.getInputStream());

        InputStream is = new ByteArrayInputStream(new byte[0]);

        initiator.setInputStream(is);

        assertEquals(is, initiator.getInputStream());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected InputStreamInitiator getComponentToTest(String name) {
        return new InputStreamInitiator(name);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
