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

import io.novaordis.esa.LogEventFactoryTest;
import io.novaordis.esa.TestDate;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/21/16
 */
public class HttpdLogEventFactoryTest extends LogEventFactoryTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void common1() throws Exception {

        String commonPattern = "127.0.0.1 - bob [10/Oct/2016:13:55:36 -0700] \"GET /test.gif HTTP/1.1\" 200 2326";

        HttpdLogEventFactory factory = new HttpdLogEventFactory(HttpdLogFormat.COMMON);

        HttpdLogEvent le = factory.parse(commonPattern);
        assertEquals("127.0.0.1", le.getRemoteHost());
        assertNull(le.getRemoteLogname());
        assertEquals("bob", le.getRemoteUser());
        assertEquals(TestDate.create("10/10/16 13:55:36 -0700"), le.getTimestamp());
        assertEquals("", le.getRequestLine());
        assertEquals(200, le.getStatusCode());
        assertEquals(2326, le.getResponseEntityBodySize());
    }

    @Test
    public void common2() throws Exception {

        String commonPattern = "172.20.2.41 - - [09/Jan/2016:20:06:07 -0800] \"OPTIONS * HTTP/1.0\" 200 -";
    }

    @Test
    public void common3() throws Exception {

        String commonPattern = "172.20.2.42 - - [11/Jan/2016:12:22:23 -0800] \"INFO / HTTP/1.1\" 403 3985";
    }

    @Test
    public void logLinesLongerThanThePatternAreOK() throws Exception {

        String line = "127.0.0.1 bob 200 2326";

        HttpdLogFormat format = new HttpdLogFormat(HttpdFormatElement.REMOTE_HOST);
        HttpdLogEventFactory factory = new HttpdLogEventFactory(format);

        HttpdLogEvent le = factory.parse(line);

        assertEquals(1, le.getValueCount());
        assertEquals("127.0.0.1", le.getRemoteHost());
        assertNull(le.getTimestamp());
        assertNull(le.getRemoteUser());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * @see LogEventFactoryTest#getLogEventFactoryToTest()
     */
    @Override
    protected HttpdLogEventFactory getLogEventFactoryToTest()  {

        throw new RuntimeException("getLogFormatToTest() NOT YET IMPLEMENTED");
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
