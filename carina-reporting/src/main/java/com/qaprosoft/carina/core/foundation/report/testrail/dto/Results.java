package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;
import com.qaprosoft.carina.core.foundation.report.testrail.core.TestStatus;

public class Results {
	
	protected static final Logger LOGGER = Logger.getLogger(Results.class);

    @SuppressWarnings("unchecked")
	public static Request addResults(int runId, HashMap<String, TestStatus> testStatusHashMap) {
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, TestStatus> entry : testStatusHashMap.entrySet()) {
            JSONObject obj = new JSONObject();
            try {
            	obj.put("case_id", Integer.parseInt(entry.getKey()));
                obj.put("status_id", entry.getValue().getNumber());
                jsonArray.add(obj);
            } catch (NumberFormatException e) {
            	LOGGER.warn("Unable to get TestRail case_id from '" + entry.getKey() + "'");
            }
        }
        JSONObject finalObject = new JSONObject();
        finalObject.put("results", jsonArray);

        return new Request(finalObject, "add_results_for_cases/" + runId, "POST");
    }

    @SuppressWarnings("unchecked")
	public static Request addResultsWithLinks(int runId, Map<String, TestCaseResult> testCaseResultHashMap) {
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, TestCaseResult> entry : testCaseResultHashMap.entrySet()) {
            JSONObject obj = new JSONObject();
            try {
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
            } catch (NumberFormatException e) {
            	LOGGER.warn("Unable to get TestRail case_id from '" + entry.getKey() + "'");
            }
        }
        JSONObject finalObject = new JSONObject();
        finalObject.put("results", jsonArray);

        return new Request(finalObject, "add_results_for_cases/" + runId, "POST");
    }
}
