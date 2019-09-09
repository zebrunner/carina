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
package com.qaprosoft.carina.core.foundation.reporting;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.report.testrail.ITestRailManager;
import com.qaprosoft.carina.core.foundation.report.testrail.TestRailCases;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.zafira.models.dto.TagType;

/**
 * Tests for {@link ITestRailManager}
 */
public class TestRailTest implements ITestRailManager {

    private static final Logger LOGGER = Logger.getLogger(TestRailTest.class);

    private static final String TEST_ID = "5,6,65500";
    private static final String EXPECTED_TEST_ID = "65500";
    private static final String FIRST_TEST_ID = "65536";
    private static final String SECOND_TEST_ID = "15536";

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

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, SpecialKeywords.IOS);

        testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);

        Assert.assertEquals(testRailUdids.size(), 1);

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, SpecialKeywords.ANDROID);

        testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(SECOND_TEST_ID), "TestRail should contain id=" + SECOND_TEST_ID);

        Assert.assertEquals(testRailUdids.size(), 1);

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "");
    }


    @Test
    @TestRailCases(testCasesId = FIRST_TEST_ID + ",3333")
    public void testTestRailSetting() {
        setCases("3333,5555".split(","));

        ITestResult result = Reporter.getCurrentTestResult();

        Set<TagType> tags = new HashSet<TagType>();

        Set<String> testRailTags = getTestRailCasesUuid(result);

        int projectID = getTestRailProjectId(result.getTestContext());
        int suiteID = getTestRailSuiteId(result.getTestContext());

        Set<TagType> finalTags = tags;
        testRailTags.forEach((entry) -> {
            TagType tagEntry = new TagType();
            tagEntry.setName(SpecialKeywords.TESTRAIL_TESTCASE_UUID);
            tagEntry.setValue(projectID + "-" + suiteID + "-" + entry);
            finalTags.add(tagEntry);
        });

        tags.stream().forEachOrdered((entry) -> {
            Object currentKey = entry.getName();
            Object currentValue = entry.getValue();
            LOGGER.info(currentKey + "=" + currentValue);
        });

        Assert.assertEquals(tags.size(), 3);

    }

    @Test
    @TestRailCases(testCasesId = FIRST_TEST_ID, locale = "en")
    @TestRailCases(testCasesId = SECOND_TEST_ID, locale = "fr")
    public void testTestRailByLocale() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);

        R.CONFIG.put(Parameter.LOCALE.getKey(), "fr");
        testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(SECOND_TEST_ID), "TestRail should contain id=" + SECOND_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);
        
        R.CONFIG.put(Parameter.LOCALE.getKey(), "en");
    }

    @Test
    @TestRailCases(testCasesId = FIRST_TEST_ID, language = "en")
    @TestRailCases(testCasesId = SECOND_TEST_ID, language = "fr")
    public void testTestRailBylanguage() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);

        R.CONFIG.put(Parameter.LANGUAGE.getKey(), "fr");
        testRailUdids = getTestRailCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(SECOND_TEST_ID), "TestRail should contain id=" + SECOND_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);
        
        R.CONFIG.put(Parameter.LANGUAGE.getKey(), "en");
    }
}
