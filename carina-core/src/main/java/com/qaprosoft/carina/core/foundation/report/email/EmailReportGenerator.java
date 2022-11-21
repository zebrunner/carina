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
package com.qaprosoft.carina.core.foundation.report.email;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.utils.report.TestResult;
import com.zebrunner.carina.utils.report.TestResultItem;
import com.zebrunner.carina.utils.report.TestResultType;

/**
 * EmailReportGenerator generates emailable report using data from test suite log.
 * 
 * @author Alex Khursevich
 */
public class EmailReportGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static String CONTAINER = R.EMAIL.get("container");
    private static String PACKAGE_TR = R.EMAIL.get("package_tr");
    private static String PASS_TEST_LOG_DEMO_TR = R.EMAIL.get("pass_test_log_demo_tr");
    private static String FAIL_TEST_LOG_DEMO_TR = R.EMAIL.get("fail_test_log_demo_tr");
    private static String SKIP_TEST_LOG_DEMO_TR = R.EMAIL.get("skip_test_log_demo_tr");
    private static String FAIL_CONFIG_LOG_DEMO_TR = R.EMAIL.get("fail_config_log_demo_tr");
    private static String PASS_TEST_LOG_TR = R.EMAIL.get("pass_test_log_tr");
    private static String FAIL_TEST_LOG_TR = R.EMAIL.get("fail_test_log_tr");
    private static String SKIP_TEST_LOG_TR = R.EMAIL.get("skip_test_log_tr");
    private static String FAIL_CONFIG_LOG_TR = R.EMAIL.get("fail_config_log_tr");
    private static String CREATED_ITEMS_LIST = R.EMAIL.get("created_items_list");
    private static String CREATED_ITEM = R.EMAIL.get("created_item");
    private static final String TITLE_PLACEHOLDER = "${title}";
    private static final String ENV_PLACEHOLDER = "${env}";
    private static final String BROWSER_PLACEHOLDER = "${browser}";
    private static final String VERSION_PLACEHOLDER = "${version}";
    private static final String FINISH_DATE_PLACEHOLDER = "${finish_date}";
    private static final String PASS_COUNT_PLACEHOLDER = "${pass_count}";
    private static final String FAIL_COUNT_PLACEHOLDER = "${fail_count}";
    private static final String SKIP_COUNT_PLACEHOLDER = "${skip_count}";
    private static final String PASS_RATE_PLACEHOLDER = "${pass_rate}";
    private static final String RESULTS_PLACEHOLDER = "${result_rows}";
    private static final String PACKAGE_NAME_PLACEHOLDER = "${package_name}";
    private static final String TEST_NAME_PLACEHOLDER = "${test_name}";
    private static final String FAIL_REASON_PLACEHOLDER = "${fail_reason}";
    private static final String SKIP_REASON_PLACEHOLDER = "${skip_reason}";
    private static final String FAIL_CONFIG_REASON_PLACEHOLDER = "${fail_config_reason}";
    private static final String SCREENSHOTS_URL_PLACEHOLDER = "${screenshots_url}";
    private static final String LOG_URL_PLACEHOLDER = "${log_url}";
    private static final String CREATED_ITEMS_LIST_PLACEHOLDER = "${created_items_list}";
    private static final String CREATED_ITEM_PLACEHOLDER = "${created_item}";
    private static final int MESSAGE_LIMIT = R.EMAIL.getInt("fail_description_limit");
    
    // Cucumber section
    private static final String CUCUMBER_RESULTS_PLACEHOLDER = "${cucumber_results}";

    private static boolean INCLUDE_PASS = R.EMAIL.getBoolean("include_pass");
    private static boolean INCLUDE_FAIL = R.EMAIL.getBoolean("include_fail");
    private static boolean INCLUDE_SKIP = R.EMAIL.getBoolean("include_skip");

    private String emailBody = CONTAINER;
    private StringBuilder testResults = null;

    private int passCount = 0;
    private int failCount = 0;
    private int skipCount = 0;

    public EmailReportGenerator(String title, String url, String version, String browser, String finishDate,
            List<TestResultItem> testResultItems, List<String> createdItems) {
        emailBody = emailBody.replace(TITLE_PLACEHOLDER, title);
        emailBody = emailBody.replace(ENV_PLACEHOLDER, url);
        emailBody = emailBody.replace(VERSION_PLACEHOLDER, version);
        emailBody = emailBody.replace(BROWSER_PLACEHOLDER, browser);
        emailBody = emailBody.replace(FINISH_DATE_PLACEHOLDER, finishDate);
        emailBody = emailBody.replace(RESULTS_PLACEHOLDER, getTestResultsList(testResultItems));
        emailBody = emailBody.replace(PASS_COUNT_PLACEHOLDER, String.valueOf(passCount));
        emailBody = emailBody.replace(FAIL_COUNT_PLACEHOLDER, String.valueOf(failCount));
        emailBody = emailBody.replace(SKIP_COUNT_PLACEHOLDER, String.valueOf(skipCount));
        emailBody = emailBody.replace(PASS_RATE_PLACEHOLDER, String.valueOf(getSuccessRate()));
        emailBody = emailBody.replace(CREATED_ITEMS_LIST_PLACEHOLDER, getCreatedItemsList(createdItems));
        
        // Cucumber section
        emailBody = emailBody.replace(CUCUMBER_RESULTS_PLACEHOLDER, getCucumberResultsHTML());

    }

    public String getEmailBody() {
        return emailBody;
    }

    private String getTestResultsList(List<TestResultItem> testResultItems) {
        if (testResultItems.size() > 0) {
            Collections.sort(testResultItems, new EmailReportItemComparator());

            String packageName = "";
            testResults = new StringBuilder();
            for (TestResultItem testResultItem : testResultItems) {
                if (!testResultItem.isConfig() && !packageName.equals(testResultItem.getPack())) {
                    packageName = testResultItem.getPack();
                    testResults.append(PACKAGE_TR.replace(PACKAGE_NAME_PLACEHOLDER, packageName));
                }
                testResults.append(getTestRow(testResultItem));
            }
        }
        return testResults != null ? testResults.toString() : "";
    }

    private String getTestRow(TestResultItem testResultItem) {
        String result = "";
        String failReason = "";
        if (testResultItem.getResult().name().equalsIgnoreCase("FAIL")) {
            if (INCLUDE_FAIL) {
                if (testResultItem.isConfig()) {
                    result = testResultItem.getLinkToScreenshots() != null && !"".equals(testResultItem.getLinkToScreenshots()) ? FAIL_CONFIG_LOG_DEMO_TR : FAIL_CONFIG_LOG_TR;
                    result = result.replace(TEST_NAME_PLACEHOLDER, testResultItem.getTest());

                    failReason = testResultItem.getFailReason();
                    if (!StringUtils.isEmpty(failReason)) {
                        // Make description more compact for email report
                        failReason = failReason.length() > MESSAGE_LIMIT ? (failReason.substring(0, MESSAGE_LIMIT) + "...") : failReason;
                        result = result.replace(FAIL_CONFIG_REASON_PLACEHOLDER, formatFailReasonAsHtml(failReason));
                    } else {
                        result = result.replace(FAIL_CONFIG_REASON_PLACEHOLDER, "Undefined failure: contact qa engineer!");
                    }
                } else {
                    result = testResultItem.getLinkToScreenshots() != null && !"".equals(testResultItem.getLinkToScreenshots()) ? FAIL_TEST_LOG_DEMO_TR : FAIL_TEST_LOG_TR;
                    result = result.replace(TEST_NAME_PLACEHOLDER, testResultItem.getTest());

                    failReason = testResultItem.getFailReason();
                    if (!StringUtils.isEmpty(failReason)) {
                        // Make description more compact for email report
                        failReason = failReason.length() > MESSAGE_LIMIT ? (failReason.substring(0, MESSAGE_LIMIT) + "...") : failReason;
                        result = result.replace(FAIL_REASON_PLACEHOLDER, formatFailReasonAsHtml(failReason));
                    } else {
                        result = result.replace(FAIL_REASON_PLACEHOLDER, "Undefined failure: contact qa engineer!");
                    }
                }

                result = result.replace(LOG_URL_PLACEHOLDER, testResultItem.getLinkToLog());

                if (testResultItem.getLinkToScreenshots() != null) {
                    result = result.replace(SCREENSHOTS_URL_PLACEHOLDER, testResultItem.getLinkToScreenshots());
                }
            }
            failCount++;
        }

        if (testResultItem.getResult().name().equalsIgnoreCase("SKIP")) {
            failReason = testResultItem.getFailReason();
            if (!testResultItem.isConfig()) {
                if (INCLUDE_SKIP) {
                    result = testResultItem.getLinkToScreenshots() != null && !"".equals(testResultItem.getLinkToScreenshots()) ? SKIP_TEST_LOG_DEMO_TR : SKIP_TEST_LOG_TR;
                    result = result.replace(TEST_NAME_PLACEHOLDER, testResultItem.getTest());

                    if (!StringUtils.isEmpty(failReason)) {
                        // Make description more compact for email report
                        failReason = failReason.length() > MESSAGE_LIMIT
                                ? (failReason.substring(0, MESSAGE_LIMIT) + "...")
                                : failReason;
                        result = result.replace(SKIP_REASON_PLACEHOLDER, formatFailReasonAsHtml(failReason));
                    } else {
                        result = result.replace(SKIP_REASON_PLACEHOLDER,
                                "Analyze SYSTEM ISSUE log for details or check dependency settings for the test.");
                    }

                    result = result.replace(LOG_URL_PLACEHOLDER, testResultItem.getLinkToLog());

                    if (testResultItem.getLinkToScreenshots() != null) {
                        result = result.replace(SCREENSHOTS_URL_PLACEHOLDER, testResultItem.getLinkToScreenshots());
                    }
                }
                skipCount++;
            }
        }
        if (testResultItem.getResult().name().equalsIgnoreCase("PASS")) {
            if (!testResultItem.isConfig()) {
                passCount++;
                if (INCLUDE_PASS) {
                    result = testResultItem.getLinkToScreenshots() != null && !"".equals(testResultItem.getLinkToScreenshots()) ? PASS_TEST_LOG_DEMO_TR : PASS_TEST_LOG_TR;
                    result = result.replace(TEST_NAME_PLACEHOLDER, testResultItem.getTest());
                    result = result.replace(LOG_URL_PLACEHOLDER, testResultItem.getLinkToLog());

                    if (testResultItem.getLinkToScreenshots() != null) {
                        result = result.replace(SCREENSHOTS_URL_PLACEHOLDER, testResultItem.getLinkToScreenshots());
                    }
                }
            }
        }
        return result;
    }

    private int getSuccessRate() {
        return passCount > 0 ? (int) (((double) passCount) / ((double) passCount + (double) failCount + (double) skipCount) * 100) : 0;
    }

    public static TestResult getSuiteResult(List<TestResultItem> ris) {
        int passed = 0;
        int failed = 0;
        int failedKnownIssue = 0;
        int skipped = 0;

        for (TestResultItem ri : ris) {
            if (ri.isConfig()) {
                continue;
            }

            switch (ri.getResult()) {
            case PASS:
                passed++;
                break;
            case FAIL:
               failed++;
                break;
            case SKIP:
                skipped++;
                break;
            default:
                // do nothing
                break;
            }
        }
        TestResult result = new TestResult(TestResultType.FAIL);
        if (passed > 0 && failedKnownIssue == 0 && failed == 0 && skipped == 0) {
            result.setTestResultType(TestResultType.PASS);
        } else if (passed >= 0 && failedKnownIssue > 0 && failed == 0 && skipped == 0) {
            result.setTestResultType(TestResultType.PASS_WITH_KNOWN_ISSUES);
        } else if (passed >= 0 && failed == 0 && skipped > 0) {
            result.setTestResultType(TestResultType.SKIP);
        }

        result.setAmountOfPassed(passed);
        result.setAmountOfFailed(failed + failedKnownIssue);
        result.setAmountOfSkipped(skipped);

        return result;
    }

    public String getCreatedItemsList(List<String> createdItems) {
        if (!CollectionUtils.isEmpty(createdItems)) {
            StringBuilder result = new StringBuilder();
            for (String createdItem : createdItems) {
                result.append(CREATED_ITEM.replace(CREATED_ITEM_PLACEHOLDER, createdItem));
            }
            return CREATED_ITEMS_LIST.replace(CREATED_ITEMS_LIST_PLACEHOLDER, result.toString());
        } else {
            return "";
        }
    }

    public String formatFailReasonAsHtml(String reasonText) {
        if (!StringUtils.isEmpty(reasonText)) {
            reasonText = StringEscapeUtils.escapeHtml4(reasonText);
            reasonText = reasonText.replace("\n", "<br/>");
        }
        return reasonText;
    }

    private String getCucumberResultsHTML() {
        String result = "";

        if (isCucumberReportFolderExists()) {

            String link = ReportContext.getCucumberReportLink();
            LOGGER.debug("Cucumber Report link: " + link);
            result = String.format(
                    "<br/><b><a href='%s' style='color: green;' target='_blank' style='display: block'> Open Cucumber Report in a new tab</a></b><br/>",
                    link);
            LOGGER.debug("Cucumber result: " + result);
        }

        return result;
    }

    /**
     * Check that CucumberReport Folder exists.
     * 
     * @return boolean
     */
    private boolean isCucumberReportFolderExists() {
        try {
            File reportOutputDirectory = new File(String.format("%s/%s", ReportContext.getBaseDir(), SpecialKeywords.CUCUMBER_REPORT_FOLDER));
            if (reportOutputDirectory.exists() && reportOutputDirectory.isDirectory()) {
                if (reportOutputDirectory.list().length > 0) {
                    LOGGER.debug("Cucumber Report Folder is not empty!");
                    return true;
                } else {
                    LOGGER.error("Cucumber Report Folder is empty!");
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error happen during checking that CucumberReport Folder exists or not. Error: " + e.getMessage());
        }
        return false;
    }
    
}
