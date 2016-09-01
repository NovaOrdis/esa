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

import io.novaordis.events.core.event.MeasureUnit;
import io.novaordis.events.metric.MetricDefinitionBase;
import io.novaordis.events.metric.MetricDefinitionException;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/31/16
 */
public class JBossCliMetricDefinition extends MetricDefinitionBase {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String PREFIX = "jboss:";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @throws MetricDefinitionException in case an invalid metric definition is encountered. The error message
     * must be human-readable, as it will most likely end up in error messages.
     * @throws IllegalArgumentException
     */
    public JBossCliMetricDefinition(String definition) throws MetricDefinitionException {

        if (definition == null) {

            throw new IllegalArgumentException("null jboss CLI metric definition");
        }

        if (!definition.startsWith(PREFIX)) {

            throw new IllegalArgumentException("invalid jboss CLI metric, no prefix: \"" + definition + "\"");
        }

        parse(definition.substring(PREFIX.length()));
    }

    // MetricDefinitionBase overrides ----------------------------------------------------------------------------------

    @Override
    public String getSimpleLabel() {
        throw new RuntimeException("getSimpleLabel() NOT YET IMPLEMENTED");
    }

    @Override
    public MeasureUnit getMeasureUnit() {
        throw new RuntimeException("getMeasureUnit() NOT YET IMPLEMENTED");
    }

    @Override
    public String getDescription() {
        throw new RuntimeException("getDescription() NOT YET IMPLEMENTED");
    }

    @Override
    public Class getType() {
        throw new RuntimeException("getType() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    /**
     * @param definition - without prefix
     */
    private void parse(String definition) throws MetricDefinitionException {
        throw new RuntimeException("NYE");
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
