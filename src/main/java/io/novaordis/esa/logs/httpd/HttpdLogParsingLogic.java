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

import io.novaordis.esa.core.ClosedException;
import io.novaordis.esa.core.ProcessingLogic;
import io.novaordis.esa.core.event.EndOfStreamEvent;
import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.FaultEvent;
import io.novaordis.esa.core.event.StringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class HttpdLogParsingLogic implements ProcessingLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(HttpdLogParsingLogic.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean closed;
    private List<Event> buffer;
    private HttpdLogParser logParser;

    // Constructors ----------------------------------------------------------------------------------------------------

    public HttpdLogParsingLogic(HttpdLogFormat httpdLogFormat) {

        this.buffer = new ArrayList<>();
        this.logParser = new HttpdLogParser(httpdLogFormat);
    }

    // ProcessingLogic implements --------------------------------------------------------------------------------------

    @Override
    public boolean process(Event inputEvent) throws ClosedException {

        if (closed) {
            throw new ClosedException(this + " is closed");
        }

        if (inputEvent instanceof EndOfStreamEvent) {
            closed = true;
            return false;
        }
        else if (!(inputEvent instanceof StringEvent)) {

            throw new IllegalArgumentException(this + " can only handle StringEvents and it got " + inputEvent);
        }

        //noinspection ConstantConditions
        String s = ((StringEvent)inputEvent).get();

        try {

            HttpdLogLine logLine = logParser.parse(s);
            Event event = HttpdLogLine.toEvent(logLine);
            buffer.add(event);
        }
        catch (Exception e) {

            log.error("parsing failed", e);
            buffer.add(new FaultEvent());
        }

        return true;
    }

    @Override
    public List<Event> getEvents() {

        List<Event> result = new ArrayList<>(buffer);
        buffer.clear();
        return result;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public HttpdLogFormat getHttpdLogFormat() {

        if (logParser == null) {
            return null;
        }

        return logParser.getLogFormat();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
