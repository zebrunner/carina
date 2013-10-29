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
package com.qaprosoft.carina.core.foundation;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.performance.PerformanceTestResult;
import com.qaprosoft.carina.core.foundation.performance.PerformanceTestRunner;
import com.qaprosoft.carina.core.foundation.performance.PerformanceTestResult.Status;
import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;

public abstract class PerformanceTest extends DriverHelper implements Callable<PerformanceTestResult>
{
	private CountDownLatch latch;
	protected PerformanceTestResult performanceTestResult;
	private Map<String, String> testParams;

	public PerformanceTest(WebDriver driver, CountDownLatch latch, Map<String, String> testParams)
	{
		super(driver);
		this.latch = latch;
		this.testParams = testParams;
		this.performanceTestResult = new PerformanceTestResult(testParams.get(PerformanceTestRunner.TEST_KEY));
	}

	@Override
	public PerformanceTestResult call() throws Exception
	{
		try
		{
			performanceTestResult.startTest();
			executeTask(testParams);
			performanceTestResult.finishTest(Status.PASS, null);
		}
		catch (Throwable e)
		{
			performanceTestResult.finishTest(Status.FAIL, e.getMessage());
		}

		latch.countDown();

		LOGGER.info(performanceTestResult.toString());

		driver.quit();

		return performanceTestResult;
	}

	public abstract void executeTask(Map<String, String> testParams) throws Exception;
}