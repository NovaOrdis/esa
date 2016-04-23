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

package io.novaordis.events.clad;

import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/29/16
 */
public class MockConfiguration implements Configuration {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<Option> globalOptions;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockConfiguration() {

        this.globalOptions = new ArrayList<>();
    }

    // Configuration implementation ------------------------------------------------------------------------------------

    @Override
    public String getApplicationName() {
        throw new RuntimeException("getApplicationName() NOT YET IMPLEMENTED");
    }

    @Override
    public List<Option> getGlobalOptions() {
        return globalOptions;
    }

    @Override
    public Option getGlobalOption(Option definition) {

        for(Option o: globalOptions) {
            if (o.equals(definition)) {
                return o;
            }
        }

        return null;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
