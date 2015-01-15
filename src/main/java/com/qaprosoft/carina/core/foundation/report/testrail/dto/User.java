package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import com.qaprosoft.carina.core.foundation.report.testrail.core.Request;
import org.json.simple.JSONObject;

/**
 * Created by Patotsky on 14.01.2015.
 */
public class User {

    public static Request getUserByEmail(String userEmail){
        return new Request(new JSONObject(),"get_user_by_email&email="+userEmail,"GET");

    }
}
