package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;
import org.json.simple.JSONObject;

/**
 * Created by yauheni_patotski on 6/30/17.
 */
public class TestInfo {

    public static Request getTestInfo(String testID) {
        return new Request(new JSONObject(), "get_test/" + testID , "GET");

    }
}
