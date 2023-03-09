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

import com.zebrunner.carina.api.apitools.validation.XmlCompareMode;
import com.zebrunner.carina.api.apitools.validation.XmlValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.normalizeSpace;

public class XmlValidatorTest {

    @Test
    public void testValidateXmlSuccess() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/actual.xml"))
                .collect(Collectors.joining("\n"));
        XmlValidator.validateXml(actualXmlData,
                "src/test/resources/validation/xml_file/expected.xml", XmlCompareMode.STRICT);
    }

    @Test
    public void testValidateXmlSuccess2() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/actual1.xml"))
                .collect(Collectors.joining("\n"));
        XmlValidator.validateXml(actualXmlData,
                "src/test/resources/validation/schema/schema_xml/expected1.xml", XmlCompareMode.STRICT);
    }

    @Test
    public void testValidateXmlError() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/actual_error.xml"))
                .collect(Collectors.joining("\n"));
        String expectedError = Files.lines(Path.of("src/test/resources/validation/xml_file/error_message/error.xml"))
                .collect(Collectors.joining("\n"));
        boolean isErrorThrown = false;
        try {
            XmlValidator.validateXml(actualXmlData, "src/test/resources/validation/xml_file/expected.xml",
                    XmlCompareMode.STRICT);
        } catch (AssertionError e) {
            isErrorThrown = true;
            System.out.println(e.getMessage());
            Assert.assertTrue(normalizeSpace(e.getMessage()).contains(normalizeSpace(expectedError)),
                    "Error message not as expected");
        }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }

    @Test
    public void testValidateXmlNotStrictOrderSuccess() throws IOException {
        String actualXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/actual_order.xml"))
                .collect(Collectors.joining("\n"));
        XmlValidator.validateXml(actualXmlData, "src/test/resources/validation/xml_file/actual.xml",
                XmlCompareMode.NON_STRICT);
    }

    @Test
    public void testValidateNonExtensibleError() throws IOException {
        String expectedXmlData = Files.lines(Path.of("src/test/resources/validation/xml_file/expected_extensible.xml"))
                .collect(Collectors.joining("\n"));
        String expectedError = Files.lines(Path.of("src/test/resources/validation/xml_file/error_message/error_extensible.xml"))
                .collect(Collectors.joining("\n"));
        boolean isErrorThrown = false;
        try {
            XmlValidator.validateXml(expectedXmlData,
                    "src/test/resources/validation/xml_file/actual.xml", XmlCompareMode.NON_STRICT);
        } catch (AssertionError e) {
            isErrorThrown = true;
            Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError),
                    "Error message not as expected");
        }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }
}
