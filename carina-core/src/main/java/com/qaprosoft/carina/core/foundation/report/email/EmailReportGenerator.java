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
package com.qaprosoft.carina.core.foundation.report.email;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * EmailReportGenerator generates emailable report using data from test suite log.
 * 
 * @author Alex Khursevich
 */
public class EmailReportGenerator {
    protected static final Logger LOGGER = Logger.getLogger(EmailReportGenerator.class);

    private static String CONTAINER = R.EMAIL.get("container");
    private static String PACKAGE_TR = R.EMAIL.get("package_tr");
    private static String PASS_TEST_LOG_DEMO_TR = R.EMAIL.get("pass_test_log_demo_tr");
    private static String FAIL_TEST_LOG_DEMO_TR = R.EMAIL.get("fail_test_log_demo_tr");
    private static String BUG_TEST_LOG_DEMO_TR = R.EMAIL.get("bug_test_log_demo_tr");
    private static String SKIP_TEST_LOG_DEMO_TR = R.EMAIL.get("skip_test_log_demo_tr");
    private static String FAIL_CONFIG_LOG_DEMO_TR = R.EMAIL.get("fail_config_log_demo_tr");
    private static String PASS_TEST_LOG_TR = R.EMAIL.get("pass_test_log_tr");
    private static String FAIL_TEST_LOG_TR = R.EMAIL.get("fail_test_log_tr");
    private static String BUG_TEST_LOG_TR = R.EMAIL.get("bug_test_log_tr");
    private static String SKIP_TEST_LOG_TR = R.EMAIL.get("skip_test_log_tr");
    private static String FAIL_CONFIG_LOG_TR = R.EMAIL.get("fail_config_log_tr");
    private static String CREATED_ITEMS_LIST = R.EMAIL.get("created_items_list");
    private static String CREATED_ITEM = R.EMAIL.get("created_item");
    private static final String TITLE_PLACEHOLDER = "${title}";
    private static final String ENV_PLACEHOLDER = "${env}";
    private static final String DEVICE_PLACEHOLDER = "${device}";
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
    private static final String BUG_URL_PLACEHOLDER = "${bug_url}";
    private static final String BUG_ID_PLACEHOLDER = "${bug_id}";
    private static final int MESSAGE_LIMIT = R.EMAIL.getInt("fail_description_limit");

    private static boolean INCLUDE_PASS = R.EMAIL.getBoolean("include_pass");
    private static boolean INCLUDE_FAIL = R.EMAIL.getBoolean("include_fail");
    private static boolean INCLUDE_SKIP = R.EMAIL.getBoolean("include_skip");

    private String emailBody = CONTAINER;
    private StringBuilder testResults = null;

    private int passCount = 0;
    private int failCount = 0;
    private int skipCount = 0;

    public EmailReportGenerator(String title, String url, String version, String device, String browser, String finishDate,
            List<TestResultItem> testResultItems, List<String> createdItems) {
        emailBody = emailBody.replace(TITLE_PLACEHOLDER, title);
        emailBody = emailBody.replace(ENV_PLACEHOLDER, url);
        emailBody = emailBody.replace(DEVICE_PLACEHOLDER, device);
        emailBody = emailBody.replace(VERSION_PLACEHOLDER, version);
        emailBody = emailBody.replace(BROWSER_PLACEHOLDER, browser);
        emailBody = emailBody.replace(FINISH_DATE_PLACEHOLDER, finishDate);
        emailBody = emailBody.replace(RESULTS_PLACEHOLDER, getTestResultsList(testResultItems));
        emailBody = emailBody.replace(PASS_COUNT_PLACEHOLDER, String.valueOf(passCount));
        emailBody = emailBody.replace(FAIL_COUNT_PLACEHOLDER, String.valueOf(failCount));
        emailBody = emailBody.replace(SKIP_COUNT_PLACEHOLDER, String.valueOf(skipCount));
        emailBody = emailBody.replace(PASS_RATE_PLACEHOLDER, String.valueOf(getSuccessRate()));
        emailBody = emailBody.replace(CREATED_ITEMS_LIST_PLACEHOLDER, getCreatedItemsList(createdItems));
    }

    public String getEmailBody() {
        return emailBody;
    }

    private String getTestResultsList(List<TestResultItem> testResultItems) {
        if (testResultItems.size() > 0) {
            if (Configuration.getBoolean(Parameter.RESULT_SORTING)) {

                // TODO: identify way to synch config failure with testNG method
                Collections.sort(testResultItems, new EmailReportItemComparator());
            }

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
                    result = testResultItem.getLinkToScreenshots() != null ? FAIL_CONFIG_LOG_DEMO_TR : FAIL_CONFIG_LOG_TR;
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
                    if (Configuration.getBoolean(Parameter.TRACK_KNOWN_ISSUES) && !testResultItem.getJiraTickets().isEmpty()) {
                        result = testResultItem.getLinkToScreenshots() != null ? BUG_TEST_LOG_DEMO_TR : BUG_TEST_LOG_TR;
                    } else {
                        result = testResultItem.getLinkToScreenshots() != null ? FAIL_TEST_LOG_DEMO_TR : FAIL_TEST_LOG_TR;
                    }

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

            if (Configuration.getBoolean(Parameter.TRACK_KNOWN_ISSUES) && !testResultItem.getJiraTickets().isEmpty()) {
                // do nothing
            } else
                failCount++;
        }
        if (testResultItem.getResult().name().equalsIgnoreCase("SKIP")) {
            failReason = testResultItem.getFailReason();
            if (!testResultItem.isConfig() && !failReason.contains(SpecialKeywords.ALREADY_PASSED)
                    && !failReason.contains(SpecialKeywords.SKIP_EXECUTION)) {
                if (INCLUDE_SKIP) {
                    result = testResultItem.getLinkToScreenshots() != null ? SKIP_TEST_LOG_DEMO_TR : SKIP_TEST_LOG_TR;
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
                    result = testResultItem.getLinkToScreenshots() != null ? PASS_TEST_LOG_DEMO_TR : PASS_TEST_LOG_TR;
                    result = result.replace(TEST_NAME_PLACEHOLDER, testResultItem.getTest());
                    result = result.replace(LOG_URL_PLACEHOLDER, testResultItem.getLinkToLog());

                    if (testResultItem.getLinkToScreenshots() != null) {
                        result = result.replace(SCREENSHOTS_URL_PLACEHOLDER, testResultItem.getLinkToScreenshots());
                    }
                }
            }
        }

        List<String> jiraTickets = testResultItem.getJiraTickets();

        String bugId = null;
        String bugUrl = null;

        if (jiraTickets.size() > 0) {
            bugId = jiraTickets.get(0);

            if (!Configuration.get(Parameter.JIRA_URL).isEmpty()) {
                bugUrl = Configuration.get(Parameter.JIRA_URL) + "/browse/" + jiraTickets.get(0);
            }

            if (jiraTickets.size() > 1) {
                LOGGER.error("Current implementation doesn't support email report generation with several Jira Tickets fo single test!");
            }
        }
        if (bugId == null) {
            bugId = "N/A";
        }

        if (bugUrl == null) {
            bugUrl = "#";
        }
        result = result.replace(BUG_ID_PLACEHOLDER, bugId);
        result = result.replace(BUG_URL_PLACEHOLDER, bugUrl);

        return result;
    }

    private int getSuccessRate() {
        return passCount > 0 ? (int) (((double) passCount) / ((double) passCount + (double) failCount + (double) skipCount) * 100) : 0;
    }

    public static TestResultType getSuiteResult(List<TestResultItem> ris) {
        int passed = 0;
        int failed = 0;
        int failedKnownIssue = 0;
        int skipped = 0;
        int skipped_already_passed = 0;

        for (TestResultItem ri : ris) {
            if (ri.isConfig()) {
                continue;
            }

            switch (ri.getResult()) {
            case PASS:
                passed++;
                break;
            case FAIL:
                if (Configuration.getBoolean(Parameter.TRACK_KNOWN_ISSUES)) {
                    if (ri.getJiraTickets().size() > 0) {
                        // increment known issue counter
                        failedKnownIssue++;
                    } else {
                        failed++;
                    }
                } else {
                    failed++;
                }
                break;
            case SKIP:
                if (ri.getFailReason().startsWith(SpecialKeywords.ALREADY_PASSED)) {
                    skipped_already_passed++;
                } else if (ri.getFailReason().startsWith(SpecialKeywords.SKIP_EXECUTION)) {
                    // don't calculate such message at all as it shouldn't be
                    // included into the report at all
                } else {
                    skipped++;
                }
                break;
            case SKIP_ALL:
                // do nothing
                break;
            default:
                // do nothing
                break;
            }
        }
        TestResultType result;
        if (passed == 0 && failed == 0 && skipped == 0 && skipped_already_passed > 0) {
            result = TestResultType.SKIP_ALL_ALREADY_PASSED; // it was re-run of the suite where all tests passed during previous run
        } else if (passed > 0 && failedKnownIssue == 0 && failed == 0 && skipped == 0) {
            result = TestResultType.PASS;
        } else if (passed >= 0 && failedKnownIssue > 0 && failed == 0 && skipped == 0) {
            result = TestResultType.PASS_WITH_KNOWN_ISSUES;
        } else if (passed == 0 && failed == 0 && skipped > 0) {
            result = TestResultType.SKIP_ALL;
        } else if (passed >= 0 && failed == 0 && skipped > 0) {
            result = TestResultType.SKIP;
        } else {
            result = TestResultType.FAIL;
        }
        result.setPassed(passed);
        result.setFailed(failed + failedKnownIssue);
        result.setSkipped(skipped);

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

}
