package com.qaprosoft.apitools.validation;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONException;
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
	private final static Logger LOGGER = Logger.getLogger(JsonValidator.class);

	public static void validateJson(String expectedJson, String actualJson, JSONCompareMode jsonCompareMode) {
		try {
			JSONAssert.assertEquals(expectedJson, actualJson, new JsonKeywordsComparator(jsonCompareMode));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static void validateJsonAgainstSchema(String jsonSchema, String jsonData) {
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
			throw new RuntimeException(result.toString());
		}
	}
}
