package com.qaprosoft.carina.core.foundation.reporting;

import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemComparator;
import com.qaprosoft.carina.core.foundation.report.email.EmailValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class EmailTest {

    private static final String EMAIL = "test123@gmail.com";

    private static final TestResultItem TEST_RESULT_ITEM1 = new TestResultItem("carina-reporting", "Test api 1", "", TestResultType.PASS,
            "", "", new ArrayList<>(), "");
    private static final TestResultItem TEST_RESULT_ITEM1_1 = new TestResultItem("carina-reporting", "Test api 1", "", TestResultType.PASS,
            "", "", new ArrayList<>(), "");
    private static final TestResultItem TEST_RESULT_ITEM2 = new TestResultItem("carina-reporting", "Test api 2", "", TestResultType.PASS,
            "", "", new ArrayList<>(), "");
    private static final TestResultItem TEST_RESULT_ITEM3 = new TestResultItem("carina-reporting", "Test api 3", "", TestResultType.PASS,
            "", "", new ArrayList<>(), "");


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
