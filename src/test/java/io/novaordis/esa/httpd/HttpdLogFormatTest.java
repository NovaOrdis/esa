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

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogFormatTest extends LogFormatTest {

    // Constants -------------------------------------------------------------------------------------------------------

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
    public void pattern() throws Exception {

        HttpdLogFormat logFormat = new HttpdLogFormat(HttpdFormatElement.REMOTE_HOST);

        Pattern pattern =  logFormat.createPattern();

        String patternAsString = pattern.pattern();
        assertEquals("", patternAsString);
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
