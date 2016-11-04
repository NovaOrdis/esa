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

package io.novaordis.events.core.event;

import io.novaordis.utilities.time.Timestamp;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/6/16
 */
public class GenericTimedEventTest extends TimedEventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_PropertyList() throws Exception {

        List<Property> input = new ArrayList<>();
        input.add(new IntegerProperty("test1", 1));
        input.add(new StringProperty("test2", "2"));

        GenericTimedEvent gte = new GenericTimedEvent(input);

        assertNull(gte.getTime());
        assertNull(gte.getTimestamp());

        List<Property> result = gte.getPropertyList();

        assertEquals(input.size(), result.size());

        for(int i = 0; i < input.size(); i ++) {

            Property ip = input.get(i);
            Property op = result.get(i);

            assertEquals(ip.getName(), op.getName());
            assertEquals(ip.getValue(), op.getValue());
        }
    }

    @Test
    public void constructor_PropertyList2() throws Exception {

        List<Property> input = new ArrayList<>();
        input.add(new IntegerProperty("test1", 1));
        input.add(new StringProperty("test2", "2"));

        long t = 7L;

        GenericTimedEvent gte = new GenericTimedEvent(t, input);

        assertEquals(t, gte.getTime().longValue());

        Timestamp ts = gte.getTimestamp();
        assertEquals(t, ts.getTime());

        List<Property> result = gte.getPropertyList();

        assertEquals(input.size(), result.size());

        for(int i = 0; i < input.size(); i ++) {

            Property ip = input.get(i);
            Property op = result.get(i);

            assertEquals(ip.getName(), op.getName());
            assertEquals(ip.getValue(), op.getValue());
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected GenericTimedEvent getEventToTest() throws Exception {
        return new GenericTimedEvent();
    }

    @Override
    protected GenericTimedEvent getEventToTest(Long timestamp) throws Exception {
        return new GenericTimedEvent(timestamp);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
