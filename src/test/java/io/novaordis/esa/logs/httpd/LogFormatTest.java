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

package io.novaordis.esa.logs.httpd;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class LogFormatTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(LogFormatTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        // duplicate format elements - this should be acceptable
        LogFormat f = new LogFormat(FormatStrings.REMOTE_HOST, FormatStrings.REMOTE_HOST);

        List<FormatString> formatStrings = f.getFormatStrings();
        assertEquals(2, formatStrings.size());
        assertEquals(FormatStrings.REMOTE_HOST, formatStrings.get(0));
        assertEquals(FormatStrings.REMOTE_HOST, formatStrings.get(1));
    }

    @Test
    public void unbalancedDoubleQuotes() throws Exception {

        try {
            new LogFormat(FormatStrings.DOUBLE_QUOTES);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void unbalancedDoubleQuotes2() throws Exception {

        try {
            new LogFormat(
                    FormatStrings.REMOTE_HOST,
                    FormatStrings.DOUBLE_QUOTES,
                    FormatStrings.TIMESTAMP);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void unbalancedSingleQuotes() throws Exception {

        try {
            new LogFormat(FormatStrings.SINGLE_QUOTE);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void getFormatStrings_ReturnsTheUnderlyingStorage() throws Exception {

        FormatString fs = new MockFormatString("A");
        FormatString fs2 = new MockFormatString("b");

        LogFormat logFormat = new LogFormat(fs, fs2);

        List<FormatString> fes = logFormat.getFormatStrings();

        assertEquals(2, fes.size());
        assertEquals(fs, fes.get(0));
        assertEquals(fs2, fes.get(1));

        // test mutability
        fes.set(0, null);

        List<FormatString> fes2 = logFormat.getFormatStrings();

        assertEquals(2, fes2.size());
        assertNull(fes.get(0));
        assertEquals(fs2, fes.get(1));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
