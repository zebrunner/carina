/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
 ******************************************************************************/
package com.qaprosoft.carina.core.foundation.performance;

import com.qaprosoft.carina.core.foundation.performance.Timer.IPerformanceOperation;

public enum ACTION_NAME implements IPerformanceOperation {
    CLICK("click"),
    TAP("tap"),
    DOUBLE_CLICK("double_click"),
    RIGHT_CLICK("right_click"),
    HOVER("hover"),
    SEND_KEYS("send_keys"),
    TYPE("type"),
    ATTACH_FILE("attach_file"),
    GET_TEXT("get_text"),
    GET_LOCATION("get_location"),
    GET_SIZE("get_size"),
    GET_ATTRIBUTE("get_attribute"),
    PAUSE("pause"),
    WAIT("wait"),
    CHECK("check"),
    UNCHECK("uncheck"),
    IS_CHECKED("is_checked"),
    SELECT("select"),
    SELECT_VALUES("select_values"),
    SELECT_BY_MATCHER("select_by_matcher"),
    SELECT_BY_PARTIAL_TEXT("select_by_partial_text"),
    SELECT_BY_INDEX("select_by_index"),
    GET_SELECTED_VALUE("get_selected_value"),
    GET_SELECTED_VALUES("get_selected_values"),
    CAPTURE_SCREENSHOT("capture_screenshot"),
    GET_LOGS("get_logs"),;


    private String key;

    private ACTION_NAME(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }
    
}
