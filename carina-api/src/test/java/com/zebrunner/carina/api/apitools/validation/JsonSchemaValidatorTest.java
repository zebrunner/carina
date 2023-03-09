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

import static org.apache.commons.lang3.StringUtils.normalizeSpace;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonSchemaValidatorTest {

	@Test
    public void testErrorSchema4() throws IOException {
        String actualRs = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema4/rs_w_schema_error.json"), Charset.forName("UTF-8").toString());
        String schema = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema4/schema4.json"), Charset.forName("UTF-8").toString());
        String expectedError = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema4/error_schema4.json"), Charset.forName("UTF-8").toString());

        boolean isErrorThrown = false;
        try {
			JsonValidator.validateJsonAgainstSchema(schema, actualRs);
		} catch (AssertionError e) {
            System.out.println(e.getMessage());
            isErrorThrown = true;
            Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
        }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }

	@Test
    public void testErrorSchema3() throws IOException {
        String actualRs = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema3/rs_w_schema_error.json"), Charset.forName("UTF-8").toString());
        String schema = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema3/schema3.json"), Charset.forName("UTF-8").toString());
        String expectedError = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema3/error_schema3.json"), Charset.forName("UTF-8").toString());

        boolean isErrorThrown = false;
        try {
            JsonValidator.validateJsonAgainstSchema(schema, actualRs);
		} catch (AssertionError e) {
            System.out.println(e.getMessage());
            isErrorThrown = true;
            Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
        }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }
    
	@Test
    public void testErrorSchema7() throws IOException {
        String actualRs = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema7/rs_w_schema_error.json"), Charset.forName("UTF-8").toString());
        String schema = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema7/schema7.json"), Charset.forName("UTF-8").toString());
        String expectedError = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
                "validation/schema/schema7/error_schema7.json"), Charset.forName("UTF-8").toString());

        boolean isErrorThrown = false;
        try {
			JsonValidator.validateJsonAgainstSchema(schema, actualRs);
		} catch (AssertionError e) {
            System.out.println(e.getMessage());
            isErrorThrown = true;
            Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
        }
        Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
    }
}
