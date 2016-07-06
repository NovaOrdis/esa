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

package io.novaordis.events.extensions.bscenarios;

import io.novaordis.events.core.CsvOutputFormatterTest;
import io.novaordis.events.core.event.MockEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringTokenizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 5/18/16
 */
public class BusinessScenarioOutputFormatterTest extends CsvOutputFormatterTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BusinessScenarioOutputFormatterTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Overrides -------------------------------------------------------------------------------------------------------

    @Test
    @Override
    public void process_RegularTimedEvent_NoConfiguredOutputFormat() {
        // noop
    }

    @Test
    @Override
    public void process_RegularTimedEvent_WithConfiguredOutputFormat() {
        // noop
    }

    @Test
    @Override
    public void process_RegularUntimedEvent_NoConfiguredOutputFormat() {
        // noop
    }

    @Test
    @Override
    public void process_RegularUntimedEvent_WithConfiguredOutputFormat() {
        // noop
    }

    @Test
    @Override
    public void process_MultipleUncollectedEventsFollowedByEndOfStream() {
        // noop
    }

    @Test
    public void toStringEvent_MapProperty() throws Exception {

        // noop
    }

    // header line overrides -------------------------------------------------------------------------------------------

    @Test
    @Override
    public void outputHeader_OutputFormatSet() throws Exception {

        //
        // noop, irrelevant here
        //
    }

    @Test
    @Override
    public void outputHeader_OutputFormatNotSet() throws Exception {

        //
        // noop, irrelevant here
        //
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void toString_NotABusinessScenarioEvent() throws Exception {

        BusinessScenarioOutputFormatter f = getConversionLogicToTest();

        try {
            //noinspection ResultOfMethodCallIgnored
            f.toString(new MockEvent());
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void toString_Reference() throws Exception {

        BusinessScenarioOutputFormatter f = getConversionLogicToTest();

        BusinessScenario bs = new BusinessScenario();

        long beginTimestamp = 1L;
        bs.setBeginTimestamp(beginTimestamp);
        bs.setId(2L);
        bs.setJSessionId("test-jsession-id");
        bs.setIterationId("test-iteration-id");
        bs.setType("test-type");
        bs.setState(BusinessScenarioState.COMPLETE);

        List<HttpRequestResponsePair> httpRequestResponsePairs = bs.getRequestResponsePairs();

        HttpRequestResponsePair p = new HttpRequestResponsePair();
        p.setStatusCode(200);
        p.setDuration(11L);
        httpRequestResponsePairs.add(p);


        HttpRequestResponsePair p2 = new HttpRequestResponsePair();
        p2.setStatusCode(300);
        p2.setDuration(22L);
        httpRequestResponsePairs.add(p2);


        HttpRequestResponsePair p3 = new HttpRequestResponsePair();
        p3.setStatusCode(400);
        p3.setDuration(33L);
        httpRequestResponsePairs.add(p3);

        BusinessScenarioEvent bse = bs.toEvent();

        String s = f.toString(bse);

        // depending on where the test is run, the hour may be different
        String bts = BusinessScenarioOutputFormatter.TIMESTAMP_FORMAT.format(beginTimestamp);
        assertEquals(bts + ", 2, test-jsession-id, test-iteration-id, test-type, COMPLETE, 3, 1, 66, 11, 22, 33, 200, 300, 400", s);
    }

    @Test
    public void outputHeader() throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yy/dd HH:mm:ss");

        BusinessScenarioOutputFormatter formatter = getConversionLogicToTest();

        BusinessScenarioEvent bse = new BusinessScenarioEvent(dateFormat.parse("12/16/24 16:17:18").getTime());

        assertTrue(formatter.process(bse));

        String output = new String(formatter.getBytes());
        assertTrue(output.startsWith("12/24/16 16:17:18,"));

        //
        // turn on header generation
        //

        formatter.setHeaderOn();
        assertTrue(formatter.isHeaderOn());

        bse = new BusinessScenarioEvent(dateFormat.parse("12/16/24 16:17:19").getTime());

        assertTrue(formatter.process(bse));
        assertFalse(formatter.isHeaderOn());

        output = new String(formatter.getBytes());

        StringTokenizer st = new StringTokenizer(output, "\n");
        String header = st.nextToken();
        assertEquals("Time, Business Scenario ID, JSessionID, Iteration ID, Scenario Type, Scenario State, Requests in Scenario, Successful Requests in Scenario, Scenario Duration (ms)", header);
        String firstLine = st.nextToken();
        assertTrue(firstLine.startsWith("12/24/16 16:17:19,"));

        //
        // make sure the header generation turns off automatically
        //

        bse = new BusinessScenarioEvent(dateFormat.parse("12/16/24 16:17:20").getTime());

        assertTrue(formatter.process(bse));

        output = new String(formatter.getBytes());
        assertTrue(output.startsWith("12/24/16 16:17:20,"));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected BusinessScenarioOutputFormatter getConversionLogicToTest() throws Exception {
        return new BusinessScenarioOutputFormatter();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
