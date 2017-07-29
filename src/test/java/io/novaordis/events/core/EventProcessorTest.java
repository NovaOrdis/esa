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

import io.novaordis.events.api.event.Event;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public class EventProcessorTest extends ComponentTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(EventProcessorTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        BlockingQueue<Event> inputQueue = new ArrayBlockingQueue<>(1);
        ProcessingLogic mpl = new MockProcessingLogic();
        BlockingQueue<Event> outputQueue = new ArrayBlockingQueue<>(1);

        EventProcessor eventProcessor = new EventProcessor("test", inputQueue, mpl, outputQueue);

        assertEquals("test", eventProcessor.getName());
        assertEquals(inputQueue, eventProcessor.getInputQueue());
        assertEquals(mpl, eventProcessor.getProcessingLogic());
        assertEquals(outputQueue, eventProcessor.getOutputQueue());
    }

    @Test
    public void toStringWithNoName() {

        EventProcessor eventProcessor = new EventProcessor();

        String s = eventProcessor.toString();

        log.info(s);

        assertTrue(s.matches("EventProcessor\\[.*\\]"));
    }

    @Test
    public void inputQueue() throws Exception {

        EventProcessor eventProcessor = getComponentToTest("test");

        assertNull(eventProcessor.getInputQueue());

        ArrayBlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);

        eventProcessor.setInputQueue(queue);

        assertEquals(queue, eventProcessor.getInputQueue());
    }

    @Test
    public void processingLogic() throws Exception {

        EventProcessor eventProcessor = getComponentToTest("test");

        assertNull(eventProcessor.getProcessingLogic());

        MockProcessingLogic mockProcessingLogic = new MockProcessingLogic();

        eventProcessor.setProcessingLogic(mockProcessingLogic);

        assertNull(eventProcessor.getInputQueue());

        ArrayBlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);

        eventProcessor.setInputQueue(queue);

        assertEquals(queue, eventProcessor.getInputQueue());
    }

    @Test
    public void outputQueue() throws Exception {

        EventProcessor eventProcessor = getComponentToTest("test");

        assertNull(eventProcessor.getOutputQueue());

        ArrayBlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);

        eventProcessor.setOutputQueue(queue);

        assertEquals(queue, eventProcessor.getOutputQueue());
    }

    @Test
    public void insureReadyForStart() throws Exception {

        EventProcessor eventProcessor = getComponentToTest("test");

        try {
            eventProcessor.insureReadyForStart();
            fail("should throw exception, processor not ready for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        eventProcessor.setInputQueue(new ArrayBlockingQueue<>(1));

        try {
            eventProcessor.insureReadyForStart();
            fail("should throw exception, processor not ready for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        eventProcessor.setProcessingLogic(new MockProcessingLogic());

        try {
            eventProcessor.insureReadyForStart();
            fail("should throw exception, processor not ready for start");
        }
        catch(IllegalStateException e) {
            log.info(e.getMessage());
        }

        eventProcessor.setOutputQueue(new ArrayBlockingQueue<>(1));

        eventProcessor.insureReadyForStart();

        log.info("ok");
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected EventProcessor getComponentToTest(String name) throws Exception {

        return new EventProcessor(name);
    }

    @Override
    protected void configureForStart(Component c) throws Exception {

        if (!(c instanceof EventProcessor)) {
            throw new Exception("not an EventProcessor");
        }

        EventProcessor eventProcessor = (EventProcessor)c;

        eventProcessor.setInputQueue(new ArrayBlockingQueue<>(1));
        eventProcessor.setProcessingLogic(new MockProcessingLogic());
        eventProcessor.setOutputQueue(new LinkedBlockingQueue<>(1));
    }

    @Override
    protected boolean willTimeoutOnStop() {
        return false;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
