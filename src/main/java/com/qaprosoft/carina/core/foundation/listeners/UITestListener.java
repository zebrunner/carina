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
package com.qaprosoft.carina.core.foundation.listeners;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.qaprosoft.carina.core.foundation.log.TestLogCollector;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;
import com.qaprosoft.carina.core.foundation.retry.RetryCounter;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;

/**
 * Listener that controls retry logic for test according to retry_count configuration attribute.
 * Also it generates test result item if test passed or retry limit is exceed.
 * 
 * @author Alex Khursevich
 */
public class UITestListener extends AbstractTestListener
{
	private static final Logger LOGGER = Logger.getLogger(UITestListener.class);

	private static final int MAX_COUNT = Configuration.getInt(Parameter.RETRY_COUNT);

	@SuppressWarnings("deprecation")
	@Override
	public void onTestStart(ITestResult result)
	{
		super.onTestStart(result);
		String test = TestNamingUtil.getCanonicalTestName(result);
		DriverPool.associateTestNameWithDriver(test, DriverPool.getDriverBySessionId(result.getTestContext().getCurrentXmlTest().getParameters().get("sessionId")));
	}
	
	@Override
	public void onTestFailure(ITestResult result)
	{
		String test = TestNamingUtil.getCanonicalTestName(result);
		int count = RetryCounter.getRunCount(test);
		if (count < MAX_COUNT)
		{
			LOGGER.warn(String.format("Test '%s' FAILED! Retry %d/%d time.", test, count + 1, MAX_COUNT));
			RetryCounter.incrementRunCount(test);
			result.setStatus(ITestResult.SKIP);
			ReportContext.removeTestReport(test);
		}
		else
		{
			LOGGER.error("Retry limit exceeded for " + result.getName());
			String screenId = "";
			try
			{
				WebDriver driver = DriverPool.getDriverByTestName(TestNamingUtil.getCanonicalTestName(result));
				screenId = Screenshot.capture(driver, true);
			}
			catch (Exception e)
			{
				LOGGER.error(e.getMessage());
			}
			String errorMessage = result.getThrowable().getMessage();
			errorMessage = errorMessage != null ? StringEscapeUtils.escapeHtml4(errorMessage) : errorMessage;
//			TestLogCollector.logToSession(DriverPool.getSessionIdByTestName(TestNamingUtil.getCanonicalTestName(result)), "TEST FAILED - " + errorMessage);
			TestLogCollector.addScreenshotComment(screenId, "TEST FAILED - " + errorMessage);
			EmailReportItemCollector.push(createTestResult(result, TestResultType.FAIL, errorMessage, result.getMethod().getDescription()));
			super.onTestFailure(result);
		}
		Reporter.setCurrentTestResult(result);
		
	}

	@Override
	public void onTestSuccess(ITestResult result)
	{
		String screenId = Screenshot.capture(DriverPool.getDriverByTestName(TestNamingUtil.getCanonicalTestName(result)));
//		TestLogCollector.logToSession(DriverPool.getSessionIdByTestName(TestNamingUtil.getCanonicalTestName(result)), "TEST PASSED!");
		TestLogCollector.addScreenshotComment(screenId, "TEST PASSED!");
		EmailReportItemCollector.push(createTestResult(result, TestResultType.PASS, null, result.getMethod().getDescription()));
		if (!Configuration.getBoolean(Parameter.KEEP_ALL_SCREENSHOTS))
		{
			ReportContext.removeTestReport(TestNamingUtil.getCanonicalTestName(result));
		}
		super.onTestSuccess(result);
	}
}