package com.qaprosoft.carina.core.foundation.report.testrail.core;

/**
 * Created by Patotsky on 14.01.2015.
 */
public enum TestStatus {


    PASSED(1), BLOCKED(2), UNTESTED(3), RETEST(4), FAILED(5);


    int i;

    TestStatus(int i) {
        this.i = i;

    }

    public int getNumber() {
        return i;
    }
}
