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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/1/16
 */
public class ListPropertyTest extends PropertyTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ListPropertyTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Overrides

    // externalizeValue() ----------------------------------------------------------------------------------------------

    @Override
    @Test
    public void externalizeValue() throws Exception {

        Property p = getPropertyToTest("test");
        p.setValue(null);

        List l = (List)p.getValue();
        assertTrue(l.isEmpty());

        String s = p.externalizeValue();
        assertEquals("[]", s);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void value() throws Exception {

        ListProperty<Integer> lp = new ListProperty<>("test-name", 1, 2, 3);

        assertEquals("test-name", lp.getName());

        List l = (List)lp.getValue();
        List<Integer> l2 = lp.getList();

        assertEquals(l, l2);

        assertEquals(3, l.size());

        assertEquals(1, l.get(0));
        assertEquals(2, l.get(1));
        assertEquals(3, l.get(2));

        assertEquals(List.class, lp.getType());
    }

    @Test
    public void fromString() throws Exception {

        //
        // noop - fromString() currently not implemented for ListProperty
        //
    }

    @Test
    public void externalizeValue_ListProperty_Integer() throws Exception {

        ListProperty<Integer> lp = new ListProperty<>("test-name", 1, 2, 3);

        String externalizedValue = lp.externalizeValue();

        log.info("externalized value: " + externalizedValue);

        assertEquals("[1, 2, 3]", externalizedValue);
    }

    @Test
    public void externalizeValue_ListProperty_String() throws Exception {

        ListProperty<String> lp = new ListProperty<>("test-name", "A", "B", "C");

        String externalizedValue = lp.externalizeValue();

        log.info("externalized value: " + externalizedValue);

        assertEquals("[\"A\", \"B\", \"C\"]", externalizedValue);
    }

    @Test
    public void externalizeType_ListProperty() throws Exception {

        ListProperty<Integer> lp = new ListProperty<>("test-name", 1);
        assertEquals("test-name", lp.externalizeType());
    }

    // getList() -------------------------------------------------------------------------------------------------------

    @Test
    public void getList_emptyListNeverNull() throws Exception {

        ListProperty<Integer> lp = new ListProperty<>("test-name");

        List<Integer> l = lp.getList();
        assertNotNull(l);
        assertTrue(l.isEmpty());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected ListProperty getPropertyToTest(String name) {

        return new ListProperty<>(name, 1, 2, 3);
    }

    @Override
    protected List<Integer> getAppropriateValueForPropertyToTest() {

        List<Integer> result = new ArrayList<>();

        result.add(1);
        result.add(2);
        result.add(3);

        return result;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
