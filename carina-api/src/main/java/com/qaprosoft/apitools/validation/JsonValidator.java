/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.apitools.validation;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class JsonValidator {
    private static final Logger LOGGER = Logger.getLogger(JsonValidator.class);

	public static void validateJson(String expectedJson, String actualJson, JSONCompareMode jsonCompareMode) {
		try {
			JSONAssert.assertEquals(expectedJson, actualJson, new JsonKeywordsComparator(jsonCompareMode));
		} catch (JSONException e) {
			throw new AssertionError(e);
		}
	}

	public static void validateJsonAgainstSchema(String jsonSchema, String jsonData) {
		Matcher m = Pattern.compile("\\d+", Pattern.MULTILINE).matcher(jsonSchema);
		if (m.find()) {
			int schemaVersion = Integer.valueOf(m.group());
			if (schemaVersion <= 4) {
				LOGGER.info("JSON schema of version below or equal to draft-04 was detected");
				validateJsonAgainstSchemaV3V4(jsonSchema, jsonData);
			} else {
				LOGGER.info("JSON schema of version higher than draft-04 was detected");
				validateJsonAgainstSchemaV6V7(jsonSchema, jsonData);
			}
		} else {
			LOGGER.warn("JSON schema version can not be detected");
			validateJsonAgainstSchemaV3V4(jsonSchema, jsonData);
		}
	}

	public static void validateJsonAgainstSchemaV3V4(String jsonSchema, String jsonData) {
		// create the Json nodes for schema and data
		JsonNode schemaNode;
		JsonNode data;
		try {
			schemaNode = JsonLoader.fromString(jsonSchema);
		} catch (IOException e) {
			throw new RuntimeException("Can't read schema from String: " + e.getMessage(), e);
		}
		try {
			data = JsonLoader.fromString(jsonData);
		} catch (IOException e) {
			throw new RuntimeException("Can't read json from String: " + e.getMessage(), e);
		}

		JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		// load the schema and validate
		JsonSchema schema;
		try {
			schema = factory.getJsonSchema(schemaNode);
		} catch (ProcessingException e) {
			throw new RuntimeException("Can't process shema", e);
		}
		ProcessingReport report;
		try {
			report = schema.validate(data, true);
		} catch (ProcessingException e) {
			throw new RuntimeException("Exception during processing Json", e);
		}
		if (report.isSuccess()) {
			LOGGER.info("Validation against Json schema successfully passed");
		} else {
			StringBuffer result = new StringBuffer("Validation against Json schema failed: \n");
			Iterator<ProcessingMessage> itr = report.iterator();
			while (itr.hasNext()) {
				ProcessingMessage message = (ProcessingMessage) itr.next();
				JsonNode json = message.asJson();
				String instance = json.get("instance").get("pointer").asText();
				String errorMsg = json.get("message").asText();
				result.append("[");
				result.append(instance);
				result.append("]: ");
				result.append(errorMsg);
				result.append("\n");
			}
			throw new AssertionError(result.toString());
		}
	}

	public static void validateJsonAgainstSchemaV6V7(String jsonSchema, String jsonData) {
		JSONObject rawSchema;
		try {
			rawSchema = new JSONObject(new JSONTokener(jsonSchema));
		} catch (JSONException e) {
			throw new RuntimeException("Can't parse json schema from file: " + e.getMessage(), e);
		}

		JSONObject data;
		try {
			data = new JSONObject(new JSONTokener(jsonData));
		} catch (JSONException e) {
			throw new RuntimeException("Can't parse json data schema from file: " + e.getMessage(), e);
		}

		Schema schema = SchemaLoader.load(rawSchema);
		StringBuffer result = new StringBuffer("Validation against Json schema failed: \n");
		try {
			schema.validate(data);
		} catch (ValidationException ex) {
			ex.getAllMessages().stream().peek(e -> result.append("\n")).forEach(result::append);
			throw new AssertionError(result.toString());
		}
	}
}
