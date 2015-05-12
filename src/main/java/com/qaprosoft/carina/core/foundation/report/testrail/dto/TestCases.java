package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;
import org.json.simple.JSONObject;

/**
 * Created by yauheni_patotski on 5/13/15.
 */
public class TestCases {

    public static Request getRe(int projectId, int suiteID) {
        return new Request(new JSONObject(), "get_cases/" + projectId + "&suite_id=dd" + suiteID, "GET");

    }
}
