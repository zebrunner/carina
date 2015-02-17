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
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.log.TestLogCollector;
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
	private static final Logger LOGGER = Logger.getLogger(UITestListener.class);
	

	@Override
	public void onTestStart(ITestResult result) {
		super.onTestStart(result);
	}

	@Override
	public void onConfigurationFailure(ITestResult result) {
		String errorMessage = getFailureReason(result);
		TestLogCollector.addScreenshotComment(takeScreenshot(result), "CONFIGURATION FAILED - " + errorMessage);
		LOGGER.error("CONFIGURATION FAILED - " + errorMessage);
		super.onConfigurationFailure(result);
	}
	
	@Override
	public void onTestFailure(ITestResult result) {
		String errorMessage = getFailureReason(result);
		TestLogCollector.addScreenshotComment(takeScreenshot(result), "TEST FAILED - " + errorMessage);
		LOGGER.error("TEST FAILED - " + errorMessage);
		
/*		
		String test = TestNamingUtil.getCanonicalTestName(result);
		
		int count = RetryCounter.getRunCount(test);
		int maxCount = RetryAnalyzer.getMaxRetryCountForTest(result);
		LOGGER.debug("count: " + count + "; maxCount:" + maxCount);
		
		String errorMessage = getFailureReason(result);

		IRetryAnalyzer retry=result.getMethod().getRetryAnalyzer();
		if (count < maxCount && retry == null) {
			LOGGER.error("retry_count will be ignored as RetryAnalyzer is not declared for " + result.getMethod().getMethodName());
		}
		
		
		if (count < maxCount && retry != null)
		{
			//decrease counter for TestNamingUtil.testName2Counter. It should fix invCount for re-executed tests
			TestNamingUtil.decreaseRetryCounter(test);
			
			String deviceName = getDeviceName();
			if (!deviceName.isEmpty()){
				deviceName = " on " + deviceName;
			}
			LOGGER.error(String.format("Test '%s' FAILED%s! Retry %d of %d time - %s", test, deviceName, count, maxCount, errorMessage));
			LOGGER.debug("UITestListener->onTestFailure retry analyzer: " + result.getMethod().getRetryAnalyzer());

			//screenshot should be added for all cases obligatory
			TestLogCollector.addScreenshotComment(takeScreenshot(result), "TEST FAILED - " + errorMessage);

			closeLogAppender(test);

			//clean test results from failure 
			//result.getTestContext().getFailedTests().removeResult(result.getMethod());
		}
		else
		{		
			if (count > 0) {
				LOGGER.error("Retry limit exceeded for " + result.getName());
			}
	
			//screenshot should be added for all cases obligatory
			TestLogCollector.addScreenshotComment(takeScreenshot(result), "TEST FAILED - " + errorMessage);
			LOGGER.debug("count >= maxCount: onTestFailure listener finished successfully.");
			super.onTestFailure(result);
		}*/
	
		//Reporter.setCurrentTestResult(result);
		LOGGER.debug("onTestFailure listener finished successfully.");
		super.onTestFailure(result);		
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		//retry logic shouldn't work for Skipped tests as DriverFactory already implemented driver initialization retry
		super.onTestSkipped(result);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		super.onTestSuccess(result);
	}

	private String takeScreenshot(ITestResult result) {
		String screenId = "";
		long threadId = Thread.currentThread().getId();
		WebDriver driver = DriverPool.getDriverByThread(threadId) != null ? DriverPool.getDriverByThread(threadId) : DriverPool.getExtraDriverByThread(threadId);
		
		if (driver != null) {
			screenId = Screenshot.capture(driver, true); // in case of failure
															// make screenshot
															// by default
		}

		return screenId;
	}

//	// cleaning of test results after retry logic work
//	public void onFinish(ITestContext testContext) {
//		super.onFinish(testContext);
//	}
}