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

import io.novaordis.esa.core.ProcessingLogicBase;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.FaultEvent;
import io.novaordis.esa.core.event.StringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class HttpdLogParsingLogic extends ProcessingLogicBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLogParsingLogic.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private HttpdLogParser logParser;

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpdLogParsingLogic(HttpdLogFormat httpdLogFormat) {

        super();

        this.logParser = new HttpdLogParser(httpdLogFormat);

        log.debug(this + " constructed");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public HttpdLogFormat getHttpdLogFormat() {

        if (logParser == null) {
            return null;
        }

        return logParser.getLogFormat();
    }

    @Override
    public String toString() {

        return "HttpdLogParsingLogic[" + logParser + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected Event processInternal(Event inputEvent) {

        if (!(inputEvent instanceof StringEvent)) {

            return new FaultEvent(this + " can only handle StringEvents and it got " + inputEvent);
        }

        String s = ((StringEvent)inputEvent).get();

        try {

            HttpdLogLine logLine = logParser.parse(s);
            //noinspection UnnecessaryLocalVariable
            HttpEvent event = logLine.toEvent();
            return event;
        }
        catch (Exception e) {

            return new FaultEvent("httpd log line parsing failed: " + e.getMessage());
        }
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
