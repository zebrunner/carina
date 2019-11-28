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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qaprosoft.zafira.listener.adapter.SuiteAdapter;
import com.qaprosoft.zafira.listener.adapter.TestResultAdapter;
import com.qaprosoft.zafira.models.db.workitem.BaseWorkItem;
import org.apache.log4j.Logger;
import org.testng.ISuite;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.performance.Timer;
import com.qaprosoft.carina.core.foundation.report.qtest.IQTestManager;
import com.qaprosoft.carina.core.foundation.report.testrail.ITestRailManager;
import com.qaprosoft.carina.core.foundation.retry.RetryCounter;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.utils.ownership.Ownership;
import com.qaprosoft.carina.core.foundation.utils.tag.PriorityManager;
import com.qaprosoft.carina.core.foundation.utils.tag.TagManager;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.zafira.config.IConfigurator;
import com.qaprosoft.zafira.models.dto.TagType;
import com.qaprosoft.zafira.models.dto.TestArtifactType;
import com.qaprosoft.zafira.models.dto.config.ArgumentType;
import com.qaprosoft.zafira.models.dto.config.ConfigurationType;

/**
 * Carina-based implementation of IConfigurator that provides better integration with Zafira reporting tool.
 *
 * @author akhursevich
 */
public class ZafiraConfigurator implements IConfigurator, ITestRailManager, IQTestManager {
    private static final Logger LOGGER = Logger.getLogger(ZafiraConfigurator.class);

    @Override
    public ConfigurationType getConfiguration() {
        ConfigurationType conf = new ConfigurationType();
        
        String platform = Configuration.getPlatform();
        // override platform to register correctly on Zafira based on capabilities.platform and platformName
        R.CONFIG.put(Parameter.PLATFORM.getKey(), platform);
        
        String browser = Configuration.getBrowser();
        // override browser to register correctly on Zafira based on capabilities.browserName as well
        R.CONFIG.put(Parameter.BROWSER.getKey(), browser);
        
        for (Parameter parameter : Parameter.values()) {
            conf.getArg().add(buildArgumentType(parameter.getKey(), R.CONFIG.get(parameter.getKey())));
        }

        if (R.CONFIG.containsKey(SpecialKeywords.ACTUAL_BROWSER_VERSION)) {
            // update browser_version in returned config to register real value instead of * of matcher
            conf.getArg().add(buildArgumentType("browser_version", R.CONFIG.get(SpecialKeywords.ACTUAL_BROWSER_VERSION)));
        }

        if (buildArgumentType("platform", R.CONFIG.get("os")).getValue() != null) {
            // TODO: review and fix for 5.2.2.xx implementation
            // add custom arguments from browserStack
            conf.getArg().add(buildArgumentType("platform", R.CONFIG.get("os")));
            conf.getArg().add(buildArgumentType("platform_version", R.CONFIG.get("os_version")));
        }

        long threadId = Thread.currentThread().getId();

        // add custom arguments from current mobile device
        Device device = IDriverPool.getDefaultDevice();
        if (!device.getName().isEmpty()) {
            String deviceName = device.getName();
            String deviceOs = device.getOs();
            String deviceOsVersion = device.getOsVersion();

            conf.getArg().add(buildArgumentType("device", deviceName));
            conf.getArg().add(buildArgumentType("platform", deviceOs));
            conf.getArg().add(buildArgumentType("platform_version", deviceOsVersion));

            LOGGER.debug("Detected device: '" + deviceName + "'; os: '" + deviceOs + "'; os version: '" + deviceOsVersion + "'");
        } else {
            LOGGER.debug("Unable to detect current device for threadId: " + threadId);
        }
        return conf;
    }

    private ArgumentType buildArgumentType(String key, String value) {
        ArgumentType arg = new ArgumentType();
        arg.setKey(key);
        // populate valid null values for all arguments
        arg.setValue("NULL".equalsIgnoreCase(value) ? null : value);
        return arg;
    }

    @Override
    public String getOwner(SuiteAdapter suiteAdapter) {
        ISuite suite = (ISuite) suiteAdapter.getSuite();
        String owner = suite.getParameter("suiteOwner");
        LOGGER.debug("owner: " + owner);
        return owner != null ? owner : "";
    }

    @Override
    public String getPrimaryOwner(TestResultAdapter testResultAdapter) {
        // TODO: re-factor that
        ITestResult test = (ITestResult) testResultAdapter.getTestResult();
        String primaryOwner = Ownership.getMethodOwner(test); 
        LOGGER.debug("primaryOwner: " + primaryOwner);
        return primaryOwner;
    }

    //TODO need to remove this method from com.qaprosoft.zafira.config.IConfigurator
    @Override
    public String getSecondaryOwner(TestResultAdapter testResultAdapter) {
        // TODO: re-factor that
        ITestResult test = (ITestResult) testResultAdapter.getTestResult();
        String secondaryOwner = Ownership.getMethodOwner(test);
        LOGGER.debug("secondaryOwner: " + secondaryOwner);
        return secondaryOwner;
    }

    @Override
    public String getTestName(TestResultAdapter testResultAdapter) {
        // TODO: avoid TestNamingUtil
        ITestResult test = (ITestResult) testResultAdapter.getTestResult();
        String testName = TestNamingUtil.getCanonicalTestName(test);
        LOGGER.debug("testName: " + testName);
        return testName;
    }

    @Override
    public String getTestMethodName(TestResultAdapter testResultAdapter) {
        // TODO: avoid TestNamingUtil
        ITestResult test = (ITestResult) testResultAdapter.getTestResult();
        String testMethodName = TestNamingUtil.getCanonicalTestMethodName(test);
        LOGGER.debug("testMethodName: " + testMethodName);
        return testMethodName;
    }

    @Override
    public List<String> getTestWorkItems(TestResultAdapter testResultAdapter) {
        ITestResult test = (ITestResult) testResultAdapter.getTestResult();
        return Jira.getTickets(test);
    }

    @Override
    public BaseWorkItem getTestKnownIssue(TestResultAdapter testResultAdapter) {
        return Jira.getKnownIssue();
    }

    @Override
    public void clearTestWorkItemArtifacts() {
        Jira.clearJiraArtifacts();
    }

    @Override
    public int getRunCount(TestResultAdapter testResultAdapter) {
        int runCount = RetryCounter.getRunCount();
        LOGGER.debug("runCount: " + runCount);
        return runCount;
    }

    @Override
    public Map<String, Long> getTestMetrics(TestResultAdapter testResultAdapter) {
        return Timer.readAndClear();
    }

    @Override
    public Set<TagType> getTestTags(TestResultAdapter testResultAdapter) {
        LOGGER.debug("Collecting TestTags...");
        ITestResult test = (ITestResult) testResultAdapter.getTestResult();
        Set<TagType> tags = new HashSet<TagType>();

        String testPriority = PriorityManager.getPriority(test);
        if (testPriority != null && !testPriority.isEmpty()) {
            TagType priority = new TagType();
            priority.setName(SpecialKeywords.TEST_PRIORITY_KEY);
            priority.setValue(testPriority);
            tags.add(priority);
        }

        Map<String, String> testTags = TagManager.getTags(test);
        testTags.forEach((name, value) -> {
            TagType tagEntry = new TagType();
            tagEntry.setName(name);
            tagEntry.setValue(value);
            tags.add(tagEntry);
        });


        //Add testrail tags
        tags.addAll(getTestRailTags(test));

        //Add qTest tags
        tags.addAll(getQTestTags(test));
        
        LOGGER.debug("Found " + tags.size() + " new TestTags");
        return tags;
    }

    @Override
    public Set<TestArtifactType> getArtifacts(TestResultAdapter testResultAdapter) {
        LOGGER.debug("Collecting artifacts...");
        // Generate additional artifacts links on test run
        return Artifacts.getArtifacts();
    }

    @Override
    public void clearArtifacts() {
        Artifacts.clearArtifacts();
    }

    //Moved them separately for future easier reusing if getTestTags will be overridden.
    //TODO: Should we make them public or protected?
    private Set<TagType> getTestRailTags(ITestResult test) {
        LOGGER.debug("Collecting TestRail Tags...");
        Set<TagType> tags = new HashSet<TagType>();
        Set<String> testRailTags = getTestRailCasesUuid(test);
        int projectID = getTestRailProjectId(test.getTestContext());
        int suiteID = getTestRailSuiteId(test.getTestContext());

        //do not add test rail id if no integration tags/parameters detected
        if (projectID != -1 && suiteID != -1) {
            testRailTags.forEach((entry) -> {
                TagType tagEntry = new TagType();
                tagEntry.setName(SpecialKeywords.TESTRAIL_TESTCASE_UUID);
                tagEntry.setValue(projectID + "-" + suiteID + "-" + entry);
                tags.add(tagEntry);
            });
        }
        LOGGER.debug("Found " + tags.size() + " new TestRail tags");
        return tags;
    }

    private Set<TagType> getQTestTags(ITestResult test) {
        LOGGER.debug("Collecting qTest Tags...");
        Set<TagType> tags = new HashSet<TagType>();

        Set<String> qTestTags = getQTestCasesUuid(test);
        int projectID = getQTestProjectId(test.getTestContext());

        if (projectID != -1) {
            qTestTags.forEach((entry) -> {
                TagType tagEntry = new TagType();
                tagEntry.setName(SpecialKeywords.QTEST_TESTCASE_UUID);
                tagEntry.setValue(projectID + "-" + entry);
                tags.add(tagEntry);
            });
        }

        LOGGER.debug("Found " + tags.size() + " new qTest tags");
        return tags;
    }


}