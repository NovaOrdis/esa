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

package io.novaordis.events.clad.command;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.command.CommandBase;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.events.clad.EventsApplicationRuntime;
import io.novaordis.events.core.event.EndOfStreamEvent;
import io.novaordis.events.core.event.Event;
import io.novaordis.events.core.event.MapProperty;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.TimedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/3/16
 */
@SuppressWarnings("unused")
public class DescribeCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Set<String> signatures;

    // Constructors ----------------------------------------------------------------------------------------------------

    public DescribeCommand() {

        signatures = new HashSet<>();
    }

    // CommandBase override --------------------------------------------------------------------------------------------

    @Override
    public void execute(Configuration configuration, ApplicationRuntime r) throws Exception {

        EventsApplicationRuntime runtime = (EventsApplicationRuntime)r;
        runtime.getTerminator().disable();
        runtime.start();

        BlockingQueue<Event> inputQueue = runtime.getLastEventProcessor().getOutputQueue();

        for(;;) {

            Event event = inputQueue.take();
            if (event == null || event instanceof EndOfStreamEvent) {
                break;
            }

            analyze(event);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    public static final boolean YAML = true;
    public static final boolean YAML_INLINE = false;

    /**
     * Build a list of event categories (type, signature) and place them in a tree structure
     * @param event
     */
    void analyze(Event event) throws InterruptedException {

        String signature = getSignature(event, YAML_INLINE);

        if (!signatures.contains(signature)) {

            signatures.add(signature);
            System.out.println(getSignature(event, YAML));
            //terminatorQueue.put(new LineEvent(getSignature(event, YAML)));
        }
    }

    /**
     * @param yamlStandard if true, the output will use standard YAML notation, otherwise will use YAML in-line (
     *                     brackets for lists, braces for Maps) notation.
     * @return
     */
    static String getSignature(Event event, boolean yamlStandard) {

        String signature = "";
        int level = 0;

        Class c = event.getClass();

        signature += c.getSimpleName() + (yamlStandard ? "" : "[");

        level++;

        if (event instanceof TimedEvent) {

            signature += (yamlStandard ? "\n" + indentation(level) : "") + "timestamp";
        }

        Set<Property> properties = event.getProperties();

        if (!properties.isEmpty()) {

            signature += yamlStandard ? "" : ", ";

            List<Property> sortedProperties = new ArrayList<>(properties);
            Collections.sort(sortedProperties);

            for(Iterator<Property> i = sortedProperties.iterator(); i.hasNext(); ) {

                Property p = i.next();
                Class type = p.getType();

                signature += (yamlStandard ? "\n" + indentation(level) : "") +
                        p.getName() + "(" + type.getSimpleName() + ")";

                if (Map.class.equals(type)) {

                    level++;

                    signature += yamlStandard ? "" : "{";
                    List<String> keys = new ArrayList<>();
                    for(Object o: ((MapProperty)p).getMap().keySet()) {
                        keys.add(o.toString());
                    }
                    Collections.sort(keys);

                    if (yamlStandard && keys.isEmpty()) {
                        signature += "\n" + indentation(level) + "<empty>";
                    }

                    for(Iterator<String> j = keys.iterator(); j.hasNext(); ) {

                        signature += (yamlStandard ? "\n" + indentation(level) : "") + j.next();

                        if (j.hasNext()) {
                            signature += (yamlStandard ? "": ", ");
                        }
                    }

                    signature += yamlStandard ? "" : "}";

                    level--;
                }

                if (i.hasNext()) {
                    signature += yamlStandard ? "" : ", ";
                }
            }
        }

        signature += yamlStandard ? "\n" : "]";

        return signature;
    }

    static String indentation(int level) {
        //
        // two spaces per level
        //
        if (level == 0) {
            return "";
        }
        else if (level == 1) {
            return "  ";
        }
        else if (level == 2) {
            return "    ";
        }
        else if (level == 3) {
            return "      ";
        }
        else if (level == 4) {
            return "        ";
        }
        else {
            String s = "";
            for(int i = 0; i < level; i ++) {
                s += "  ";
            }
            return s;
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
