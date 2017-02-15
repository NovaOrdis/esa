/*
 * Copyright (c) 2017 Nova Ordis LLC
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

package io.novaordis.events.gc.g1;

import io.novaordis.events.core.LineParserTest;
import org.junit.Test;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/14/17
 */
public class G1LineParserTest extends LineParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void happyPath() throws Exception {

        //
        // noop for the time being
        //

    }

    @Test
    public void emptyLine() throws Exception {

        //
        // noop for the time being
        //

    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected G1LineParser getLineParserToTest(String format) throws Exception {

        return new G1LineParser();
    }

    @Override
    protected String getValidFormatForLineParserToTest() throws Exception {

        return "g1";
    }

    @Override
    protected String getValidLineForLineParserToTest() throws Exception {

        return "2017-02-14T03:40:58.716-0600: 28.628: [GC pause (G1 Evacuation Pause) (young), 0.2383459 secs]";
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
