/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.report.email;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

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
public class EmailReportGenerator
{
	private static String CONTAINER = R.EMAIL.get("container");
	private static String PACKAGE_TR = R.EMAIL.get("package_tr");
	private static String PASS_TEST_LOG_DEMO_TR = R.EMAIL.get("pass_test_log_demo_tr");
	private static String FAIL_TEST_LOG_DEMO_TR = R.EMAIL.get("fail_test_log_demo_tr");
	private static String PASS_TEST_LOG_TR = R.EMAIL.get("pass_test_log_tr");
	private static String FAIL_TEST_LOG_TR = R.EMAIL.get("fail_test_log_tr");
	private static String CREATED_ITEMS_LIST = R.EMAIL.get("created_items_list");
	private static String CREATED_ITEM = R.EMAIL.get("created_item");
	private static final String TITLE_PLACEHOLDER = "${title}";
	private static final String ENV_PLACEHOLDER = "${env}";
	private static final String BROWSER_PLACEHOLDER = "${browser}";
	private static final String VERSION_PLACEHOLDER = "${version}";
	private static final String FINISH_DATE_PLACEHOLDER = "${finish_date}";
	private static final String CI_TEST_JOB = "${ci_test_job}";
	private static final String PASS_COUNT_PLACEHOLDER = "${pass_count}";
	private static final String FAIL_COUNT_PLACEHOLDER = "${fail_count}";
	private static final String PASS_RATE_PLACEHOLDER = "${pass_rate}";
	private static final String RESULTS_PLACEHOLDER = "${result_rows}";
	private static final String PACKAGE_NAME_PLACEHOLDER = "${package_name}";
	private static final String TEST_NAME_PLACEHOLDER = "${test_name}";
	private static final String FAIL_REASON_PLACEHOLDER = "${fail_reason}";
	private static final String SCREENSHOTS_URL_PLACEHOLDER = "${screenshots_url}";
	private static final String LOG_URL_PLACEHOLDER = "${log_url}";
	private static final String CREATED_ITEMS_LIST_PLACEHOLDER = "${created_items_list}";
	private static final String CREATED_ITEM_PLACEHOLDER = "${created_item}";
	private static final String BUG_URL_PLACEHOLDER = "${bug_url}";
	private static final String BUG_ID_PLACEHOLDER = "${bug_id}";

	private String emailBody = CONTAINER;
	private StringBuilder testResults = null;
	private int passCount = 0;
	private int failCount = 0;

	public EmailReportGenerator(String title, String url, String version, String browser, String finishDate, String ciTestJob,
			List<TestResultItem> testResultItems, List<String> createdItems)
	{
		emailBody = emailBody.replace(TITLE_PLACEHOLDER, title);
		emailBody = emailBody.replace(ENV_PLACEHOLDER, url);
		emailBody = emailBody.replace(VERSION_PLACEHOLDER, version);
		emailBody = emailBody.replace(BROWSER_PLACEHOLDER, browser);
		emailBody = emailBody.replace(FINISH_DATE_PLACEHOLDER, finishDate);
		emailBody = emailBody.replace(CI_TEST_JOB, !StringUtils.isEmpty(ciTestJob) ? ciTestJob : "n/a");
		emailBody = emailBody.replace(RESULTS_PLACEHOLDER, getTestResultsList(testResultItems));
		emailBody = emailBody.replace(PASS_COUNT_PLACEHOLDER, String.valueOf(passCount));
		emailBody = emailBody.replace(FAIL_COUNT_PLACEHOLDER, String.valueOf(failCount));
		emailBody = emailBody.replace(PASS_RATE_PLACEHOLDER, String.valueOf(getSuccessRate()));
		emailBody = emailBody.replace(CREATED_ITEMS_LIST_PLACEHOLDER, getCreatedItemsList(createdItems));
	}

	public String getEmailBody()
	{
		return emailBody;
	}

	private String getTestResultsList(List<TestResultItem> testResultItems)
	{
		if (testResultItems.size() > 0)
		{
			Collections.sort(testResultItems, new EmailReportItemComparator());
			String packageName = testResultItems.get(0).getPack();

			testResults = new StringBuilder(PACKAGE_TR.replace(PACKAGE_NAME_PLACEHOLDER, packageName));
			for (TestResultItem testResultItem : testResultItems)
			{
				if (!packageName.equals(testResultItem.getPack()))
				{
					packageName = testResultItem.getPack();
					testResults.append(PACKAGE_TR.replace(PACKAGE_NAME_PLACEHOLDER, packageName));
				}
				testResults.append(getTestRow(testResultItem));
			}
		}
		return testResults != null ? testResults.toString() : "";
	}

	private String getTestRow(TestResultItem testResultItem)
	{
		String result = "";
		if ("FAIL".equals(testResultItem.getResult().name()))
		{
			result = testResultItem.getLinkToScreenshots() != null ? FAIL_TEST_LOG_DEMO_TR : FAIL_TEST_LOG_TR;
			result = result.replace(TEST_NAME_PLACEHOLDER, testResultItem.getTest());
			
			String failReason = testResultItem.getFailReason();
			if (!StringUtils.isEmpty(failReason))
			{
				// Make description more compact for email report																																																											
				failReason = failReason.length() > 500 ? (failReason.substring(0, 500) + "...") : failReason;
				result = result.replace(FAIL_REASON_PLACEHOLDER, formatFailReasonAsHtml(failReason));
			}
			else
			{
				result = result.replace(FAIL_REASON_PLACEHOLDER, "Test RuntimeException: contact qa engineer!");
			}
			
			result = result.replace(LOG_URL_PLACEHOLDER, testResultItem.getLinkToLog());
			
			if(testResultItem.getLinkToScreenshots() != null)
			{
				result = result.replace(SCREENSHOTS_URL_PLACEHOLDER, testResultItem.getLinkToScreenshots());
			}
			
			failCount++;
		}
		else
		{
			result = testResultItem.getLinkToScreenshots() != null ? PASS_TEST_LOG_DEMO_TR : PASS_TEST_LOG_TR;
			result = result.replace(TEST_NAME_PLACEHOLDER, testResultItem.getTest());
			result = result.replace(LOG_URL_PLACEHOLDER, testResultItem.getLinkToLog());
			
			if(testResultItem.getLinkToScreenshots() != null)
			{
				result = result.replace(SCREENSHOTS_URL_PLACEHOLDER, testResultItem.getLinkToScreenshots());
			}
			passCount++;
		}
		if (testResultItem.getDescription() != null && testResultItem.getDescription().contains("JIRA#"))
		{
			String id = testResultItem.getDescription().split("#")[1];
			String url = Configuration.get(Parameter.JIRA_URL) + id;
			result = result.replace(BUG_ID_PLACEHOLDER, id);
			result = result.replace(BUG_URL_PLACEHOLDER, url);
		}
		else
		{
			result = result.replace(BUG_ID_PLACEHOLDER, "N/A");
			result = result.replace(BUG_URL_PLACEHOLDER, "#");
		}
		return result;
	}

	private int getSuccessRate()
	{
		return passCount > 0 ? (int) (((double) passCount) / ((double) passCount + (double) failCount) * 100) : 0;
	}

	public TestResultType getSuiteResult()
	{
		return (failCount == 0 && passCount > 0) ? TestResultType.PASS : TestResultType.FAIL;
	}
	
	public static TestResultType getSuiteResult(List<TestResultItem> ris)
	{
		int passed = 0;
		int failed = 0;
		for(TestResultItem ri : ris)
		{
			if(ri.getResult().equals(TestResultType.PASS))
			{
				passed++;
			}
			else
			{
				failed++;
			}
		}
		return (failed == 0 && passed > 0) ? TestResultType.PASS : TestResultType.FAIL;
	}

	public String getCreatedItemsList(List<String> createdItems)
	{
		if (!CollectionUtils.isEmpty(createdItems))
		{
			StringBuilder result = new StringBuilder();
			for (String createdItem : createdItems)
			{
				result.append(CREATED_ITEM.replace(CREATED_ITEM_PLACEHOLDER, createdItem));
			}
			return CREATED_ITEMS_LIST.replace(CREATED_ITEMS_LIST_PLACEHOLDER, result.toString());
		}
		else
		{
			return "";
		}
	}

	public String formatFailReasonAsHtml(String reasonText)
	{
		if (!StringUtils.isEmpty(reasonText))
		{
			reasonText = StringEscapeUtils.escapeHtml4(reasonText);
			reasonText = reasonText.replace("\n", "<br/>");
		}
		return reasonText;
	}
}
