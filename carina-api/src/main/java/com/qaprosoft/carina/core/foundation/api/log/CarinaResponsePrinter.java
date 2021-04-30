package com.qaprosoft.carina.core.foundation.api.log;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.filter.log.LogDetail.BODY;
import static io.restassured.filter.log.LogDetail.COOKIES;
import static io.restassured.filter.log.LogDetail.HEADERS;
import static io.restassured.filter.log.LogDetail.STATUS;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import io.restassured.filter.log.LogDetail;
import io.restassured.http.Cookies;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.internal.print.ResponsePrinter;
import io.restassured.internal.support.Prettifier;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;

public class CarinaResponsePrinter extends ResponsePrinter {

    private static final String BLACKLISTED = "[ BLACKLISTED ]";
    private static final String HEADER_NAME_AND_VALUE_SEPARATOR = ": ";

    private static final String HIDDEN_PATTERN = "****************";

    private static final Configuration configuration = Configuration.builder()
            .jsonProvider(
                    new JacksonJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();

    /**
     * Prints the response to the print stream
     *
     * @return A string of representing the response
     */
    public static String print(ResponseOptions responseOptions, ResponseBody responseBody, PrintStream stream, LogDetail logDetail,
            boolean shouldPrettyPrint, Set<String> blacklistedHeaders) {
        final StringBuilder builder = new StringBuilder();
        if (logDetail == ALL || logDetail == STATUS) {
            builder.append(responseOptions.statusLine());
        }
        if (logDetail == ALL || logDetail == HEADERS) {
            final Headers headers = responseOptions.headers();
            if (headers.exist()) {
                appendNewLineIfAll(logDetail, builder).append(toString(headers, blacklistedHeaders));
            }
        } else if (logDetail == COOKIES) {
            final Cookies cookies = responseOptions.detailedCookies();
            if (cookies.exist()) {
                appendNewLineIfAll(logDetail, builder).append(cookies.toString());
            }
        }
        if (logDetail == ALL || logDetail == BODY) {
            String responseBodyToAppend;
            if (shouldPrettyPrint) {
                responseBodyToAppend = new Prettifier().getPrettifiedBodyIfPossible(responseOptions, responseBody);
            } else {
                responseBodyToAppend = responseBody.asString();
            }
            if (logDetail == ALL && !isBlank(responseBodyToAppend)) {
                builder.append(SystemUtils.LINE_SEPARATOR).append(SystemUtils.LINE_SEPARATOR);
            }


            JsonNode updatedJson = JsonPath.using(configuration).parse(responseBodyToAppend).set("$.address.zipcode", HIDDEN_PATTERN).json();
            // JSONObject jsonObject = new JSONObject(responseBodyToAppend);
            // jsonObject.

            builder.append(updatedJson);
        }
        String response = builder.toString();
        stream.println(response);
        return response;
    }

    private static String toString(Headers headers, Set<String> blacklistedHeaders) {
        if (!headers.exist()) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();
        for (Header header : headers) {
            StringBuilder headerStringBuilder = builder.append(header.getName())
                    .append(HEADER_NAME_AND_VALUE_SEPARATOR);
            if (blacklistedHeaders.contains(header.getName())) {
                headerStringBuilder.append(BLACKLISTED);
            } else {
                headerStringBuilder.append(header.getValue());
            }
            headerStringBuilder
                    .append(SystemUtils.LINE_SEPARATOR);
        }
        builder.delete(builder.length() - SystemUtils.LINE_SEPARATOR.length(), builder.length());
        return builder.toString();
    }

    private static StringBuilder appendNewLineIfAll(LogDetail logDetail, StringBuilder builder) {
        if (logDetail == ALL) {
            builder.append(SystemUtils.LINE_SEPARATOR);
        }
        return builder;
    }
}
