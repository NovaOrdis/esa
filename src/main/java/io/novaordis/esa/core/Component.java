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

package io.novaordis.esa.core;

/**
 * An event pipeline component: an instance that can be used as part of an event pipeline. It could have a name, it can
 * be started and stopped and can have listeners registered on it. Usually starting involves putting internal threads
 * to work.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/24/16
 */
public interface Component {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    String getName();
    void setName(String name);

    /**
     * Idempotent.
     */
    void start() throws Exception;

    /**
     * Idempotent.
     */
    void stop();

    void addEndOfStreamListener(EndOfStreamListener listener);


}
