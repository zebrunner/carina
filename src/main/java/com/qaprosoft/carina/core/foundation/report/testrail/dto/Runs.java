package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;
import org.json.simple.JSONObject;

/**
 * Created by Patotsky on 13.01.2015.
 */
public class Runs {

    public static Request addRun(int suite_id, String name, int assignedto_id, int projectID){
        JSONObject obj=new JSONObject();
        obj.put("suite_id",suite_id);
        obj.put("name",name);
        obj.put("assignedto_id",assignedto_id);
        obj.put("include_all",true);
        return new Request(obj,"add_run/"+projectID,"POST");
    }
    public static Request addRun(int suite_id, String name, int assignedto_id, int projectID, int milestoneId){
        JSONObject obj=new JSONObject();
        obj.put("suite_id",suite_id);
        obj.put("name",name);
        obj.put("assignedto_id",assignedto_id);
        obj.put("include_all",true);
        obj.put("milestone_id",milestoneId);
        return new Request(obj,"add_run/"+projectID,"POST");
    }


    public static Request addRun(int suite_id, String name, int assignedto_id, int projectID, int milestoneId, String desc){
        JSONObject obj=new JSONObject();
        obj.put("suite_id",suite_id);
        obj.put("name",name);
        obj.put("assignedto_id",assignedto_id);
        obj.put("include_all",true);
        obj.put("milestone_id",milestoneId);
        obj.put("description",desc);
        return new Request(obj,"add_run/"+projectID,"POST");
    }

    public static Request getRun(int run_id){
        return new Request(new JSONObject(),"get_run/"+run_id,"GET");

    }
}
