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

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;

/**
 * Created by Patotsky on 13.01.2015.
 */
public class Runs {

    @SuppressWarnings("unchecked")
    public static Request addRun(int suite_id, String name, int assignedto_id, int projectID, boolean insludeAllCases) {
        JSONObject obj = new JSONObject();
        obj.put("suite_id", suite_id);
        obj.put("name", name);
        obj.put("assignedto_id", assignedto_id);
        obj.put("include_all", insludeAllCases);
        return new Request(obj, "add_run/" + projectID, "POST");
    }
    
	public static Request addRun(int suite_id, String name, int assignedto_id, int projectID) {
		return addRun(suite_id, name, assignedto_id, projectID, Boolean.TRUE);
	}

    @SuppressWarnings("unchecked")
    public static Request addRun(int suite_id, String name, int assignedto_id, int projectID, int milestoneId, boolean insludeAllCases) {
        JSONObject obj = new JSONObject();
        obj.put("suite_id", suite_id);
        obj.put("name", name);
        obj.put("assignedto_id", assignedto_id);
        obj.put("include_all", insludeAllCases);
        obj.put("milestone_id", milestoneId);
        return new Request(obj, "add_run/" + projectID, "POST");
    }
    
	public static Request addRun(int suite_id, String name, int assignedto_id, int projectID, int milestoneId) {
		return addRun(suite_id, name, assignedto_id, projectID, milestoneId, Boolean.TRUE);
	}

    @SuppressWarnings("unchecked")
    public static Request addRun(int suite_id, String name, int assignedto_id, int projectID, int milestoneId, String desc) {
        JSONObject obj = new JSONObject();
        obj.put("suite_id", suite_id);
        obj.put("name", name);
        obj.put("assignedto_id", assignedto_id);
        obj.put("include_all", true);
        obj.put("milestone_id", milestoneId);
        obj.put("description", desc);
        return new Request(obj, "add_run/" + projectID, "POST");
    }
    
    @SuppressWarnings("unchecked")
    public static Request updateRun(int runId, List<String> caseIds) {
    		List<Integer> ids = new ArrayList<Integer>();
    		caseIds.stream().forEach(id -> ids.add(Integer.valueOf(id)));
        JSONObject obj = new JSONObject();
        obj.put("case_ids", ids);
        return new Request(obj, "update_run/" + runId, "POST");
    }

    public static Request getRun(int run_id) {
        return new Request(new JSONObject(), "get_run/" + run_id, "GET");

    }

	public static Request getRuns(int projectId) {
        return new Request(new JSONObject(), "get_runs/" + projectId, "GET");

    }

    @SuppressWarnings("unchecked")
    public static Request deleteRun(int run_id) {
        JSONObject obj = new JSONObject();
        obj.put("run_id", run_id);
        return new Request(obj, "delete_run/" + run_id, "POST");
    }

    @SuppressWarnings("unchecked")
    public static Request closeRun(int run_id) {
        JSONObject obj = new JSONObject();
        obj.put("run_id", run_id);
        return new Request(obj, "close_run/" + run_id, "POST");
    }
}
