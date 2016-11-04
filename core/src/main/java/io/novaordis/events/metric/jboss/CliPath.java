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

package io.novaordis.events.metric.jboss;

import io.novaordis.events.metric.MetricDefinitionException;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/31/16
 */
class CliPath {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String path;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param literal - expected format /a=b/c=d/
     */
    public CliPath(String literal) throws MetricDefinitionException {

        this.path = literal;

        //
        // get rid of trailing slashes
        //

        int i = this.path.length() - 1;

        for(; i >= 0; i --) {

            if (this.path.charAt(i) != '/') {
                break;
            }
        }

        this.path = this.path.substring(0, i + 1);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path == null ? "null" : path;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
