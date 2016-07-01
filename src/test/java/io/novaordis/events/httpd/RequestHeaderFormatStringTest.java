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

package io.novaordis.events.httpd;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 2/4/16
 */
public class RequestHeaderFormatStringTest extends ParameterizedFormatStringTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void getLiteral() throws Exception {

        RequestHeaderFormatString i = new RequestHeaderFormatString("%{i,Test-Request-Header}");

        assertEquals("%{i,Test-Request-Header}", i.getLiteral());
    }

    @Test
    public void getLiteral_AlternativeFormat() throws Exception {

        RequestHeaderFormatString i = new RequestHeaderFormatString("%{Test-Request-Header}i");

        assertEquals("%{Test-Request-Header}i", i.getLiteral());
    }

    @Test
    public void literalStartsWithHeaderSpecificationButAlsoContainsSomethingElse() throws Exception {

        RequestHeaderFormatString i = new RequestHeaderFormatString("%{i,Test-Request-Header}blah");

        assertEquals("%{i,Test-Request-Header}", i.getLiteral());
    }

    // capitalization --------------------------------------------------------------------------------------------------

    @Test
    public void capitalization() throws Exception {

        RequestHeaderFormatString i = new RequestHeaderFormatString("%{i,Something}");
        assertEquals("Something", i.getHeaderName());

        RequestHeaderFormatString i2 = new RequestHeaderFormatString("%{i,SoMeThInG}");
        assertEquals("SoMeThInG", i2.getHeaderName());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected RequestHeaderFormatString getFormatStringToTest(String s) {

        if (s == null) {
            s = "%{i,Test-Request-Header}";
        }

        return new RequestHeaderFormatString(s);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
