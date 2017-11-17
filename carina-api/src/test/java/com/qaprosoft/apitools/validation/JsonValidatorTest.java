package com.qaprosoft.apitools.validation;

import static org.apache.commons.lang3.StringUtils.normalizeSpace;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonValidatorTest {

	@Test
	public void testArrayContainsValidation() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/contains/rs_array_many_items.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/contains/rs_array_2_items.json"), Charset.forName("UTF-8"));
		String expectedError = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/contains/error.json"), Charset.forName("UTF-8"));

		boolean isErrorThrown = false;
		try {
			JSONAssert.assertEquals(expectedRs, actualRs, new JsonKeywordsComparator(JSONCompareMode.STRICT,
					JsonCompareKeywords.ARRAY_CONTAINS.getKey() + "content"));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		} catch (AssertionError e) {
			isErrorThrown = true;
			Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
		}
		Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
	}

	@Test
	public void testArrayDiffSizeNoContainsFlag() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/contains/rs_array_many_items.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/contains/rs_array_2_items.json"), Charset.forName("UTF-8"));
		String expectedError = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/contains/error_diff_size.json"), Charset.forName("UTF-8"));

		boolean isErrorThrown = false;
		try {
			JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.STRICT);
		} catch (AssertionError e) {
			isErrorThrown = true;
			Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
		}
		Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
	}

	@Test
	public void testArrayWSkipSuccess() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader()
					.getResourceAsStream("validation/array/skip/array_act.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
					"validation/array/skip/array_exp.json"), Charset.forName("UTF-8"));

		JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.NON_EXTENSIBLE);
	}

	@Test
	public void testArrayWSkipError() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
					"validation/array/skip/array_act_err.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
					"validation/array/skip/array_exp.json"), Charset.forName("UTF-8"));
		String expectedError = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
					"validation/array/skip/error.json"), Charset.forName("UTF-8"));

		boolean isErrorThrown = false;
		try {
			JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.STRICT);
		} catch (AssertionError e) {
			isErrorThrown = true;
			Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
		}
		Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
	}

	@Test
	public void testArrayWDuplicateSuccess() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/duplicate/array_act.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/duplicate/array_exp.json"), Charset.forName("UTF-8"));

		JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.NON_EXTENSIBLE);
	}

	@Test
	public void testArrayWDuplicateError() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/duplicate/array_act_err.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/duplicate/array_exp.json"), Charset.forName("UTF-8"));
		String expectedError = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/duplicate/error.json"), Charset.forName("UTF-8"));

		boolean isErrorThrown = false;
		try {
			JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.STRICT);
		} catch (AssertionError e) {
			isErrorThrown = true;
			Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
		}
		Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
	}

	@Test
	public void testArrayOfIntegersSuccess() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_act.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_exp.json"), Charset.forName("UTF-8"));

		JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.NON_EXTENSIBLE);
	}
	
	@Test
	public void testArrayOfIntegersSkip() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_act.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_exp_skip.json"), Charset.forName("UTF-8"));

		JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.NON_EXTENSIBLE);
	}

	@Test
	public void testArrayOfIntegersError() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_act_err.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_exp.json"), Charset.forName("UTF-8"));
		String expectedError = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/error.json"), Charset.forName("UTF-8"));

		boolean isErrorThrown = false;
		try {
			JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.STRICT);
		} catch (AssertionError e) {
			isErrorThrown = true;
			Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError), "Error message not as expected");
		}
		Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
	}
	
	@Test
	public void testArrayOfIntegersContains() throws IOException, JSONException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_act_contains.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_exp.json"), Charset.forName("UTF-8"));

		JSONAssert.assertEquals(expectedRs, actualRs, new JsonKeywordsComparator(JSONCompareMode.STRICT,
				JsonCompareKeywords.ARRAY_CONTAINS.getKey() + "clientIds"));
	}
	
	@Test
	public void testArrayOfIntegersContainsMissed() throws IOException, JSONException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_act_contains_missed.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/array_exp.json"), Charset.forName("UTF-8"));
		String expectedError = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/integer/error_contains_missed.json"), Charset.forName("UTF-8"));

		boolean isErrorThrown = false;
		try {
			JSONAssert.assertEquals(expectedRs, actualRs, new JsonKeywordsComparator(JSONCompareMode.STRICT,
					JsonCompareKeywords.ARRAY_CONTAINS.getKey() + "clientIds"));
		} catch (AssertionError e) {
			isErrorThrown = true;
			Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError),
					"Error message not as expected");
		}
		Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
	}
	
	@Test
	public void testArrayValidationNotExtensible() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/mode/actual.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/mode/expected.json"), Charset.forName("UTF-8"));

		JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.NON_EXTENSIBLE);
	}
	
	@Test
	public void testArrayValidationStrict() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/mode/actual.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/array/mode/expected.json"), Charset.forName("UTF-8"));

		JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.STRICT);
	}
	
	@Test
	public void testObjectValidationLenient() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/object/mode/actual.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/object/mode/expected.json"), Charset.forName("UTF-8"));

		JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.LENIENT);
	}
	
	@Test
	public void testObjectValidationNotExtensible() throws IOException {
		String actualRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/object/mode/actual.json"), Charset.forName("UTF-8"));
		String expectedRs = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/object/mode/expected.json"), Charset.forName("UTF-8"));
		String expectedError = IOUtils.toString(JsonValidatorTest.class.getClassLoader().getResourceAsStream(
				"validation/object/mode/error.json"), Charset.forName("UTF-8"));

		boolean isErrorThrown = false;
		try {
			JsonValidator.validateJson(expectedRs, actualRs, JSONCompareMode.NON_EXTENSIBLE);
		} catch (AssertionError e) {
			isErrorThrown = true;
			Assert.assertEquals(normalizeSpace(e.getMessage()), normalizeSpace(expectedError),
					"Error message not as expected");
		}
		Assert.assertTrue(isErrorThrown, "Assertion Error not thrown");
	}
}
