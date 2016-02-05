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

import io.novaordis.esa.core.event.IntegerProperty;
import io.novaordis.esa.core.event.MapProperty;
import io.novaordis.esa.core.event.StringProperty;
import io.novaordis.esa.httpd.HttpEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/3/16
 */
public class DescribeCommandTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DescribeCommandTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void getSignature_String_Yaml() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        httpEvent.setProperty(new StringProperty("name1", "value1"));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML);

        log.info(s);

        assertEquals("HttpEvent\n  timestamp\n  name1(String)\n", s);
    }

    @Test
    public void getSignature_String_YamlInline() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        httpEvent.setProperty(new StringProperty("name1", "value1"));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML_INLINE);

        log.info(s);

        assertEquals("HttpEvent[timestamp, name1(String)]", s);
    }

    @Test
    public void getSignature_String_Integer_Yaml() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        httpEvent.setProperty(new StringProperty("name1", "value1"));
        httpEvent.setProperty(new IntegerProperty("name2", 2));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML);

        log.info(s);

        assertEquals("HttpEvent\n  timestamp\n  name1(String)\n  name2(Integer)\n", s);
    }

    @Test
    public void getSignature_String_Integer_YamlInline() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        httpEvent.setProperty(new StringProperty("name1", "value1"));
        httpEvent.setProperty(new IntegerProperty("name2", 2));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML_INLINE);

        log.info(s);

        assertEquals("HttpEvent[timestamp, name1(String), name2(Integer)]", s);
    }

    @Test
    public void getSignature_Map_NoElements_Yaml() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        httpEvent.setProperty(new MapProperty("name1", new HashMap()));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML);

        log.info(s);

        assertEquals("HttpEvent\n  timestamp\n  name1(Map)\n    <empty>\n", s);
    }

    @Test
    public void getSignature_Map_NoElements_YamlInline() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        httpEvent.setProperty(new MapProperty("name1", new HashMap()));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML_INLINE);

        log.info(s);

        assertEquals("HttpEvent[timestamp, name1(Map){}]", s);
    }

    @Test
    public void getSignature_Map_HasElements_Yaml() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        Map<String, Object> map = new HashMap<>();
        map.put("x", "val-x");
        map.put("a", "val-a");
        httpEvent.setProperty(new MapProperty("name1", map));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML);

        log.info(s);

        assertEquals("HttpEvent\n  timestamp\n  name1(Map)\n    a\n    x\n", s);
    }

    @Test
    public void getSignature_Map_HasElements_YamlInline() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        Map<String, Object> map = new HashMap<>();
        map.put("x", "val-x");
        map.put("a", "val-a");
        httpEvent.setProperty(new MapProperty("name1", map));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML_INLINE);

        log.info(s);

        assertEquals("HttpEvent[timestamp, name1(Map){a, x}]", s);
    }

    @Test
    public void getSignature_Map_HasElements_PropertyFollows_Yaml() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        Map<String, Object> map = new HashMap<>();
        map.put("x", "val-x");
        map.put("a", "val-a");
        httpEvent.setProperty(new MapProperty("name1", map));
        httpEvent.setProperty(new StringProperty("name2", "value2"));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML);

        log.info(s);

        assertEquals("HttpEvent\n  timestamp\n  name1(Map)\n    a\n    x\n  name2(String)\n", s);
    }

    @Test
    public void getSignature_Map_HasElements_PropertyFollows_YamlInline() throws Exception {

        HttpEvent httpEvent = new HttpEvent(1L);

        Map<String, Object> map = new HashMap<>();
        map.put("x", "val-x");
        map.put("a", "val-a");
        httpEvent.setProperty(new MapProperty("name1", map));
        httpEvent.setProperty(new StringProperty("name2", "value2"));

        String s = DescribeCommand.getSignature(httpEvent, DescribeCommand.YAML_INLINE);

        log.info(s);

        assertEquals("HttpEvent[timestamp, name1(Map){a, x}, name2(String)]", s);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
