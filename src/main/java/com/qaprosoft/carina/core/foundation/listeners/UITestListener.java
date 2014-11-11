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

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.qaprosoft.carina.core.foundation.log.TestLogCollector;
import com.qaprosoft.carina.core.foundation.log.ThreadLogAppender;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;
import com.qaprosoft.carina.core.foundation.retry.RetryAnalyzer;
import com.qaprosoft.carina.core.foundation.retry.RetryCounter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;

/**
 * Listener that controls retry logic for test according to retry_count
 * configuration attribute. Also it generates test result item if test passed or
 * retry limit is exceed.
 * 
 * @author Alex Khursevich
 */

public class UITestListener extends AbstractTestListener {
	// private static final Logger LOGGER =
	// Logger.getLogger(UITestListener.class);
	private static final Logger LOGGER = Logger.getLogger(UITestListener.class);
	

	@Override
	public void onTestStart(ITestResult result) {
		super.onTestStart(result);
		String sessionId = result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.SESSION_ID);
		WebDriver drv = DriverPool.getDriverBySessionId(sessionId);
		String test = TestNamingUtil.getCanonicalTestName(result);
		DriverPool.associateTestNameWithDriver(test, drv);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		String test = TestNamingUtil.getCanonicalTestName(result);
		
		int count = RetryCounter.getRunCount(test);
		int maxCount = RetryAnalyzer.getMaxRetryCountForTest(result);
		LOGGER.debug("count: " + count + "; maxCount:" + maxCount);
		
		String errorMessage = "";
		Throwable thr = (Throwable) result.getTestContext().getAttribute(SpecialKeywords.INITIALIZATION_FAILURE);
		if (thr != null) {
			errorMessage = getFullStackTrace(thr);
		}

		if (result.getThrowable() != null) {
			errorMessage = getFullStackTrace(result.getThrowable());
		}
		
		IRetryAnalyzer retry=result.getMethod().getRetryAnalyzer();
		if (count < maxCount && retry == null) {
			LOGGER.error("retry_count will be ignored as RetryAnalyzer is not declared for " + result.getMethod().getMethodName());
		}
		
		
		if (count < maxCount && retry != null)
		{
			LOGGER.error(String.format("Test '%s' FAILED! Retry %d of %d time - %s", test, count, maxCount, errorMessage));
			LOGGER.debug("UITestListener->onTestFailure retry analyzer: " + result.getMethod().getRetryAnalyzer());
			
			RetryCounter.incrementRunCount(test);
			//temporary wrap into try/catch to analyze any possible failures with extended logging
			try {
				ThreadLogAppender tla = (ThreadLogAppender) Logger.getRootLogger().getAppender("ThreadLogAppender");
				if(tla != null)
				{
					tla.closeResource(test);
				}
				LOGGER.debug("count < maxCount: onTestFailure listener finished successfully.");
			}
			catch (Exception e) {
				LOGGER.error("onTestFailure listener was not successful.");
				e.printStackTrace();
			}
			//ReportContext.removeTestReport(test);			
		}
		else
		{		
			if (count > 0) {
				LOGGER.error("Retry limit exceeded for " + result.getName());
			}
	
			TestLogCollector.addScreenshotComment(takeScreenshot(result), "TEST FAILED - " + errorMessage);
			EmailReportItemCollector.push(createTestResult(result, TestResultType.FAIL, errorMessage, result.getMethod().getDescription()));
			super.onTestFailure(result);
			LOGGER.debug("count >= maxCount: onTestFailure listener finished successfully.");
		}
		Reporter.setCurrentTestResult(result);
		LOGGER.debug("onTestFailure listener finished successfully.");
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		//retry logic shouldn't work for Skipped tests as DriverFactory already implemented driver initializarion retry
		String errorMessage = "";
		Throwable thr = (Throwable) result.getTestContext().getAttribute(SpecialKeywords.INITIALIZATION_FAILURE);
		if (thr != null) {
			errorMessage = getFullStackTrace(thr);
		}

		if (result.getThrowable() != null) {
			errorMessage = getFullStackTrace(result.getThrowable());
		}
		
		// TestLogCollector.addScreenshotComment(takeScreenshot(result),
		// "TEST SKIPPED - " + errorMessage);
		EmailReportItemCollector.push(createTestResult(result, TestResultType.SKIP, errorMessage, result.getMethod().getDescription()));
		super.onTestSkipped(result);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
//		String test = TestNamingUtil.getCanonicalTestName(result);
		EmailReportItemCollector.push(createTestResult(result, TestResultType.PASS, null, result.getMethod().getDescription()));

		super.onTestSuccess(result);
	}

	private String takeScreenshot(ITestResult result) {
		String screenId = "";
		String sessionId = result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.SESSION_ID);

		WebDriver driver = DriverPool.getDriverBySessionId(sessionId);

		if (driver != null) {
			screenId = Screenshot.capture(driver, true); // in case of failure
															// make screenshot
															// by default
		}

		return screenId;
	}

	// cleaning of test results after retry logic work
	public void onFinish(ITestContext testContext) {
		super.onFinish(testContext);

/*		// List of test results which we will delete later
		List<ITestResult> testsToBeRemoved = new ArrayList<ITestResult>();

		// collect all id's from passed test
		Set<Integer> passedTestIds = new HashSet<Integer>();
		for (ITestResult passedTest : testContext.getPassedTests().getAllResults()) {
			passedTestIds.add(TestUtil.getId(passedTest));
		}

		Set<Integer> failedTestIds = new HashSet<Integer>();
		for (ITestResult failedTest : testContext.getFailedTests().getAllResults()) {

			// id = class + method + dataprovider
			int failedTestId = TestUtil.getId(failedTest);

			// if we saw this test as a failed test before we mark as to be deleted
			// or delete this failed test if there is at least one passed version
			if (failedTestIds.contains(failedTestId) || passedTestIds.contains(failedTestId)) {
				testsToBeRemoved.add(failedTest);
			} else {
				failedTestIds.add(failedTestId);
			}
		}

		// finally delete all tests that are marked
		for (Iterator<ITestResult> iterator = testContext.getFailedTests().getAllResults().iterator(); iterator.hasNext();) {
			ITestResult testResult = iterator.next();
			if (testsToBeRemoved.contains(testResult)) {
				iterator.remove();
			}
		}*/
	}

	public static class TestUtil {
		public static int getId(ITestResult result) {
			int id = result.getTestClass().getName().hashCode();
			id = 31 * id + result.getMethod().getMethodName().hashCode();
			id = 31 * id + (result.getParameters() != null ? Arrays.hashCode(result.getParameters()) : 0);
			return id;
		}
	}
}