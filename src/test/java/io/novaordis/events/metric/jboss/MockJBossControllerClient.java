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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 9/3/16
 */
public class MockJBossControllerClient implements JBossControllerClient {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // JBossControllerClient implementation ----------------------------------------------------------------------------

    @Override
    public void setHost(String s) {
        throw new RuntimeException("setHost() NOT YET IMPLEMENTED");
    }

    @Override
    public String getHost() {
        throw new RuntimeException("getHost() NOT YET IMPLEMENTED");
    }

    @Override
    public void setPort(int i) {
        throw new RuntimeException("setPort() NOT YET IMPLEMENTED");
    }

    @Override
    public int getPort() {
        throw new RuntimeException("getPort() NOT YET IMPLEMENTED");
    }

    @Override
    public void setUsername(String s) {
        throw new RuntimeException("setUsername() NOT YET IMPLEMENTED");
    }

    @Override
    public String getUsername() {
        throw new RuntimeException("getUsername() NOT YET IMPLEMENTED");
    }

    @Override
    public void setPassword(char[] chars) {
        throw new RuntimeException("setPassword() NOT YET IMPLEMENTED");
    }

    @Override
    public char[] getPassword() {
        throw new RuntimeException("getPassword() NOT YET IMPLEMENTED");
    }

    @Override
    public void connect() throws JBossCliException {
        throw new RuntimeException("connect() NOT YET IMPLEMENTED");
    }

    @Override
    public void disconnect() {
        throw new RuntimeException("disconnect() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean isConnected() {
        throw new RuntimeException("isConnected() NOT YET IMPLEMENTED");
    }

    @Override
    public String getAttributeValue(String s, String s1) throws JBossCliException {
        throw new RuntimeException("getAttributeValue() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
