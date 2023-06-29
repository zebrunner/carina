/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.utils.report;

import java.nio.charset.StandardCharsets;

public class TestResultItem {
    private String pack = null;
    private String test = null;
    private String description = null;    
    private String linkToLog = null;
    private String linkToScreenshots = null;
    private String failReason = null;
    private TestResultType result = null;

    private boolean config = false;

    public TestResultItem(String group, String test, String desc, TestResultType result, String linkToScreenshots, String linkToLog, String failReason) {
        this.pack = group;
        this.test = test;
        this.description = desc;
        this.result = result;
        this.linkToLog = linkToLog;
        this.linkToScreenshots = linkToScreenshots;
        this.failReason = failReason;
    }

    public String getPack() {
        return pack;
    }

    public String getTest() {
        return test;
    }
    
    public String getDescription() {
        return description;
    }

    public TestResultType getResult() {
        return result;
    }

    public String getLinkToLog() {
        return linkToLog;
    }

    public String getLinkToScreenshots() {
        return linkToScreenshots;
    }

    public String getFailReason() {
        if (failReason != null) {
            return new String(failReason.getBytes(), StandardCharsets.UTF_8);
        } else {
            return failReason;
        }

    }

    public String hash() {
        return pack.hashCode() + "-" + test.hashCode();
    }

    public boolean isConfig() {
        return config;
    }
}
