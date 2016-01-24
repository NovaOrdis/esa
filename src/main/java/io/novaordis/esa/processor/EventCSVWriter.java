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

package io.novaordis.esa.processor;

import io.novaordis.esa.csv.CsvWriter;
import io.novaordis.esa.event.OldEvent;
import io.novaordis.esa.event.OldEventImpl;
import io.novaordis.esa.event.special.EndOfStreamOldEvent;
import io.novaordis.esa.logs.httpd.LogLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/23/16
 */
public class EventCSVWriter implements EventOldLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(EventCSVWriter.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private CsvWriter csvWriter;

    // Constructors ----------------------------------------------------------------------------------------------------

    public EventCSVWriter() {

        this.csvWriter = new CsvWriter();
        this.csvWriter.setOutputStream(System.out);
    }

    // EventLogic ------------------------------------------------------------------------------------------------------

    @Override
    public List<OldEvent> process(OldEvent inputEvent) {
        //
        // we only process EventImpls and EndOfStreamEvents, we warn for everything else
        //

        if (inputEvent instanceof EndOfStreamOldEvent) {

            return Collections.singletonList(inputEvent);
        }
        else if (inputEvent instanceof OldEventImpl) {

            LogLine le = (LogLine)inputEvent.getProperty(0).getValue();

            try {
                csvWriter.process(le);
            }
            catch (Exception e) {
                log.error("failed", e);
            }
            return Collections.emptyList();
        }
        else {
            log.warn("unknown event type " + inputEvent + ", ignoring ...");
            return Collections.emptyList();
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
