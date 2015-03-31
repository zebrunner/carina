package com.qaprosoft.carina.core.foundation.report.testrail.dto;

import com.qaprosoft.carina.core.foundation.report.testrail.core.TestStatus;

/**
 * Created by Patotsky on 30.03.2015.
 */
public class TestCaseResult {

    private final String version;
    private final String elapsedTime;
    private final String comment;
    private final TestStatus testStatus;
    private final int assignTo;
    private final String defects;


    public TestCaseResult(String version, String elapsedTime, TestStatus testStatus) {

        this(version, elapsedTime, "", testStatus, 0, "");
    }

    public TestCaseResult(String version, String elapsedTime, String comment, TestStatus testStatus, int assignTo, String defects) {
        this.version = version;
        this.elapsedTime = elapsedTime;
        this.comment = comment;
        this.testStatus = testStatus;
        this.assignTo = assignTo;
        this.defects = defects;
    }

    public String getVersion() {
        return version;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public String getComment() {
        return comment;
    }

    public TestStatus getTestStatus() {
        return testStatus;
    }

    public int getAssignTo() {
        return assignTo;
    }

    public String getDefects() {
        return defects;
    }
}
