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
import com.qaprosoft.carina.core.foundation.report.qtest.IQTestManager;
import com.qaprosoft.carina.core.foundation.report.qtest.QTestCases;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.zafira.models.dto.TagType;

/**
 * Tests for {@link IQTestManager}
 */
public class QTestTest implements IQTestManager {

    protected static final Logger LOGGER = Logger.getLogger(QTestTest.class);

    private static final String TEST_ID = "5,6,65500";
    private static final String EXPECTED_TEST_ID = "65500";
    private static final String FIRST_TEST_ID = "65536";
    private static final String SECOND_TEST_ID = "15536";

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

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, SpecialKeywords.IOS);

        QTestUdids = getQTestCasesUuid(result);

        Assert.assertTrue(QTestUdids.contains(FIRST_TEST_ID), "QTest should contain id=" + FIRST_TEST_ID);

        Assert.assertEquals(QTestUdids.size(), 1);

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, SpecialKeywords.ANDROID);

        QTestUdids = getQTestCasesUuid(result);

        Assert.assertTrue(QTestUdids.contains(SECOND_TEST_ID), "QTest should contain id=" + SECOND_TEST_ID);

        Assert.assertEquals(QTestUdids.size(), 1);

        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "");
    }


    @Test
    @QTestCases(id = FIRST_TEST_ID + ",3333")
    public void testQTestSetting() {
        setCases("3333,5555".split(","));

        ITestResult result = Reporter.getCurrentTestResult();

        Set<TagType> tags = new HashSet<TagType>();

        Set<String> QTestTags = getQTestCasesUuid(result);

        int projectID = getQTestProjectId(result.getTestContext());

        Set<TagType> finalTags = tags;
        QTestTags.forEach((entry) -> {
            TagType tagEntry = new TagType();
            tagEntry.setName(SpecialKeywords.QTEST_TESTCASE_UUID);
            tagEntry.setValue(projectID + "-" + entry);
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
    @QTestCases(id = FIRST_TEST_ID, locale = "en")
    @QTestCases(id = SECOND_TEST_ID, locale = "fr")
    public void testTestRailByLocale() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getQTestCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);

        R.CONFIG.put(Parameter.LOCALE.getKey(), "fr");
        testRailUdids = getQTestCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(SECOND_TEST_ID), "TestRail should contain id=" + SECOND_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);
        
        R.CONFIG.put(Parameter.LOCALE.getKey(), "en");
    }

    @Test
    @QTestCases(id = FIRST_TEST_ID, language = "en")
    @QTestCases(id = SECOND_TEST_ID, language = "fr")
    public void testTestRailBylanguage() {
        ITestResult result = Reporter.getCurrentTestResult();

        Set<String> testRailUdids = getQTestCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(FIRST_TEST_ID), "TestRail should contain id=" + FIRST_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);

        R.CONFIG.put(Parameter.LANGUAGE.getKey(), "fr");
        testRailUdids = getQTestCasesUuid(result);

        Assert.assertTrue(testRailUdids.contains(SECOND_TEST_ID), "TestRail should contain id=" + SECOND_TEST_ID);
        Assert.assertEquals(testRailUdids.size(), 1);
        
        R.CONFIG.put(Parameter.LANGUAGE.getKey(), "en");
    }

}
