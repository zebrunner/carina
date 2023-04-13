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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.zebrunner.carina.core.report.email.EmailReportItemCollector;
import com.zebrunner.carina.core.report.email.EmailReportItemComparator;
import com.zebrunner.carina.core.report.email.EmailValidator;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.report.TestResultItem;
import com.zebrunner.carina.utils.report.TestResultType;

public class EmailTest {

    private static final String EMAIL = "test123@gmail.com";

    private static final TestResultItem TEST_RESULT_ITEM1 = new TestResultItem("carina-reporting", "Test api 1", "", TestResultType.PASS,
            "", "", "");
    private static final TestResultItem TEST_RESULT_ITEM1_1 = new TestResultItem("carina-reporting", "Test api 1", "", TestResultType.PASS,
            "", "", "");
    private static final TestResultItem TEST_RESULT_ITEM2 = new TestResultItem("carina-reporting", "Test api 2", "", TestResultType.PASS,
            "", "", "");
    private static final TestResultItem TEST_RESULT_ITEM3 = new TestResultItem("carina-reporting", "Test api 3", "", TestResultType.PASS,
            "", "", "");

    private static final String CREATED_ITEM1 = "item 1";
    private static final String CREATED_ITEM2 = "item 2";

    @AfterMethod
    public void afterMethod() {
        R.CONFIG.getTestProperties().clear();
    }

    @Test
    public void testEmailValidator() {
        EmailValidator emailValidator = new EmailValidator();

        Assert.assertTrue(emailValidator.validate(EMAIL), EMAIL + " is not validated email");
    }

    @Test
    public void testEmailReportCollector() {
        EmailReportItemCollector.push(TEST_RESULT_ITEM1);
        EmailReportItemCollector.push(TEST_RESULT_ITEM2);
        EmailReportItemCollector.push(TEST_RESULT_ITEM3);

        Assert.assertTrue(EmailReportItemCollector.getTestResults().contains(TEST_RESULT_ITEM1),
                TEST_RESULT_ITEM1.getTest() + " wasn't added to email report results map");
        Assert.assertTrue(EmailReportItemCollector.getTestResults().contains(TEST_RESULT_ITEM2),
                TEST_RESULT_ITEM2.getTest() + " wasn't added to email report results map");
        Assert.assertTrue(EmailReportItemCollector.getTestResults().contains(TEST_RESULT_ITEM3),
                TEST_RESULT_ITEM3.getTest() + " wasn't added to email report results map");
    }

    @Test
    public void testPushStringEmailReportCollector() {
        EmailReportItemCollector.push(CREATED_ITEM1);
        EmailReportItemCollector.push(CREATED_ITEM2);

        Assert.assertTrue(EmailReportItemCollector.getCreatedItems().contains(CREATED_ITEM1),
                CREATED_ITEM1 + " wasn't added to email created items list");
        Assert.assertTrue(EmailReportItemCollector.getCreatedItems().contains(CREATED_ITEM1),
                CREATED_ITEM2 + " wasn't added to email created items list");
    }

    @Test
    public void testEmailReportComparatorTheDifferentTestResultItems() {
        Assert.assertFalse(isEqual(TEST_RESULT_ITEM1, TEST_RESULT_ITEM2),
                TEST_RESULT_ITEM1.getTest() + " is the same as " + TEST_RESULT_ITEM2.getTest());
    }

    @Test
    public void testEmailReportComparatorTheSameTestResultItems() {
        Assert.assertTrue(isEqual(TEST_RESULT_ITEM1, TEST_RESULT_ITEM1_1),
                TEST_RESULT_ITEM1.getTest() + " is different than " + TEST_RESULT_ITEM1_1.getTest());
    }

    private boolean isEqual(TestResultItem testResultItem1, TestResultItem testResultItem2) {
        EmailReportItemComparator comparator = new EmailReportItemComparator();

        return comparator.compare(testResultItem1, testResultItem2) == 0;
    }
}
