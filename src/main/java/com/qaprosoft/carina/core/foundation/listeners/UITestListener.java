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

}