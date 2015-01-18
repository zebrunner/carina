package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

/**
 * Created by Igor Vayner on 1/15/2015.
 */
public class Milestones {

    public static Request getMilestones(int project_id){
        return new Request(new JSONObject(),"get_milestones/"+project_id,"GET");
    }



    public static Request addMilestone(int project_id, String name){
        JSONObject obj=new JSONObject();
        obj.put("name",name);
        return new Request(obj,"add_milestone/"+project_id,"POST");
    }



    public static boolean isMilestoneExist(JSONArray jsonArray, String milestoneName){
        for (Object o : jsonArray) {
            String nameValue = ((HashMap<String ,String>)o).get("name");
            if(nameValue.equals(milestoneName)){
                return true;
            }
        }
        return false;
    }



}
