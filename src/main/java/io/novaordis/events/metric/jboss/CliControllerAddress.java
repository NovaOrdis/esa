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

import io.novaordis.events.metric.MetricDefinitionException;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/31/16
 */
class CliControllerAddress {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 9999;
    public static final CliControllerAddress DEFAULT_CONTROLLER = new CliControllerAddress(DEFAULT_HOST, DEFAULT_PORT);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String address;
    private String host;
    private int port;

    // Constructors ----------------------------------------------------------------------------------------------------

    public CliControllerAddress(String address) throws MetricDefinitionException {

        this.address = address;

        parse(address);
    }

    CliControllerAddress(String host, int port) {

        this.host = host;
        this.port = port;
        this.address = host + ":" + port;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return address == null ? "null" : address;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void parse(String s) throws MetricDefinitionException {

        int i = s.indexOf(":");

        if (i == -1) {

            this.host = s;
            this.port = DEFAULT_PORT;
        }
        else {

            this.host = s.substring(0, i);

            if (i == s.length() - 1) {
                throw new MetricDefinitionException("missing port information");
            }

            String sp = s.substring(i + 1);

            try {

                this.port = Integer.parseInt(sp);
            }
            catch (Exception e) {

                throw new MetricDefinitionException("invalid port value \"" + sp + "\"");
            }
        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
