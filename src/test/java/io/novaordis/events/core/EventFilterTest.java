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

import io.novaordis.clad.option.TimestampOption;
import io.novaordis.events.clad.MockConfiguration;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.MockTimedEvent;
import io.novaordis.utilities.timestamp.Timestamp;
import io.novaordis.utilities.timestamp.TimestampImpl;
import io.novaordis.utilities.timestamp.Timestamps;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/5/16
 */
public class EventFilterTest extends ProcessingLogicTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(EventFilterTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // buildInstance() -------------------------------------------------------------------------------------------------

    @Test
    public void buildInstance_NoAppropriateOptions() throws Exception {

        EventFilter e = EventFilter.buildInstance(new MockConfiguration());
        assertNull(e);
        log.debug(".");
    }

    @Test
    public void buildInstance() throws Exception {

        String timestamp = "07/07/16 00:00:00";
        String aDayBefore = "07/06/16 00:00:00";

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption(null, "from", timestamp));
        EventFilter e = EventFilter.buildInstance(mc);
        assertNotNull(e);

        Event me = new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse(aDayBefore).getTime());

        assertNull(e.processInternal(me));
    }

    @Test
    public void buildInstance_From_Relative() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        TimestampOption option = new TimestampOption(null, "from", "11:11:11");
        mc.addGlobalOption(option);

        EventFilter e = EventFilter.buildInstance(mc);
        assertNotNull(e);

        assertFalse(e.isCalibrated());
        Long from = e.getFromTimestampMs();
        assertNull(from);



        //
        // from not calibrated
        //

        //
        // calibrate it by sending a timed event into it
        //

        e.processInternal(new MockTimedEvent(1L));

        assertTrue(e.isCalibrated());

        //noinspection ConstantConditions
        assertNotNull(e.getFromTimestampMs());

        assertNull(e.getToTimestampMs());
    }

    @Test
    public void buildInstance_From_Full() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        TimestampOption option = new TimestampOption(null, "from", "07/01/15 11:11:11");
        mc.addGlobalOption(option);

        EventFilter e = EventFilter.buildInstance(mc);
        assertNotNull(e);

        //
        // the event filter is calibrated
        //

        assertTrue(e.isCalibrated());

        Long from  = e.getFromTimestampMs();
        assertNotNull(from);
        assertEquals(option.getFullFormat().parse("07/01/15 11:11:11").getTime(), from.longValue());

        assertNull(e.getToTimestampMs());
    }

    @Test
    public void buildInstance_To_Relative() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        TimestampOption option = new TimestampOption(null, "to", "12:12:12");
        mc.addGlobalOption(option);

        EventFilter e = EventFilter.buildInstance(mc);
        assertNotNull(e);

        assertFalse(e.isCalibrated());
        Long to = e.getToTimestampMs();
        assertNull(to);

        //
        // from not calibrated
        //

        //
        // calibrate it by sending a timed event into it
        //

        e.processInternal(new MockTimedEvent(1L));

        assertTrue(e.isCalibrated());

        //noinspection ConstantConditions
        assertNotNull(e.getToTimestampMs());

        assertNull(e.getFromTimestampMs());
    }

    @Test
    public void buildInstance_To_Full() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        TimestampOption option = new TimestampOption(null, "to", "07/01/15 12:12:12");
        mc.addGlobalOption(option);

        EventFilter e = EventFilter.buildInstance(mc);
        assertNotNull(e);
        assertTrue(e.isCalibrated());

        //
        // the event filter is calibrated
        //

        Long to = e.getToTimestampMs();
        assertNotNull(to);

        assertEquals(option.getFullFormat().parse("07/01/15 12:12:12").getTime(), to.longValue());

        assertNull(e.getFromTimestampMs());
    }

    //
    // Relative timestamp filter tests ---------------------------------------------------------------------------------
    //

    @Test
    public void relativeTimestamps() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption("from", "01:00:00"));
        mc.addGlobalOption(new TimestampOption("to", "02:00:00"));
        EventFilter e = EventFilter.buildInstance(mc);
        assertNotNull(e);

        Event result;
        MockTimedEvent mte;

        //
        // the first event sent into the filter calibrates the filter, but it does not match the filter
        //

        mte = new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse("01/01/16 00:00:00").getTime());
        result = e.processInternal(mte);
        assertNull(result);

        //
        // the next three events match the filter
        //

        mte = new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse("01/01/16 01:00:00").getTime());
        result = e.processInternal(mte);
        assertEquals(result, mte);

        mte = new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse("01/01/16 01:30:00").getTime());
        result = e.processInternal(mte);
        assertEquals(result, mte);

        mte = new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse("01/01/16 02:00:00").getTime());
        result = e.processInternal(mte);
        assertEquals(result, mte);

        //
        // this event does not match the filter
        //

        mte = new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse("01/01/16 02:00:01").getTime());
        result = e.processInternal(mte);
        assertNull(result);

        Event previousDayEvent =
                new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse("12/31/15 01:30:00").getTime());

        assertNull(e.processInternal(previousDayEvent));

        Event dayAfterEvent =
                new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse("01/02/16 01:30:00").getTime());

        assertNull(e.processInternal(dayAfterEvent));
    }

    @Test
    public void fromFilterAdjustedForTimezone_TimezoneSpecifiedInLog() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption("from", "07/01/15 10:00:00"));
        assertEquals("MM/dd/yy HH:mm:ss", TimestampOption.DEFAULT_FORMAT_AS_STRING);
        EventFilter f = EventFilter.buildInstance(mc);
        assertNotNull(f);

        //
        // the date is parsed in the default time zone so to make sure the test is relevant, use a different
        // time zone
        //

        int ourTimezoneOffsetHours = Timestamps.getDefaultTimezoneHours();
        int logTimezoneOffset = ourTimezoneOffsetHours + 2;

        // will fail if not a valid timezone offset; if it does, it means it's run from a strange timezone
        // so need to adjust the test
        String tzOffset = Timestamps.timezoneOffsetHoursToString(logTimezoneOffset);

        Event result;

        //
        // event does not match
        //

        DateFormat logDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss Z");

        Timestamp ts = new TimestampImpl("07/01/15 09:59:59 " + tzOffset, logDateFormat);
        Event e = new MockTimedEvent(ts);
        result = f.processInternal(e);
        assertNull(result);


        //
        // events match
        //

        Timestamp ts2 = new TimestampImpl("07/01/15 10:00:00 " + tzOffset, logDateFormat);
        Event e2 = new MockTimedEvent(ts2);
        result = f.processInternal(e2);
        assertEquals(e2, result);

        Timestamp ts3 = new TimestampImpl("07/01/15 10:00:01 " + tzOffset, logDateFormat);
        Event e3 = new MockTimedEvent(ts3);
        result = f.processInternal(e3);
        assertEquals(e3, result);
    }

    @Test
    public void fromFilterAdjustedForTimezone_TimezoneNotSpecifiedInLog() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption("from", "07/01/15 10:00:00"));
        assertEquals("MM/dd/yy HH:mm:ss", TimestampOption.DEFAULT_FORMAT_AS_STRING);
        EventFilter f = EventFilter.buildInstance(mc);
        assertNotNull(f);

        Event result;

        //
        // event does not match
        //

        DateFormat logDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

        Timestamp logTimestamp = new TimestampImpl("07/01/15 09:59:59", logDateFormat);
        Event e = new MockTimedEvent(logTimestamp);
        result = f.processInternal(e);
        assertNull(result);


        //
        // events match
        //

        Timestamp ts2 = new TimestampImpl("07/01/15 10:00:00", logDateFormat);
        Event e2 = new MockTimedEvent(ts2);
        result = f.processInternal(e2);
        assertEquals(e2, result);

        Timestamp ts3 = new TimestampImpl("07/01/15 10:00:01", logDateFormat);
        Event e3 = new MockTimedEvent(ts3);
        result = f.processInternal(e3);
        assertEquals(e3, result);
    }

    @Test
    public void fromFilterAdjustedForTimezone_TimezoneSpecifiedInLog_RelativeFormat() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption("from", "10:00:00"));
        // the relative format is extracted from the full format
        assertEquals("MM/dd/yy HH:mm:ss", TimestampOption.DEFAULT_FORMAT_AS_STRING);
        EventFilter f = EventFilter.buildInstance(mc);
        assertNotNull(f);

        //
        // the date is parsed in the default time zone so to make sure the test is relevant, use a different
        // time zone
        //

        int ourTimezoneOffsetHours = Timestamps.getDefaultTimezoneHours();
        int logTimezoneOffset = ourTimezoneOffsetHours + 2;

        // will fail if not a valid timezone offset; if it does, it means it's run from a strange timezone
        // so need to adjust the test
        String tzOffset = Timestamps.timezoneOffsetHoursToString(logTimezoneOffset);

        Event result;

        //
        // event does not match
        //

        DateFormat logDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss Z");

        String timestampString = "07/01/15 09:59:59 " + tzOffset;
        Timestamp ts = new TimestampImpl(timestampString, logDateFormat);
        Event e = new MockTimedEvent(ts);
        result = f.processInternal(e);
        assertNull(result);


        //
        // events match
        //

        String timestampString2 = "07/01/15 10:00:00 " + tzOffset;
        Timestamp ts2 = new TimestampImpl(timestampString2, logDateFormat);
        Event e2 = new MockTimedEvent(ts2);
        result = f.processInternal(e2);
        assertEquals(e2, result);

        String timestampString3 = "07/01/15 10:00:01 " + tzOffset;
        Timestamp ts3 = new TimestampImpl(timestampString3, logDateFormat);
        Event e3 = new MockTimedEvent(ts3);
        result = f.processInternal(e3);
        assertEquals(e3, result);
    }

    @Test
    public void fromFilterAdjustedForTimezone_TimezoneNotSpecifiedInLog_RelativeFormat() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption("from", "10:00:00"));
        // the relative format is extracted from the full format
        assertEquals("MM/dd/yy HH:mm:ss", TimestampOption.DEFAULT_FORMAT_AS_STRING);
        EventFilter f = EventFilter.buildInstance(mc);
        assertNotNull(f);

        Event result;

        //
        // event does not match
        //

        DateFormat logDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

        Timestamp logTimestamp = new TimestampImpl("07/01/15 09:59:59", logDateFormat);
        Event e = new MockTimedEvent(logTimestamp);
        result = f.processInternal(e);
        assertNull(result);


        //
        // events match
        //

        Timestamp ts2 = new TimestampImpl("07/01/15 10:00:00", logDateFormat);
        Event e2 = new MockTimedEvent(ts2);
        result = f.processInternal(e2);
        assertEquals(e2, result);

        Timestamp ts3 = new TimestampImpl("07/01/15 10:00:01", logDateFormat);
        Event e3 = new MockTimedEvent(ts3);
        result = f.processInternal(e3);
        assertEquals(e3, result);
    }

    @Test
    public void toFilterAdjustedForTimezone_TimezoneSpecifiedInLog() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption("to", "07/01/15 10:00:00"));
        assertEquals("MM/dd/yy HH:mm:ss", TimestampOption.DEFAULT_FORMAT_AS_STRING);
        EventFilter f = EventFilter.buildInstance(mc);
        assertNotNull(f);

        //
        // the date is parsed in the default time zone so to make sure the test is relevant, use a different
        // time zone
        //

        int ourTimezoneOffsetHours = Timestamps.getDefaultTimezoneHours();
        int logTimezoneOffset = ourTimezoneOffsetHours + 2;

        // will fail if not a valid timezone offset; if it does, it means it's run from a strange timezone
        // so need to adjust the test
        String tzOffset = Timestamps.timezoneOffsetHoursToString(logTimezoneOffset);

        Event result;

        //
        // events match
        //

        DateFormat logDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss Z");

        Timestamp ts = new TimestampImpl("07/01/15 09:59:59 " + tzOffset, logDateFormat);
        Event e = new MockTimedEvent(ts);
        result = f.processInternal(e);
        assertEquals(e, result);

        Timestamp ts2 = new TimestampImpl("07/01/15 10:00:00 " + tzOffset, logDateFormat);
        Event e2 = new MockTimedEvent(ts2);
        result = f.processInternal(e2);
        assertEquals(e2, result);

        //
        // event does not match
        //

        Timestamp ts3 = new TimestampImpl("07/01/15 10:00:01 " + tzOffset, logDateFormat);
        Event e3 = new MockTimedEvent(ts3);
        result = f.processInternal(e3);
        assertNull(result);
    }

    @Test
    public void toFilterAdjustedForTimezone_TimezoneNotSpecifiedInLog() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption("to", "07/01/15 10:00:00"));
        assertEquals("MM/dd/yy HH:mm:ss", TimestampOption.DEFAULT_FORMAT_AS_STRING);
        EventFilter f = EventFilter.buildInstance(mc);
        assertNotNull(f);

        Event result;

        //
        // events match
        //

        DateFormat logDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

        Timestamp logTimestamp = new TimestampImpl("07/01/15 09:59:59", logDateFormat);
        Event e = new MockTimedEvent(logTimestamp);
        result = f.processInternal(e);
        assertEquals(e, result);


        Timestamp ts2 = new TimestampImpl("07/01/15 10:00:00", logDateFormat);
        Event e2 = new MockTimedEvent(ts2);
        result = f.processInternal(e2);
        assertEquals(e2, result);

        //
        // event does not match
        //

        Timestamp ts3 = new TimestampImpl("07/01/15 10:00:01", logDateFormat);
        Event e3 = new MockTimedEvent(ts3);
        result = f.processInternal(e3);
        assertNull(result);
    }

    @Test
    public void toFilterAdjustedForTimezone_TimezoneSpecifiedInLog_RelativeFormat() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption("to", "10:00:00"));
        // the relative format is extracted from the full format
        assertEquals("MM/dd/yy HH:mm:ss", TimestampOption.DEFAULT_FORMAT_AS_STRING);
        EventFilter f = EventFilter.buildInstance(mc);
        assertNotNull(f);

        //
        // the date is parsed in the default time zone so to make sure the test is relevant, use a different
        // time zone
        //

        int ourTimezoneOffsetHours = Timestamps.getDefaultTimezoneHours();
        int logTimezoneOffset = ourTimezoneOffsetHours + 2;

        // will fail if not a valid timezone offset; if it does, it means it's run from a strange timezone
        // so need to adjust the test
        String tzOffset = Timestamps.timezoneOffsetHoursToString(logTimezoneOffset);

        Event result;

        //
        // events match
        //

        DateFormat logDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss Z");

        Timestamp ts = new TimestampImpl("07/01/15 09:59:59 " + tzOffset, logDateFormat);
        Event e = new MockTimedEvent(ts);
        result = f.processInternal(e);
        assertEquals(e, result);

        Timestamp ts2 = new TimestampImpl("07/01/15 10:00:00 " + tzOffset, logDateFormat);
        Event e2 = new MockTimedEvent(ts2);
        result = f.processInternal(e2);
        assertEquals(e2, result);

        //
        // event does not match
        //

        Timestamp ts3 = new TimestampImpl("07/01/15 10:00:01 " + tzOffset, logDateFormat);
        Event e3 = new MockTimedEvent(ts3);
        result = f.processInternal(e3);
        assertNull(result);
    }

    @Test
    public void toFilterAdjustedForTimezone_TimezoneNotSpecifiedInLog_RelativeFormat() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption("to", "10:00:00"));
        // the relative format is extracted from the full format
        assertEquals("MM/dd/yy HH:mm:ss", TimestampOption.DEFAULT_FORMAT_AS_STRING);
        EventFilter f = EventFilter.buildInstance(mc);
        assertNotNull(f);

        Event result;

        //
        // events match
        //

        DateFormat logDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

        Timestamp logTimestamp = new TimestampImpl("07/01/15 09:59:59", logDateFormat);
        Event e = new MockTimedEvent(logTimestamp);
        result = f.processInternal(e);
        assertEquals(e, result);


        Timestamp ts2 = new TimestampImpl("07/01/15 10:00:00", logDateFormat);
        Event e2 = new MockTimedEvent(ts2);
        result = f.processInternal(e2);
        assertEquals(e2, result);

        //
        // event does not match
        //

        Timestamp ts3 = new TimestampImpl("07/01/15 10:00:01", logDateFormat);
        Event e3 = new MockTimedEvent(ts3);
        result = f.processInternal(e3);
        assertNull(result);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected EventFilter getProcessingLogicToTest() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.addGlobalOption(new TimestampOption(null, "from", "01/01/01 00:00:00"));
        return EventFilter.buildInstance(mc);
    }

    @Override
    protected Event getInputEventRelevantToProcessingLogic() throws Exception {

        return new MockTimedEvent(TimestampOption.DEFAULT_FULL_FORMAT.parse("01/01/01 00:00:01").getTime());
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
