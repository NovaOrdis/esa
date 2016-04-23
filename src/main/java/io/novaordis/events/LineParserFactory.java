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

package io.novaordis.events;

import io.novaordis.clad.UserErrorException;
import io.novaordis.events.core.LineParser;
import io.novaordis.events.csv.CsvLineParser;
import io.novaordis.events.csv.InvalidFieldException;
import io.novaordis.events.httpd.HttpdLineParser;
import io.novaordis.events.httpd.InvalidFormatStringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class LineParserFactory {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(LineParserFactory.class);

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * Gets a line format and applies heuristics to figure out what kind of LineParser would accept such format, by
     * querying the known line parsers.
     *
     * @return null if no known parser exists.
     *
     * @exception UserErrorException if the factory recognizes a parser of a certain type but
     * the format is broken.
     */
    public static LineParser getInstance(String lineFormat) throws UserErrorException {

        if (lineFormat == null) {
            throw new IllegalArgumentException("null line format");
        }

        //
        // visit known LineParsers and asks them if they accept the format
        //

        try {
            return new HttpdLineParser(lineFormat);
        }
        catch(InvalidFormatStringException ifse) {

            throw new UserErrorException("invalid httpd line format: " + ifse.getMessage());
        }
        catch(Exception e) {
            //
            // this is fine, the HttpdLineParser does not recognize the format, go to the next one
            //
        }

        try {
            return new CsvLineParser(lineFormat);
        }
        catch(InvalidFieldException ife) {

            throw new UserErrorException("invalid CSV line format: " + ife.getMessage());
        }
        catch(Exception e) {
            //
            // this is fine, the HttpdLineParser does not recognize the format, go to the next one
            //
        }

        log.debug(LineParserFactory.class.getSimpleName() + "" +
                " could not find a suitable line parser for line format \"" + lineFormat + "\"");

        return null;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private LineParserFactory() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
