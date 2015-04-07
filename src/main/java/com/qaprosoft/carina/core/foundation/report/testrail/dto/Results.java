package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;

public class Results {

	@SuppressWarnings("unchecked")
	public static Request addResults(int runId, HashMap<String, TestCaseResult> testCaseResultHashMap) {
	        JSONArray jsonArray = new JSONArray();
	        for (Map.Entry<String, TestCaseResult> entry : testCaseResultHashMap.entrySet()) {
	            JSONObject obj = new JSONObject();
	            obj.put("case_id", Integer.parseInt(entry.getKey()));
	            obj.put("status_id", entry.getValue().getTestStatus().getNumber());
	            obj.put("version", entry.getValue().getVersion());
	            obj.put("assignedto_id", entry.getValue().getAssignTo());
	            obj.put("comment", entry.getValue().getComment());
	            obj.put("defects", entry.getValue().getDefects());


	            jsonArray.add(obj);
	        }
	        JSONObject finalObject = new JSONObject();
	        finalObject.put("results", jsonArray);

	        return new Request(finalObject, "add_results_for_cases/" + runId, "POST");
	    }
}
