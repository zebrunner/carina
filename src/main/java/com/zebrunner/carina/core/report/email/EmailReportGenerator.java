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
package com.zebrunner.carina.core.report.email;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;

import com.zebrunner.carina.core.config.ReportConfiguration;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    //todo move all html to the email-report.html file and read it
    private static final String CONTAINER = "<div id='container' style='width: 98%; padding: 10px; margin: 0; background: #EBEBE0; color: #717171; "
            + "font-family: Calibri;'><div id='summary'><h2 align='center' style='background-color: gray; color: white; padding: 10px; margin: 0;'>"
            + "${title}</h2><br><h2 style='clear: both; margin: 0;'>Summary:</h2><hr/><table style='width: 1000px;'><tr><td style='width: 100px;'>"
            + "Environment:</td><td>${env}</td></tr><tr><td>Version:</td><td>${version}</td></tr><tr><td>Browser:</td><td>${browser}</td></tr><tr>"
            + "<td>Finished:</td><td>${finish_date}</td></tr><tr class='pass' style='color: #66C266;'><td>Passed: </td><td>${pass_count}</td></tr>"
            + "<tr class='fail' style='color: #FF5C33;'><td>Failed:</td><td>${fail_count}</td></tr><tr class='skip' style='color: #FFD700;'>"
            + "<td>Skipped:</td><td>${skip_count}</td></tr><tr><td>Success rate:</td><td>${pass_rate}%</td></tr></table></div><br>${cucumber_results}"
            + "<br><div id='results'><h2 style='margin: 0;'>Test results:</h2><hr/><table cellspacing='0' cellpadding='0' style='width: 100%;'><tr>"
            + "<th width='10%' align='center'>Result</th><th width='75%'>Test name</th><th width='10%'>Test files</th></tr>${result_rows}</table>"
            + "</div>${created_items_list}</div>\n";
    private static final String PACKAGE_TR = "<tr><td colspan='4' class='package' style='background: gray; border-style: solid; border-width: 1px; "
            + "border-color: white; padding: 5px; color: white;'>${package_name}</td></tr>\n";
    private static final String PASS_TEST_LOG_DEMO_TR = "<tr class='pass' style='background: #66C266;'><td align='center' style='border-style: solid;"
            + " border-width: 1px; border-color: white; padding: 5px; color: white;'>PASSED</td><td style='border-style: solid; border-width: 1px; "
            + "border-color: white; padding: 5px; color: white;'>${test_name}</td><td align='center' style='border-style: solid; border-width: 1px; "
            + "border-color: white; padding: 5px; color: white;'><a target='_blank' href='${log_url}' style='color: white;'>Logs</a><span> | </span>"
            + "<a target='_blank' href='${screenshots_url}' style='color: white;'>Slides</a></td></tr>\n";
    private static final String FAIL_TEST_LOG_DEMO_TR = "<tr class='fail' style='background: #FF5C33;'><td align='center' style='border-style: solid;"
            + " border-width: 1px; border-color: white; padding: 5px; color: white;'>FAILED</td><td style='border-style: solid; border-width: 1px; "
            + "border-color: white; padding: 5px; color: white;'><span>${test_name}</span><div style=\"background:#ffcccc; color: black; padding: "
            + "5px; margin: 2px 0px 2px 0px;\">${fail_reason}</div></td><td align='center' style='border-style: solid; border-width: 1px; "
            + "border-color: white; padding: 5px; color: white;'><a target='_blank' href='${log_url}' style='color: white;'>Logs</a><span> | "
            + "</span><a target='_blank' href='${screenshots_url}' style='color: white;'>Slides</a></td></tr>\n";
    private static final String SKIP_TEST_LOG_DEMO_TR = "<tr class='skip' style='background: #DEB887;'><td align='center' style='border-style: solid;"
            + " border-width: 1px; border-color: white; padding: 5px; color: white;'>SKIPPED</td><td style='border-style: solid; border-width: 1px; "
            + "border-color: white; padding: 5px; color: white;'><span>${test_name}</span><div style=\"background:#FFE4B5; color: black; padding: 5px;"
            + " margin: 2px 0px 2px 0px;\">${skip_reason}</div></td><td align='center' style='border-style: solid; border-width: 1px; "
            + "border-color: white; padding: 5px; color: white;'><a target='_blank' href='${log_url}' style='color: white;'>Logs</a><span> | "
            + "</span><a target='_blank' href='${screenshots_url}' style='color: white;'>Slides</a></td></tr>\n";
    private static final String FAIL_CONFIG_LOG_DEMO_TR = "<tr class='fail' style='background: #009999;'><td align='center' style='border-style: "
            + "solid; border-width: 1px; border-color: white; padding: 5px; color: white;'>SYSTEM ISSUE</td><td style='border-style: solid; "
            + "border-width: 1px; border-color: white; padding: 5px; color: white;'><span>${test_name}</span><div style=\"background:#5ccccc; "
            + "color: black; padding: 5px; margin: 2px 0px 2px 0px;\">${fail_config_reason}</div></td><td align='center' style='border-style: "
            + "solid; border-width: 1px; border-color: white; padding: 5px; color: white;'><a target='_blank' href='${log_url}' style='color: "
            + "white;'>Logs</a><span> | </span><a target='_blank' href='${screenshots_url}' style='color: white;'>Slides</a></td></tr>\n";
    private static final String PASS_TEST_LOG_TR = "<tr class='pass' style='background: #66C266;'><td align='center' style='border-style:"
            + " solid; border-width: 1px; border-color: white; padding: 5px; color: white;'>PASSED</td><td style='border-style: solid; border-width:"
            + " 1px; border-color: white; padding: 5px; color: white;'>${test_name}</td><td align='center' style='border-style: solid; border-width:"
            + " 1px; border-color: white; padding: 5px; color: white;'><a target='_blank' href='${log_url}' style='color: white;'>Logs</a></td></tr>\n";
    private static final String FAIL_TEST_LOG_TR = "<tr class='fail' style='background: #FF5C33;'><td align='center' "
            + "style='border-style: solid; border-width: 1px; border-color: white; padding: 5px; color: white;'>FAILED</td><td style='border-style: "
            + "solid; border-width: 1px; border-color: white; padding: 5px; color: white;'><span>${test_name}</span><div style=\"background:#ffcccc; "
            + "color: black; padding: 5px; margin: 2px 0px 2px 0px;\">${fail_reason}</div></td><td align='center' style='border-style: solid; "
            + "border-width: 1px; border-color: white; padding: 5px; color: white;'><a target='_blank' href='${log_url}' style='color: white;'>"
            + "Logs</a></td></tr>\n";
    private static final String SKIP_TEST_LOG_TR = "<tr class='skip' style='background: #DEB887;'><td align='center' style='border-style: solid; "
            + "border-width: 1px; border-color: white; padding: 5px; color: white;'>SKIPPED</td><td style='border-style: solid; border-width: 1px; "
            + "border-color: white; padding: 5px; color: white;'><span>${test_name}</span><div style=\"background:#FFE4B5; color: black; padding: "
            + "5px; margin: 2px 0px 2px 0px;\">${skip_reason}</div></td><td align='center' style='border-style: solid; border-width: 1px; "
            + "border-color: white; padding: 5px; color: white;'><a target='_blank' href='${log_url}' style='color: white;'>Logs</a></td></tr>\n";
    private static final String FAIL_CONFIG_LOG_TR = "<tr class='fail' style='background: #009999;'><td align='center' "
            + "style='border-style: solid; border-width: 1px; border-color: white; padding: 5px; color: white;'>SYSTEM ISSUE</td><td style='"
            + "border-style: solid; border-width: 1px; border-color: white; padding: 5px; color: white;'><span>${test_name}</span><div style="
            + "\"background:#5ccccc; color: black; padding: 5px; margin: 2px 0px 2px 0px;\">${fail_config_reason}</div></td><td align='center' "
            + "style='border-style: solid; border-width: 1px; border-color: white; padding: 5px; color: white;'><a target='_blank' href='${log_url}' "
            + "style='color: white;'>Logs</a></td></tr>\n";
    private static final String CREATED_ITEMS_LIST = "<div><h3>Created items:</h3><ul>${created_items_list}</ul></div>";
    private static final String CREATED_ITEM = "<li>${created_item}</li>";
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
    private static final int MESSAGE_LIMIT = 2048;

    // Cucumber section
    private static final String CUCUMBER_RESULTS_PLACEHOLDER = "${cucumber_results}";

    private static final boolean INCLUDE_PASS = true;
    private static final boolean INCLUDE_FAIL = true;
    private static final boolean INCLUDE_SKIP = true;

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
        if (!testResultItems.isEmpty()) {
            testResultItems.sort(new EmailReportItemComparator());

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
                    result = testResultItem.getLinkToScreenshots() != null && !"".equals(testResultItem.getLinkToScreenshots())
                            ? FAIL_CONFIG_LOG_DEMO_TR
                            : FAIL_CONFIG_LOG_TR;
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
                    result = testResultItem.getLinkToScreenshots() != null && !"".equals(testResultItem.getLinkToScreenshots())
                            ? FAIL_TEST_LOG_DEMO_TR
                            : FAIL_TEST_LOG_TR;
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
                    result = testResultItem.getLinkToScreenshots() != null && !"".equals(testResultItem.getLinkToScreenshots())
                            ? SKIP_TEST_LOG_DEMO_TR
                            : SKIP_TEST_LOG_TR;
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
        if (testResultItem.getResult().name().equalsIgnoreCase("PASS") && !testResultItem.isConfig()) {
            passCount++;
            if (INCLUDE_PASS) {
                result = testResultItem.getLinkToScreenshots() != null && !"".equals(testResultItem.getLinkToScreenshots()) ? PASS_TEST_LOG_DEMO_TR
                        : PASS_TEST_LOG_TR;
                result = result.replace(TEST_NAME_PLACEHOLDER, testResultItem.getTest());
                result = result.replace(LOG_URL_PLACEHOLDER, testResultItem.getLinkToLog());

                if (testResultItem.getLinkToScreenshots() != null) {
                    result = result.replace(SCREENSHOTS_URL_PLACEHOLDER, testResultItem.getLinkToScreenshots());
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

            String link = ReportConfiguration.getCucumberReportLink();
            LOGGER.debug("Cucumber Report link: {}", link);
            result = String.format(
                    "<br/><b><a href='%s' style='color: green;' target='_blank' style='display: block'> Open Cucumber Report in a new tab</a></b><br/>",
                    link);
            LOGGER.debug("Cucumber result: {}", result);
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
            File reportOutputDirectory = new File(String.format("%s/%s", ReportContext.getBaseDirectory().toFile(), SpecialKeywords.CUCUMBER_REPORT_FOLDER));
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
            LOGGER.debug("Error happen during checking that CucumberReport Folder exists or not. Error: {}", e.getMessage());
        }
        return false;
    }
}
