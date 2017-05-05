package com.qaprosoft.carina.core.foundation.utils.rest;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.log4j.Logger;

import java.util.Map;

import static com.jayway.restassured.RestAssured.given;


public class RestUtil {
    protected static final Logger LOGGER = Logger.getLogger(RestUtil.class);

    public static Response sendHttpPost(String contentType, String payload, String httpPostCommand) {
        return sendHttpPost(contentType, payload, httpPostCommand, true);
    }

    public static Response sendHttpPost(String contentType, Map<String,?> parameters, String httpPostCommand, boolean responseLog) {
        if (responseLog) {
            return
                    given()
                            .contentType(contentType)
                            .formParameters(parameters)
                            .log().all()
                            .expect()
                            .log().all()
                            .when()
                            .post(httpPostCommand);
        }
        return
                given()
                        .contentType(contentType)
                        .formParameters(parameters)
                        // .log().all()
                        .when()
                        .post(httpPostCommand);

    }

    public static Response sendHttpPost(String contentType, String payload, String httpPostCommand, boolean responseLog) {
        if (responseLog) {
            return
                    given()
                            .contentType(contentType)
                            .body(payload)
                            .log().all()
                            .expect()
                            .log().all()
                            .when()
                            .post(httpPostCommand);
        }
        return
                given()
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
            return
                    given()
                            .contentType(contentType)
                            .log().all()
                            .expect()
                            .log().all()
                            .when()
                            .get(httpGetCommand);
        }
        return
                given()
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
