package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;
import com.qaprosoft.carina.core.foundation.report.testrail.core.TestStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Results {

    public static Request addResults(int runId, HashMap<String, TestStatus> testStatusHashMap) {
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, TestStatus> entry : testStatusHashMap.entrySet()) {
            JSONObject obj = new JSONObject();
            obj.put("case_id", Integer.parseInt(entry.getKey()));
            obj.put("status_id", entry.getValue().getNumber());

            jsonArray.add(obj);
        }
        JSONObject finalObject = new JSONObject();
        finalObject.put("results", jsonArray);

        return new Request(finalObject, "add_results_for_cases/" + runId, "POST");
    }

    public static Request addResultsWithLinks(int runId, Map<String, TestCaseResult> testCaseResultHashMap) {
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, TestCaseResult> entry : testCaseResultHashMap.entrySet()) {
            JSONObject obj = new JSONObject();
            obj.put("case_id", Integer.parseInt(entry.getKey()));
            obj.put("status_id", entry.getValue().getTestStatus().getNumber());
            obj.put("version", entry.getValue().getVersion());
            int assignID = entry.getValue().getAssignTo();
            if (assignID != 0) {
                obj.put("assignedto_id", entry.getValue().getAssignTo());
            }

            obj.put("comment", entry.getValue().getComment());
            obj.put("defects", entry.getValue().getDefects());


            jsonArray.add(obj);
        }
        JSONObject finalObject = new JSONObject();
        finalObject.put("results", jsonArray);

        return new Request(finalObject, "add_results_for_cases/" + runId, "POST");
    }
}
