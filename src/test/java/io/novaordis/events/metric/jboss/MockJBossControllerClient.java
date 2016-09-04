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

package io.novaordis.events.metric.jboss;

import io.novaordis.jboss.cli.JBossCliException;
import io.novaordis.jboss.cli.JBossControllerClient;
import io.novaordis.jboss.cli.model.JBossControllerAddress;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 9/3/16
 */
public class MockJBossControllerClient implements JBossControllerClient {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean connected;

    private Map<String, Map<String, Object>> values;

    private JBossControllerAddress address;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockJBossControllerClient() {

        this.values = new HashMap<>();

        this.address = new JBossControllerAddress(
                "mock-user", new char[] { 'm', 'o', 'c', 'k'}, "MOCK-HOST", "MOCK-HOST", 7777, "7777");
    }

    // JBossControllerClient implementation ----------------------------------------------------------------------------

    @Override
    public String getHost() {

        return address.getHost();
    }

    @Override
    public int getPort() {

        return address.getPort();
    }

    @Override
    public String getUsername() {

        return address.getUsername();
    }

    @Override
    public char[] getPassword() {
        throw new RuntimeException("getPassword() NOT YET IMPLEMENTED");
    }

    @Override
    public void setControllerAddress(JBossControllerAddress a) {

        this.address = a;
    }

    @Override
    public JBossControllerAddress getControllerAddress() {

        return address;
    }

    @Override
    public void connect() throws JBossCliException {

        if (connected) {

            log.info(this + " already connected");
            return;
        }

        log.info(this + " is connected");
        connected = true;
    }

    @Override
    public void disconnect() {
        throw new RuntimeException("disconnect() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean isConnected() {

        return connected;
    }

    @Override
    public Object getAttributeValue(String path, String attributeName) throws JBossCliException {

        Map<String, Object> attributes = values.get(path);

        if (attributes == null) {
            return null;
        }

        return attributes.get(attributeName);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setAttributeValue(String path, String attributeName, Object value) {


        Map<String, Object> attributes = values.get(path);

        if (attributes == null) {
            attributes = new HashMap<>();
            values.put(path, attributes);
        }

        attributes.put(attributeName, value);
    }

    @Override
    public String toString() {

        return address.getUsername() + ":***@" + address.getHost() + ":" + address.getPort() + "(" +
                (connected ? "connected" : "disconnected") + ")";

    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
