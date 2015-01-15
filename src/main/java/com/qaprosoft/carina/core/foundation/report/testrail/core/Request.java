package com.qaprosoft.carina.core.foundation.report.testrail.core;

import org.json.simple.JSONObject;

/**
 * Created by Patotsky on 13.01.2015.
 */
public class Request {

    JSONObject jsonObject;
    String requstURL;
    String requetsType;



    public Request(JSONObject jsonObject, String requstURL, String requetsType) {
        this.jsonObject = jsonObject;
        this.requstURL = requstURL;
        this.requetsType = requetsType;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public String getRequstURL() {
        return requstURL;
    }

    public String getRequetsType() {
        return requetsType;
    }
}
