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

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.ShutdownEvent;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricDefinitionParser;
import io.novaordis.events.csv.CSVFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

/**
 * Instances of this class convert events into comma-separated lines containing event's properties values.
 *
 * If an output format (a list of property names) is provided externally, the formatter will only include the specified
 * properties in the output. Otherwise, all the properties are introspected and included in the output.
 *
 * The class contains support for generating headers. A header line is generated when the first event is received and
 * inserted *before* the first event representation. The header starts with a "#" and it contains comma-separated
 * event's properties names. Also see:
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class ToCSV implements OutputStreamConversionLogic {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ToCSV.class);

    private static final boolean debug = log.isDebugEnabled();

    // MM/dd/yy HH:mm:ss (07/25/16 14:00:00) is the default time format so it works straight away with Excel

    public static final DateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static final String NULL_EXTERNALIZATION = "";

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @return a header. The line starts with "#", then it lists a "timestamp" field and comma-separated property names.
     *
     * We first attempt to resolve the property names by matching the property name to known metric definition IDs.
     * If a known metric definition whose ID matches the property name is identified, we use that metric definition
     * label, instead of teh property name.
     *
     * TODO: should we attempt to parse the property name every time, or we should introduce a "metric repository"?
     *
     * @param outputFormat See ToCSV#setOutputFormat(String).
     */
    public static String outputFormatToHeader(String outputFormat) {

        String headerLine = "# ";

        for(StringTokenizer st = new StringTokenizer(outputFormat, ","); st.hasMoreTokens(); ) {

            String fieldHeader;

            String propertyName = st.nextToken().trim();

            //
            // attempt to identify a known metric definition
            //

            try {

                MetricDefinition md = MetricDefinitionParser.parse(propertyName);
                fieldHeader = md.getLabel();
            }
            catch (Exception e) {

                //
                // that's fine, no known metric definition, use the property name as provided
                //

                fieldHeader = propertyName;
            }

            headerLine += fieldHeader;

            if (st.hasMoreTokens()) {

                headerLine += ", ";
            }
        }

        return headerLine;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private StringBuilder sb;

    private volatile boolean closed;

    private CSVFormatter csvFormatter;

    // Constructors ----------------------------------------------------------------------------------------------------

    public ToCSV() {

        sb = new StringBuilder();
        csvFormatter = new CSVFormatter();
    }

    // OutputStreamConversionLogic implementation ----------------------------------------------------------------------

    @Override
    public boolean process(Event inputEvent) throws ClosedException {

        if (debug) {

            log.debug("processing " + inputEvent);
        }

        if (closed) {

            throw new ClosedException(this + " closed");
        }

        if (inputEvent instanceof EndOfStreamEvent) {

            closed = true;
            return true; // need to collect the output stream close() information
        }

        if (inputEvent instanceof ShutdownEvent) {

            throw new RuntimeException("ShutdownEvent SUPPORT NOT YET IMPLEMENTED");
        }

        String s = csvFormatter.format(inputEvent);

        if (s == null) {

            return false;
        }
        else {

            sb.append(s);
            return true;
        }
    }

    @Override
    public byte[] getBytes() {

        if (closed) {

            if (sb.length() != 0) {

                // there are previously uncollected bytes
                byte[] result = sb.toString().getBytes();
                sb.setLength(0);
                return result;
            }
            else {
                return null;
            }
        }

        if (sb.length() == 0) {
            return EMPTY_BYTE_ARRAY;
        }

        byte[] result = sb.toString().getBytes();
        sb.setLength(0);
        return result;
    }

    @Override
    public boolean isClosed() {

        return closed;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public CSVFormatter getCSVFormatter() {

        return csvFormatter;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

    // Constants -------------------------------------------------------------------------------------------------------



}
