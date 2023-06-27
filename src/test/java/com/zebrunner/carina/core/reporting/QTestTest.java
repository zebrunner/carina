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

import com.zebrunner.carina.core.report.qtest.IQTestManager;
import com.zebrunner.carina.core.report.qtest.QTestCases;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.webdriver.config.WebDriverConfiguration;

/**
 * Tests for {@link IQTestManager}
 */
public class QTestTest implements IQTestManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PROJECT_ID = "1";

    private static final String TEST_ID = "5,6,65500";
    private static final String EXPECTED_TEST_ID = "65500";
    private static final String FIRST_TEST_ID = "65536";
    private static final String SECOND_TEST_ID = "15536";

    @BeforeSuite()
    public void initData(ITestContext context) {
        context.getSuite().setAttribute(SpecialKeywords.QTEST_PROJECT_ID, PROJECT_ID);
    }

    @AfterMethod
    public void afterMethod() {
        R.CONFIG.getTestProperties().clear();
    }

    @Test
    @QTestCases(id = TEST_ID)
    public void testQTestList() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> QTestUdids = getQTestCasesUuid(result);

        LOGGER.info("QTest list: " + QTestUdids.toString());

        Assert.assertTrue(QTestUdids.contains(EXPECTED_TEST_ID), "QTest should contain id=" + EXPECTED_TEST_ID);

        Assert.assertEquals(QTestUdids.size(), 3);

    }


    @Test
    @QTestCases(id = FIRST_TEST_ID)
    public void testQTestSimple() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> QTestUdids = getQTestCasesUuid(result);

        Assert.assertTrue(QTestUdids.contains(FIRST_TEST_ID), "QTest should contain id=" + FIRST_TEST_ID);

        Assert.assertEquals(QTestUdids.size(), 1);
    }


    @Test
    @QTestCases(id = TEST_ID)
    @QTestCases(id = FIRST_TEST_ID)
    public void testQTestMix() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> QTestUdids = getQTestCasesUuid(result);

        Assert.assertTrue(QTestUdids.contains(FIRST_TEST_ID), "QTest should contain id=" + FIRST_TEST_ID);

        LOGGER.info("QTest list: " + QTestUdids.toString());

        Assert.assertEquals(QTestUdids.size(), 4);
    }


    @Test
    @QTestCases(id = FIRST_TEST_ID)
    @QTestCases(id = SECOND_TEST_ID)
    public void testQTestMulti() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> QTestUdids = getQTestCasesUuid(result);

        Assert.assertTrue(QTestUdids.contains(FIRST_TEST_ID), "QTest should contain id=" + FIRST_TEST_ID);

        Assert.assertTrue(QTestUdids.contains(SECOND_TEST_ID), "QTest should contain id=" + SECOND_TEST_ID);

        Assert.assertEquals(QTestUdids.size(), 2);
    }


    @Test
    @QTestCases(id = FIRST_TEST_ID, platform = "ios")
    @QTestCases(id = SECOND_TEST_ID, platform = "android")
    public void testQTestByPlatform() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> QTestUdids = getQTestCasesUuid(result);

        Assert.assertEquals(QTestUdids.size(), 0);

        LOGGER.info(QTestUdids.toString());

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, SpecialKeywords.IOS, true);

        QTestUdids = getQTestCasesUuid(result);

        Assert.assertTrue(QTestUdids.contains(FIRST_TEST_ID), "QTest should contain id=" + FIRST_TEST_ID);

        Assert.assertEquals(QTestUdids.size(), 1);

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, SpecialKeywords.ANDROID, true);

        QTestUdids = getQTestCasesUuid(result);

        Assert.assertTrue(QTestUdids.contains(SECOND_TEST_ID), "QTest should contain id=" + SECOND_TEST_ID);

        Assert.assertEquals(QTestUdids.size(), 1);

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "", true);
    }


    @Test
    @QTestCases(id = FIRST_TEST_ID + ",3333")
    public void testQTestSetting() {
        setCases("3333,5555".split(","));

        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> QTestTags = getQTestCasesUuid(result);

        Assert.assertEquals(QTestTags.size(), 3);
        Assert.assertTrue(QTestTags.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);
        Assert.assertTrue(QTestTags.contains("3333"), "TestRail should contain id=" + "3333");
        Assert.assertTrue(QTestTags.contains("5555"), "TestRail should contain id=" + "5555");

    }


    @Test
    @QTestCases(id = FIRST_TEST_ID, locale = "en")
    @QTestCases(id = SECOND_TEST_ID, locale = "fr")
    public void testTestRailByLocale() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> QTestUdids = getQTestCasesUuid(result);

        Assert.assertTrue(QTestUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);
        Assert.assertEquals(QTestUdids.size(), 1);

        R.CONFIG.put(WebDriverConfiguration.Parameter.LOCALE.getKey(), "fr", true);
        QTestUdids = getQTestCasesUuid(result);

        Assert.assertTrue(QTestUdids.contains(SECOND_TEST_ID), "TestRail should contain id=" + SECOND_TEST_ID);
        Assert.assertEquals(QTestUdids.size(), 1);

        R.CONFIG.put(WebDriverConfiguration.Parameter.LOCALE.getKey(), "en", true);
    }

}
