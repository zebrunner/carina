/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.utils.rest;

import static io.restassured.RestAssured.given;

import java.util.Map;

import org.apache.log4j.Logger;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class RestUtil {
    private static final Logger LOGGER = Logger.getLogger(RestUtil.class);

    public static Response sendHttpPost(String contentType, String payload, String httpPostCommand) {
        return sendHttpPost(contentType, payload, httpPostCommand, true);
    }

    public static Response sendHttpPost(String contentType, Map<String, ?> parameters, String httpPostCommand, boolean responseLog) {
        if (responseLog) {
            return given()
                    .contentType(contentType)
                    .formParams(parameters)
                    .log().all()
                    .expect()
                    .log().all()
                    .when()
                    .post(httpPostCommand);
        }
        return given()
                .contentType(contentType)
                .formParams(parameters)
                // .log().all()
                .when()
                .post(httpPostCommand);

    }

    public static Response sendHttpPost(String contentType, String payload, String httpPostCommand, boolean responseLog) {
        if (responseLog) {
            return given()
                    .contentType(contentType)
                    .body(payload)
                    .log().all()
                    .expect()
                    .log().all()
                    .when()
                    .post(httpPostCommand);
        }
        return given()
                .contentType(contentType)
                .body(payload)
                // .log().all()
                .when()
                .post(httpPostCommand);

    }

    public static Response sendHttpGet(String contentType, String httpGetCommand) {
        return sendHttpGet(contentType, httpGetCommand, true);
    }

    public static Response sendHttpGet(String contentType, String httpGetCommand, boolean responseLog) {
        if (responseLog) {
            return given()
                    .contentType(contentType)
                    .log().all()
                    .expect()
                    .log().all()
                    .when()
                    .get(httpGetCommand);
        }
        return given()
                .contentType(contentType)
                .log().all()
                .when()
                .get(httpGetCommand);
    }

    public static int statusCode(Response response) {
        return response.getStatusCode();
    }

    public static boolean isResponseCorrect(Response response) {
        return isStatusCode(response, 200);
    }

    public static boolean isStatusCode(Response response, int statusCode) {
        return statusCode == statusCode(response);
    }

    public static String statusLine(Response response) {
        return response.getStatusLine();
    }

    public static String stringResponse(Response response) {
        String strResponse = null;
        try {
            strResponse = response.asString();
        } catch (Throwable thr) {
            LOGGER.info("Error: " + thr.getMessage());
        }
        return strResponse;
    }

    public static JsonPath jsonResponse(Response response) {
        return response.jsonPath();
    }
}
