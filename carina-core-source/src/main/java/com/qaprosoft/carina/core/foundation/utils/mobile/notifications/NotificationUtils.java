package com.qaprosoft.carina.core.foundation.utils.mobile.notifications;

import java.util.Map;

import com.qaprosoft.carina.core.foundation.utils.rest.RestUtil;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.response.Response;


public class NotificationUtils {

    protected static final Logger LOGGER = Logger.getLogger(NotificationUtils.class);

    /**
     * call Push Service
     *
     * @param contentType String
     * @param parameters  Map String, ?
     * @param url         String
     * @return JsonObject
     */
    public static JsonObject callPushService(String contentType, Map<String, ?> parameters, String url) {
        return callPushService(contentType, parameters, url, true);
    }

    /**
     * call Push Service
     *
     * @param contentType String
     * @param parameters  Map String, ?
     * @param url         String
     * @param responseLog boolean
     * @return JsonObject
     */
    public static JsonObject callPushService(String contentType, Map<String, ?> parameters, String url, boolean responseLog) {
        try {

            LOGGER.info("Request url: " + url);

            Response response = RestUtil.sendHttpPost(contentType, parameters, url.toString(), responseLog);


            if (response.getStatusCode() == 200) {
                LOGGER.debug("Call passed with status code '"
                        + response.getStatusCode()
                        + "'. ");

                JsonParser parser = new JsonParser();

                return parser.parse(response.asString()).getAsJsonObject();
            } else {
                LOGGER.error("Call failed with status code '"
                        + response.getStatusCode()
                        + "'. ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * get Push Service Response
     *
     * @param request String
     * @param url     String
     * @return JsonObject
     */
    public static JsonObject getPushServiceResponse(String request, String url) {
        return getPushServiceResponse("application/json", request, url, true);
    }

    /**
     * get Push Service Response
     *
     * @param contentType String
     * @param request     String
     * @param url         String
     * @param responseLog boolean
     * @return JsonObject
     */
    public static JsonObject getPushServiceResponse(String contentType, String request, String url, boolean responseLog) {
        try {

            LOGGER.info("Request url: " + url);
            Response response = RestUtil.sendHttpPost(contentType, request, url.toString(), responseLog);

            if (response.getStatusCode() == 200) {
                LOGGER.debug("Call passed with status code '"
                        + response.getStatusCode()
                        + "'. ");

                JsonParser parser = new JsonParser();

                return parser.parse(response.asString()).getAsJsonObject();
            } else {
                LOGGER.error("Call failed with status code '"
                        + response.getStatusCode()
                        + "'. ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get Get Service Response
     *
     * @param url     String
     * @return JsonObject
     */
    public static JsonObject getGetServiceResponse(String url) {
        return getGetServiceResponse("application/json", url, true);
    }

    /**
     * get Get Service Response
     *
     * @param contentType String
     * @param url         String
     * @param responseLog boolean
     * @return JsonObject
     */
    public static JsonObject getGetServiceResponse(String contentType, String url, boolean responseLog) {
        try {

            LOGGER.info("Request url: " + url);
            Response response = RestUtil.sendHttpGet(contentType, url.toString(), responseLog);

            if (response.getStatusCode() == 200) {
                LOGGER.debug("Call passed with status code '"
                        + response.getStatusCode()
                        + "'. ");

                JsonParser parser = new JsonParser();

                return parser.parse(response.asString()).getAsJsonObject();
            } else {
                LOGGER.error("Call failed with status code '"
                        + response.getStatusCode()
                        + "'. ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
