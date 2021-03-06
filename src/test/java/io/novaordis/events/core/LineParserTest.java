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

package io.novaordis.events.core;

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.FaultEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/5/16
 */
public abstract class LineParserTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void happyPath() throws Exception {

        String format = getValidFormatForLineParserToTest();
        LineParser lp = getLineParserToTest(format);
        String line = getValidLineForLineParserToTest();

        Event e = lp.parseLine(7L, line);

        assertFalse(e instanceof FaultEvent);

        assertEquals(7L, e.getLongProperty(Event.LINE_NUMBER_PROPERTY_NAME).getLong().longValue());
    }

    @Test
    public void emptyLine() throws Exception {

        String format = getValidFormatForLineParserToTest();
        LineParser lp = getLineParserToTest(format);

        //
        // test behavior on empty lines - the default is to ignore them and return null
        //
        Event e = lp.parseLine(10L, "");
        assertNull(e);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract LineParser getLineParserToTest(String format) throws Exception;

    protected abstract String getValidFormatForLineParserToTest() throws Exception;

    protected abstract String getValidLineForLineParserToTest() throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
