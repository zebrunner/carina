package com.qaprosoft.apitools.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PrintUtil {
	public static String prettyFormatJson(String json2format) {
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(json2format).getAsJsonObject();
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		return gson.toJson(json);
	}
}
