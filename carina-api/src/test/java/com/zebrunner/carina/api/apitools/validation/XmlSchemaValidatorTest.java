/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.zebrunner.carina.api.apitools.validation;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;


public class XmlSchemaValidatorTest {

    @Test
    public void testValidateXmlSchemaSuccess1() throws IOException {
        String schema = "src/test/resources/validation/schema/schema_xml/schema.xml";
        String expectedRs = IOUtils.toString(XmlSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema_xml/expected.xml"), Charset.forName("UTF-8").toString());
        XmlValidator.validateXmlAgainstSchema(schema, expectedRs);
    }

    @Test
    public void testValidateXmlSchemaSuccess2() throws IOException {
        String schema = "src/test/resources/validation/schema/schema_xml/schema1.xml";
        String expectedRs = IOUtils.toString(XmlSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema_xml/expected1.xml"), Charset.forName("UTF-8").toString());
        XmlValidator.validateXmlAgainstSchema(schema, expectedRs);
    }

    @Test
    public void testValidateXmlSchemaError() throws IOException {
        String schema = "src/test/resources/validation/schema/schema_xml/error_schema.xml";
        String expectedRs = IOUtils.toString(XmlSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema_xml/expected.xml"), Charset.forName("UTF-8").toString());
        boolean isErrorThrown = false;
        try {
            XmlValidator.validateXmlAgainstSchema(schema, expectedRs);
        } catch (AssertionError e) {
            isErrorThrown = true;
        }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }
}
