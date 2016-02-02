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

import io.novaordis.esa.core.ProcessingLogicTest;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.FaultEvent;
import io.novaordis.esa.core.event.MockEvent;
import io.novaordis.esa.core.event.StringEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class HttpdLogParsingLogicTest extends ProcessingLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLogParsingLogicTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    public static final HttpdLogFormat TEST_HTTPD_LOG_FORMAT = HttpdLogFormat.COMMON;

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void notAStringEvent() throws Exception {

        HttpdLogParsingLogic logic = getProcessingLogicToTest();

        assertTrue(logic.process(new MockEvent()));

        List<Event> outputEvents = logic.getEvents();

        assertEquals(1, outputEvents.size());

        FaultEvent e = (FaultEvent)outputEvents.get(0);

        log.info(e.getMessage());
    }

    @Test
    public void notAHttpdLogLine() throws Exception {

        HttpdLogParsingLogic logic = getProcessingLogicToTest();

        String line = "definitely not a httpd log line";

        assertTrue(logic.process(new StringEvent(line)));

        List<Event> outputEvents = logic.getEvents();

        assertEquals(1, outputEvents.size());

        FaultEvent e = (FaultEvent)outputEvents.get(0);

        log.info(e.getMessage());
    }

    @Test
    public void format() throws Exception {

        HttpdLogParsingLogic logic = getProcessingLogicToTest();

        HttpdLogFormat format = logic.getHttpdLogFormat();

        assertEquals(TEST_HTTPD_LOG_FORMAT.toString(), format.toString());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected HttpdLogParsingLogic getProcessingLogicToTest() throws Exception {

        String httpdLogFormatAsString = TEST_HTTPD_LOG_FORMAT.toString();
        return new HttpdLogParsingLogic(new HttpdLogFormat(httpdLogFormatAsString));
    }

    @Override
    protected Event getInputEventRelevantToProcessingLogic() throws Exception {

        // common format
        return new StringEvent("test-host test-remote-logname test-remote-user [31/Jan/2016:06:59:53 -0800] \"GET /test HTTP/1.1\" 200 1024");
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
