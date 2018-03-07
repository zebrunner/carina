/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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
