/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.report;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TestResultItem {
    private String pack = null;
    private String test = null;
    private String linkToLog = null;
    private String linkToScreenshots = null;
    private String failReason = null;
    private String description = null;
    private List<String> jiraTickets = new ArrayList<String>(); // empty list
    private TestResultType result = null;

    private boolean config = false;

    public TestResultItem(String group, String test, TestResultType result, String linkToScreenshots, String linkToLog, String failReason) {
        this.pack = group;
        this.test = test;
        this.result = result;
        this.linkToLog = linkToLog;
        this.linkToScreenshots = linkToScreenshots;
        this.failReason = failReason;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public TestResultType getResult() {
        return result;
    }

    public void setResult(TestResultType result) {
        this.result = result;
    }

    public String getLinkToLog() {
        return linkToLog;
    }

    public void setLinkToLog(String linkToLog) {
        this.linkToLog = linkToLog;
    }

    public String getLinkToScreenshots() {
        return linkToScreenshots;
    }

    public void setLinkToScreenshots(String linkToScreenshots) {
        this.linkToScreenshots = linkToScreenshots;
    }

    public String getFailReason() {
        if (failReason != null) {
            return new String(failReason.getBytes(), Charset.forName("UTF-8"));
        } else {
            return failReason;
        }

    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getJiraTickets() {
        return jiraTickets;
    }

    public void setJiraTickets(List<String> jiraTickets) {
        this.jiraTickets = jiraTickets;
    }

    public String hash() {
        return String.valueOf(pack.hashCode()) + "-" + String.valueOf(test.hashCode());
    }

    public boolean isConfig() {
        return config;
    }
}
