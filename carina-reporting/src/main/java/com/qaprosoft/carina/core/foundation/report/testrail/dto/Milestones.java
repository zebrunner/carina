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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;

/**
 * Created by Igor Vayner on 1/15/2015.
 */
public class Milestones {

    public static Request getMilestones(int project_id) {
        return new Request(new JSONObject(), "get_milestones/" + project_id, "GET");
    }

    @SuppressWarnings("unchecked")
    public static Request addMilestone(int project_id, String name) {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        return new Request(obj, "add_milestone/" + project_id, "POST");
    }

    public static boolean isMilestoneExist(JSONArray jsonArray, String milestoneName) {
        for (Object o : jsonArray) {
            @SuppressWarnings("unchecked")
            String nameValue = ((HashMap<String, String>) o).get("name");
            if (nameValue.equals(milestoneName)) {
                return true;
            }
        }
        return false;
    }

}
