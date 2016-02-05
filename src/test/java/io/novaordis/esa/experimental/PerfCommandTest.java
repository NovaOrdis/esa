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

package io.novaordis.esa.experimental;

import io.novaordis.esa.core.event.Event;
import io.novaordis.esa.core.event.LongProperty;
import io.novaordis.esa.core.event.MapProperty;
import io.novaordis.esa.core.event.StringProperty;
import io.novaordis.esa.httpd.HttpEvent;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class PerfCommandTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Static Private --------------------------------------------------------------------------------------------------

    private static void setJSessionIDCookie(HttpEvent event, String cookie) {

        if (cookie == null) {
            return;
        }

        MapProperty mp = event.getMapProperty(HttpEvent.COOKIES);

        if (mp == null) {
            mp = new MapProperty(HttpEvent.COOKIES);
            event.setProperty(mp);
        }

        mp.getMap().put(PerfCommand.JSESSIONID, cookie);
    }

    private static void setMarkerRequestHeader(HttpEvent event, String marker) {

        if (marker == null) {
            return;
        }

        MapProperty mp = event.getMapProperty(HttpEvent.REQUEST_HEADERS);

        if (mp == null) {
            mp = new MapProperty(HttpEvent.REQUEST_HEADERS);
            event.setProperty(mp);
        }

        mp.getMap().put(PerfCommand.MARKER_REQUEST_HEADER_NAME, marker);
    }

    private static HttpEvent buildEvent(
            long timestamp, String jSessionIDCookie, String markerHeader, String path, long processingTime) {
        HttpEvent e = new HttpEvent(timestamp);
        setJSessionIDCookie(e, jSessionIDCookie);
        setMarkerRequestHeader(e, markerHeader);
        e.setProperty(new StringProperty(HttpEvent.PATH, path));
        e.setProperty(new LongProperty(HttpEvent.REQUEST_PROCESSING_TIME, processingTime));
        return e;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void collectPerfStats() throws Exception {

        PerfCommand c = new PerfCommand();

        //
        // this will be ignored, as it does not carry any cookie
        //
        c.collectBusinessScenarioStatistics(buildEvent(1L, null, "start", "/test/A", 1L));

        c.collectBusinessScenarioStatistics(buildEvent(1L, "---001---", "start", "/test/A", 1L));
        c.collectBusinessScenarioStatistics(buildEvent(2L, "---002---", "start", "/test/A", 10L));
        c.collectBusinessScenarioStatistics(buildEvent(3L, "---001---", null, "/test/B", 2L));
        c.collectBusinessScenarioStatistics(buildEvent(4L, "---002---", null, "/test/B", 20L));
        c.collectBusinessScenarioStatistics(buildEvent(5L, "---001---", "stop", "/test/C", 3L));
        c.collectBusinessScenarioStatistics(buildEvent(6L, "---002---", "stop", "/test/C", 30L));

        BlockingQueue<Event> outputQueue = c.getOutputQueue();
        assertEquals(2, outputQueue.size());

        BusinessScenarioEvent bs = (BusinessScenarioEvent)outputQueue.take();
        BusinessScenarioEvent bs2 = (BusinessScenarioEvent)outputQueue.take();

        assertEquals(6L, bs.getLongProperty(BusinessScenarioEvent.TOTAL_PROCESSING_TIME).getLong().longValue());
        assertEquals(3, bs.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT).getInteger().intValue());

        assertEquals(60L, bs2.getLongProperty(BusinessScenarioEvent.TOTAL_PROCESSING_TIME).getLong().longValue());
        assertEquals(3, bs2.getIntegerProperty(BusinessScenarioEvent.REQUEST_COUNT).getInteger().intValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
