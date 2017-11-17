package com.qaprosoft.apitools.validation;

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
				"validation/schema/schema4/rs_w_schema_error.json"), Charset.forName("UTF-8"));
		String schema = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/schema/schema4/schema4.json"), Charset.forName("UTF-8"));
		String expectedError = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/schema/schema4/error_schema4.json"), Charset.forName("UTF-8"));

		boolean isErrorThrown = false;
		try {
			JsonValidator.validateJsonAgainstSchema(schema, actualRs);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			isErrorThrown = true;
			Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
		}
		Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
	}

	@Test
	public void testErrorSchema3() throws IOException {
		String actualRs = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/schema/schema3/rs_w_schema_error.json"), Charset.forName("UTF-8"));
		String schema = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/schema/schema3/schema3.json"), Charset.forName("UTF-8"));
		String expectedError = IOUtils.toString(JsonSchemaValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/schema/schema3/error_schema3.json"), Charset.forName("UTF-8"));

		boolean isErrorThrown = false;
		try {
			JsonValidator.validateJsonAgainstSchema(schema, actualRs);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			isErrorThrown = true;
			Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
		}
		Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
	}
}
