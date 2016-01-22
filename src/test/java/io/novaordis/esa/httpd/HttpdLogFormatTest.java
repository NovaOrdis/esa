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

package io.novaordis.esa.httpd;

import io.novaordis.esa.FormatElement;
import io.novaordis.esa.LogFormatTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogFormatTest extends LogFormatTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLogFormatTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        // duplicate format elements - this should be acceptable
        HttpdLogFormat f = new HttpdLogFormat(HttpdFormatElement.REMOTE_HOST, HttpdFormatElement.REMOTE_HOST);

        List<HttpdFormatElement> formatElements = f.getFormatElements();
        assertEquals(2, formatElements.size());
        assertEquals(HttpdFormatElement.REMOTE_HOST, formatElements.get(0));
        assertEquals(HttpdFormatElement.REMOTE_HOST, formatElements.get(1));
    }

    @Test
    public void unbalancedDoubleQuotes() throws Exception {

        try {
            new HttpdLogFormat(HttpdFormatElement.DOUBLE_QUOTES);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void unbalancedDoubleQuotes2() throws Exception {

        try {
            new HttpdLogFormat(
                    HttpdFormatElement.REMOTE_HOST,
                    HttpdFormatElement.DOUBLE_QUOTES,
                    HttpdFormatElement.TIMESTAMP);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void unbalancedSingleQuotes() throws Exception {

        try {
            new HttpdLogFormat(HttpdFormatElement.SINGLE_QUOTE);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected HttpdLogFormat getLogFormatToTest(FormatElement... es)  {

        HttpdFormatElement[] arg = new HttpdFormatElement[es.length];
        int i = 0;
        for(FormatElement e: es) {
            arg[i ++] = (HttpdFormatElement)e;
        }
        return new HttpdLogFormat(arg);
    }

    @Override
    protected HttpdFormatElement getTestFormatElement() {

        return HttpdFormatElement.REMOTE_HOST;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
