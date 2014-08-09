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
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
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

	@Override
	public void onTestStart(ITestResult result)
	{
		super.onTestStart(result);
		String sessionId = result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.SESSION_ID);
		WebDriver drv = DriverPool.getDriverBySessionId(sessionId);
		String test = TestNamingUtil.getCanonicalTestName(result);
		DriverPool.associateTestNameWithDriver(test, drv);
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
			if (MAX_COUNT != 0)
				LOGGER.error("Retry limit exceeded for " + result.getName());
			
			
			String errorMessage = "";
			Throwable thr = (Throwable) result.getTestContext().getAttribute(SpecialKeywords.INITIALIZATION_FAILURE);
			if (thr != null) {
				errorMessage = getFullStackTrace(thr);
			}
			
			if (result.getThrowable() != null) {
				errorMessage = getFullStackTrace(result.getThrowable());
			}

			TestLogCollector.addScreenshotComment(takeScreenshot(result), "TEST FAILED - " + errorMessage);
			EmailReportItemCollector.push(createTestResult(result, TestResultType.FAIL, errorMessage, result.getMethod().getDescription()));
			
			super.onTestFailure(result);
		}
		Reporter.setCurrentTestResult(result);
		
	}

	@Override
	public void onTestSkipped(ITestResult result)
	{
		String test = TestNamingUtil.getCanonicalTestName(result);
		int count = RetryCounter.getRunCount(test);
		if (count < MAX_COUNT)
		{
			LOGGER.warn(String.format("Test '%s' SKIPPED! Retry %d/%d time.", test, count + 1, MAX_COUNT));
			RetryCounter.incrementRunCount(test);
			result.setStatus(ITestResult.SKIP);
			ReportContext.removeTestReport(test);
		}
		else
		{
			if (MAX_COUNT != 0)
				LOGGER.error("Retry limit exceeded for " + result.getName());
			
			
			String errorMessage = "";
			Throwable thr = (Throwable) result.getTestContext().getAttribute(SpecialKeywords.INITIALIZATION_FAILURE);
			if (thr != null) {
				errorMessage = getFullStackTrace(thr);
			}
			
			if (result.getThrowable() != null) {
				errorMessage = getFullStackTrace(result.getThrowable());
			}
			//TestLogCollector.addScreenshotComment(takeScreenshot(result), "TEST SKIPPED - " + errorMessage);
			EmailReportItemCollector.push(createTestResult(result, TestResultType.SKIP, errorMessage, result.getMethod().getDescription()));
			
			super.onTestSkipped(result);
		}
		Reporter.setCurrentTestResult(result);
		
	}
	
	@Override
	public void onTestSuccess(ITestResult result)
	{
		String test = TestNamingUtil.getCanonicalTestName(result);
		EmailReportItemCollector.push(createTestResult(result, TestResultType.PASS, null, result.getMethod().getDescription()));


		//TestLogCollector.addScreenshotComment(takeScreenshot(result), "TEST PASSED!");

		if (!Configuration.getBoolean(Parameter.KEEP_ALL_SCREENSHOTS))
		{
			ReportContext.removeTestReport(test);
		}
		super.onTestSuccess(result);
	}
	
	private String takeScreenshot(ITestResult result) 
	{
		String screenId = "";
		String sessionId = result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.SESSION_ID);
		
		WebDriver driver = DriverPool.getDriverBySessionId(sessionId);
		
		if (driver != null) {
			screenId = Screenshot.capture(driver, true); //in case of failure make screenshot by default
		}

		
		return screenId;
	}
}