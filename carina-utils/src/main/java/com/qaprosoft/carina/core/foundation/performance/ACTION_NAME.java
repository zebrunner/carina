package com.qaprosoft.carina.core.foundation.performance;

import com.qaprosoft.carina.core.foundation.performance.Timer.IPerformanceOperation;

public enum ACTION_NAME implements IPerformanceOperation {
    CLICK("click"),
    DOUBLE_CLICK("double_click"),
    RIGHT_CLICK("right_click"),
    SEND_KEYS("send_kyes"),
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
    SELECT("select"),
    SELECT_VALUES("select_values"),
    SELECT_BY_MATCHER("select_by_matcher"),
    SELECT_BY_PARTIAL_TEXT("select_by_partial_text"),
    SELECT_BY_INDEX("select_by_index"),
    CAPTURE_SCREENSHOT("capture_screenshot");


    private String key;

    private ACTION_NAME(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }
    
}
