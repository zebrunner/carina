/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.reporting;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.zebrunner.carina.core.report.testrail.ITestRailManager;
import com.zebrunner.carina.core.report.testrail.TestRailCases;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.webdriver.config.WebDriverConfiguration;

/**
 * Tests for {@link ITestRailManager}
 */
public class TestRailTest implements ITestRailManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PROJECT_ID = "1";
    private static final String SUITE_ID = "1";

    private static final String TEST_ID = "5,6,65500";
    private static final String EXPECTED_TEST_ID = "65500";
    private static final String FIRST_TEST_ID = "65536";
    private static final String SECOND_TEST_ID = "15536";
    
    @BeforeSuite()
    public void initData(ITestContext context) {
        context.getSuite().setAttribute(SpecialKeywords.TESTRAIL_SUITE_ID, SUITE_ID);
    }

    @AfterMethod
    public void afterMethod() {
        R.CONFIG.getTestProperties().clear();
    }

    @Test
    @TestRailCases(testCasesId = TEST_ID)
    public void testTestRailList() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(EXPECTED_TEST_ID), "TestRail should contain id=" + EXPECTED_TEST_ID);

        Assert.assertEquals(testRailUdids.size(), 3);

        LOGGER.info("TestRail list: " + testRailUdids.toString());
    }


    @Test
    @TestRailCases(testCasesId = FIRST_TEST_ID)
    public void testTestRailSimple() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);

        Assert.assertEquals(testRailUdids.size(), 1);
    }

    @Test
    @TestRailCases(testCasesId = TEST_ID)
    @TestRailCases(testCasesId = FIRST_TEST_ID)
    public void testTestRailMix() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);

        LOGGER.info("TestRail list: " + testRailUdids.toString());

        Assert.assertEquals(testRailUdids.size(), 4);
        R.CONFIG.getTestProperties().clear();
    }

    @Test
    @TestRailCases(testCasesId = FIRST_TEST_ID)
    @TestRailCases(testCasesId = SECOND_TEST_ID)
    public void testTestRailMulti() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);

        Assert.assertTrue(testRailUdids.contains(SECOND_TEST_ID), "TestRail should contain id=" + SECOND_TEST_ID);

        Assert.assertEquals(testRailUdids.size(), 2);
    }

    @Test
    @TestRailCases(testCasesId = FIRST_TEST_ID, platform = "ios")
    @TestRailCases(testCasesId = SECOND_TEST_ID, platform = "android")
    public void testTestRailByPlatform() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getTestRailCasesUuid(result);

        Assert.assertEquals(testRailUdids.size(), 0);

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, SpecialKeywords.IOS, true);

        testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);

        Assert.assertEquals(testRailUdids.size(), 1);

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, SpecialKeywords.ANDROID, true);

        testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(SECOND_TEST_ID), "TestRail should contain id=" + SECOND_TEST_ID);

        Assert.assertEquals(testRailUdids.size(), 1);

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "", true);
    }


    @Test
    @TestRailCases(testCasesId = FIRST_TEST_ID + ",3333")
    public void testTestRailSetting() {
        setCases("3333,5555".split(","));

        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailTags = getTestRailCasesUuid(result);

        Assert.assertEquals(testRailTags.size(), 3);
        Assert.assertTrue(testRailTags.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);
        Assert.assertTrue(testRailTags.contains("3333"), "TestRail should contain id=" + "3333");
        Assert.assertTrue(testRailTags.contains("5555"), "TestRail should contain id=" + "5555");
    }

    @Test
    @TestRailCases(testCasesId = FIRST_TEST_ID, locale = "en")
    @TestRailCases(testCasesId = SECOND_TEST_ID, locale = "fr")
    public void testTestRailByLocale() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);

        R.CONFIG.put(WebDriverConfiguration.Parameter.LOCALE.getKey(), "fr", true);
        testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(SECOND_TEST_ID), "TestRail should contain id=" + SECOND_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);
        
        R.CONFIG.put(WebDriverConfiguration.Parameter.LOCALE.getKey(), "en", true);
    }
    
    @Test
    @TestRailCases(testCasesId = FIRST_TEST_ID, suiteId = "1") //1 is default suiteId value during unit testing!
    @TestRailCases(testCasesId = SECOND_TEST_ID, suiteId = "2")
    public void testTestRailBySuite() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getTestRailCasesUuid(result);

        testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);

        Assert.assertEquals(testRailUdids.size(), 1);
    }

}
