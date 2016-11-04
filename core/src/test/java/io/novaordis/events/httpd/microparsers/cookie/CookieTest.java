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

package io.novaordis.events.httpd.microparsers.cookie;

import io.novaordis.events.ParsingException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/1/16
 */
public class CookieTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(CookieTest.class);


    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void noSemiColonsAcceptedInLogRepresentation() throws Exception {

        try {

            new Cookie("something;somethingelse", null);
            fail("should have thrown exception");
        }
        catch (IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
        }
    }

    @Test
    public void noEqualSignInCookieString() throws Exception {

        try {

            new Cookie("blah", 7L);
            fail("should have thrown exception");
        }
        catch (ParsingException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.startsWith("blah missing from \""));
            assertEquals(7L, e.getLineNumber().longValue());
        }
    }


    // getLiteral() ----------------------------------------------------------------------------------------------------

    @Test
    public void getLiteral_LeadingSpaces() throws Exception {

        String literal = "    a=b";
        Cookie c = new Cookie(literal, null);
        assertEquals(literal, c.getLiteral());
    }

    @Test
    public void getLiteral_TrailingSpaces() throws Exception {

        String literal = "a=b   ";
        Cookie c = new Cookie(literal, null);
        assertEquals(literal, c.getLiteral());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
