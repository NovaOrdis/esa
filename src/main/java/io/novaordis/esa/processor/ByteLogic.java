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

package io.novaordis.esa.processor;

import io.novaordis.esa.event.Event;

import java.io.InputStream;
import java.util.List;

/**
 * Logic that knows how to process bytes and make events out of them.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/23/16
 */
public interface ByteLogic extends Logic {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Even if we pass an int as argument, that int is actually a byte resulted from an InputStream.read(). From
     * InputStream.read() documentation: "Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to <code>255</code>." -1 means end of stream.
     *
     * @return a list of events, which may be empty when multiple bytes are required to generate a single event, it
     * may contain just one element, or it may contain multiple elements. It is never null.
     *
     * @see InputStream#read()
     */
    List<Event> process(int b);

}
