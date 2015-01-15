package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import com.qaprosoft.carina.core.foundation.report.testrail.TestStatus;
import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Results {

    public static Request addResults(int runId, HashMap<String,TestStatus> testStatusHashMap) {
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String,TestStatus> entry : testStatusHashMap.entrySet()) {
            JSONObject obj=new JSONObject();
            obj.put("case_id", Integer.parseInt(entry.getKey()));
            obj.put("status_id", entry.getValue().getNumber());

            jsonArray.add(obj);
        }
        JSONObject finalObject = new JSONObject();
        finalObject.put("results", jsonArray);

        return new Request(finalObject,"add_results_for_cases/"+runId,"POST");
    }
}
