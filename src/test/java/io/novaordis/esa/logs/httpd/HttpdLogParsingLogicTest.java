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
import io.novaordis.esa.core.event.StringEvent;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class HttpdLogParsingLogicTest extends ProcessingLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected HttpdLogParsingLogic getProcessingLogicToTest() throws Exception {

        return new HttpdLogParsingLogic(new HttpdLogFormat(HttpdLogFormat.COMMON.toString()));
    }

    @Override
    protected Event getInputEventRelevantToProcessingLogic() throws Exception {

        // common format
        return new StringEvent("test-host test-remote-logname test-remote-user [31/Jan/2016:06:59:53 -0800] \"GET /test HTTP/1.1\" 200 1024");
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
