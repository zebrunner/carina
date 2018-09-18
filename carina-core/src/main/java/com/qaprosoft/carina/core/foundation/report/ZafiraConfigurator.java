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
package com.qaprosoft.carina.core.foundation.report;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.ISuite;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.performance.Timer;
import com.qaprosoft.carina.core.foundation.report.testrail.TestRail;
import com.qaprosoft.carina.core.foundation.retry.RetryCounter;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.utils.ownership.Ownership;
import com.qaprosoft.carina.core.foundation.utils.ownership.Ownership.OwnerType;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.zafira.config.IConfigurator;
import com.qaprosoft.zafira.models.db.TestRun.DriverMode;
import com.qaprosoft.zafira.models.dto.TestArtifactType;
import com.qaprosoft.zafira.models.dto.config.ArgumentType;
import com.qaprosoft.zafira.models.dto.config.ConfigurationType;

/**
 * Carina-based implementation of IConfigurator that provides better integration with Zafira reporting tool.
 *
 * @author akhursevich
 */
public class ZafiraConfigurator implements IConfigurator {
    protected static final Logger LOGGER = Logger.getLogger(ZafiraConfigurator.class);

    @Override
    public ConfigurationType getConfiguration() {
        ConfigurationType conf = new ConfigurationType();
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
        Device device = DevicePool.getDevice();
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
    public String getOwner(ISuite suite) {
        String owner = suite.getParameter("suiteOwner");
        return owner != null ? owner : "";
    }

    @Override
    public String getPrimaryOwner(ITestResult test) {
        // TODO: re-factor that
        return Ownership.getMethodOwner(test, OwnerType.PRIMARY);
    }

    @Override
    public String getSecondaryOwner(ITestResult test) {
        // TODO: re-factor that
        return Ownership.getMethodOwner(test, OwnerType.SECONDARY);
    }

    @Override
    public String getTestName(ITestResult test) {
        // TODO: avoid TestNamingUtil
        return TestNamingUtil.getCanonicalTestName(test);
    }

    @Override
    public String getTestMethodName(ITestResult test) {
        // TODO: avoid TestNamingUtil
        return TestNamingUtil.getCanonicalTestMethodName(test);
    }

    @Override
    public List<String> getTestWorkItems(ITestResult test) {
        return Jira.getTickets(test);
    }

    @Override
    public int getRunCount(ITestResult test) {
        return RetryCounter.getRunCount();
    }

    @Override
    public Map<String, Long> getTestMetrics(ITestResult test) {
        return Timer.readAndClear();
    }

    @Override
    public DriverMode getDriverMode() {
        return DriverMode.valueOf(R.CONFIG.get("driver_mode").toUpperCase());
    }

    @Override
    public Set<TestArtifactType> getArtifacts(ITestResult test) {
        // Generate additional artifacts links on test run
        test.setAttribute(SpecialKeywords.TESTRAIL_CASES_ID, TestRail.getCases(test));
        TestRail.updateAfterTest(test, (String) test.getTestContext().getAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE));
        TestRail.clearCases();
        return Artifacts.getArtifacts();
    }

}