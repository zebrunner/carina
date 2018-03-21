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