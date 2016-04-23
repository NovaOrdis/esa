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

import io.novaordis.clad.UserErrorException;
import io.novaordis.clad.option.StringOption;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/29/16
 */
public class EventsApplicationRuntimeTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(EventsApplicationRuntimeTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // getInputFormatSpecification() -----------------------------------------------------------------------------------

    @Test
    public void getInputFormatSpecification_NoInputFormatSpecification() throws Exception {

        MockConfiguration configuration = new MockConfiguration();

        try {

            EventsApplicationRuntime.getInputFormatSpecification(configuration);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("-" + EventsApplicationRuntime.INPUT_FORMAT_OPTION.getShortLiteral()));
            assertTrue(msg.contains("--" + EventsApplicationRuntime.INPUT_FORMAT_OPTION.getLongLiteral()));
            assertTrue(msg.contains("--" + EventsApplicationRuntime.INPUT_FORMAT_FILE_OPTION.getLongLiteral()));
        }
    }

    @Test
    public void getInputFormatSpecification_CommandLineInputFormatSpec() throws Exception {

        MockConfiguration configuration = new MockConfiguration();
        StringOption o = EventsApplicationRuntime.INPUT_FORMAT_OPTION;
        o.setValue("A B C D E");
        configuration.getGlobalOptions().add(o);

        String spec = EventsApplicationRuntime.getInputFormatSpecification(configuration);
        assertEquals("A B C D E", spec);
    }

    @Test
    public void getInputFormatSpecification_FileInputFormatSpec_FileDoesNotExist() throws Exception {

        MockConfiguration configuration = new MockConfiguration();

        StringOption o = EventsApplicationRuntime.INPUT_FORMAT_FILE_OPTION;
        o.setValue("/I/am/pretty/sure/this/file/does/not.exists");
        configuration.getGlobalOptions().add(o);

        try {

            EventsApplicationRuntime.getInputFormatSpecification(configuration);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("/I/am/pretty/sure/this/file/does/not.exists"));
            assertTrue(msg.contains("does not exist"));
        }
    }

    @Test
    public void getInputFormatSpecification_FileInputFormatSpec_FileExists() throws Exception {

        String basedir = System.getProperty("basedir");
        String fileName = basedir + "/src/test/resources/data/input-format-file.txt";
        assertTrue(new File(fileName).isFile());

        MockConfiguration configuration = new MockConfiguration();

        StringOption o = EventsApplicationRuntime.INPUT_FORMAT_FILE_OPTION;
        o.setValue(fileName);
        configuration.getGlobalOptions().add(o);

        String result = EventsApplicationRuntime.getInputFormatSpecification(configuration);
        assertEquals("a b c d", result);
    }

    @Test
    public void getInputFormatSpecification_BothInputFormatAndInputFormatFileSpecified() throws Exception {

        MockConfiguration configuration = new MockConfiguration();

        StringOption o = EventsApplicationRuntime.INPUT_FORMAT_OPTION;
        o.setValue("something");
        configuration.getGlobalOptions().add(o);

        StringOption o2 = EventsApplicationRuntime.INPUT_FORMAT_FILE_OPTION;
        o2.setValue("some-file");
        configuration.getGlobalOptions().add(o2);

        try {
            EventsApplicationRuntime.getInputFormatSpecification(configuration);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
