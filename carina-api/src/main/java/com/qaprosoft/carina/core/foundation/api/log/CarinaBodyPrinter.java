package com.qaprosoft.carina.core.foundation.api.log;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import io.restassured.http.ContentType;
import io.restassured.response.ResponseBody;
import io.restassured.specification.FilterableRequestSpecification;

public class CarinaBodyPrinter {

    private final static Logger LOGGER = Logger.getLogger(CarinaBodyPrinter.class);

    private static final String HIDDEN_PATTERN = "****************";

    private static final String NONE = "<none>";
    private static final String TAB = "\t";

    private static final Configuration JSON_PARSE_CFG = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider()).build();

    /**
     * Prints the response to the print stream
     *
     * @return A string of representing the response
     */
    public static String printResponseBody(ResponseBody<?> responseBody, PrintStream stream, boolean shouldPrettyPrint, Set<String> hiddenPaths,
            ContentType contentType) {
        final StringBuilder builder = new StringBuilder();
        String responseBodyToAppend = new String(responseBody.asString());

        // replace values by paths
        responseBodyToAppend = replaceValues(responseBodyToAppend, hiddenPaths, contentType);

        // pretty print
        if (shouldPrettyPrint) {
            builder.append(prettyPrint(responseBodyToAppend));
        } else {
            builder.append(responseBodyToAppend);
        }

        if (!isBlank(responseBodyToAppend)) {
            builder.append(SystemUtils.LINE_SEPARATOR).append(SystemUtils.LINE_SEPARATOR);
        }
        String response = builder.toString();
        stream.println(response);
        return response;
    }

    /**
     * Prints the request to the print stream
     */
    public static void printRequestBody(FilterableRequestSpecification requestSpec, PrintStream stream, boolean shouldPrettyPrint,
            Set<String> hiddenPaths, ContentType contentType) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Body:");
        if (requestSpec.getBody() != null) {
            String body = new String((String) requestSpec.getBody());

            // replace values by paths
            body = replaceValues(body, hiddenPaths, contentType);

            // pretty print
            if (shouldPrettyPrint) {
                body = prettyPrint(body);
            }
            builder.append(SystemUtils.LINE_SEPARATOR).append(body);
        } else {
            appendTab(appendTab(appendTab(builder))).append(NONE);
        }
        String response = builder.toString();
        stream.println(response);
    }

    private static String replaceValues(String body, Set<String> hiddenPaths, ContentType contentType) {
        if(!hiddenPaths.isEmpty()) {
            if (ContentType.JSON.equals(contentType)) {
                for (String p : hiddenPaths) {
                    body = JsonPath.using(JSON_PARSE_CFG).parse(body).set(p, HIDDEN_PATTERN).jsonString();
                }
            } else {
                LOGGER.warn(String.format("Only content type '%s' is supported for body parts hiding in logs", ContentType.JSON.toString()));
            }
        }
        
        return body;
    }

    private static String prettyPrint(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(json);
        String prettyJsonString = gson.toJson(je);
        return prettyJsonString;
    }
    
    private static StringBuilder appendTab(StringBuilder builder) {
        return builder.append(TAB);
    }

}
