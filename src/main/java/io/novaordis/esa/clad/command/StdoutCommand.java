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

package io.novaordis.esa.clad.command;

import io.novaordis.clad.ApplicationRuntime;
import io.novaordis.clad.Configuration;
import io.novaordis.clad.command.CommandBase;
import io.novaordis.esa.clad.EventsApplicationRuntime;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/28/16
 */
// will be instantiated via reflection
@SuppressWarnings("unused")
public class StdoutCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Command implementation ------------------------------------------------------------------------------------------

    @Override
    public void execute(Configuration configuration, ApplicationRuntime ar) throws Exception {

        //
        // everything is already in place, just connect the head of the pipeline to the terminator and start the
        // component
        //

        EventsApplicationRuntime runtime = (EventsApplicationRuntime)ar;
        runtime.connectToTerminator(runtime.getOutputQueue());
        runtime.start();
        runtime.waitForEndOfStream();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
