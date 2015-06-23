/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
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

import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;

/*
 * API test listener is responsible for log initialization/finalization and test result generation.
 * 
 * @author Alex Khursevich
 */
public class APITestListener extends AbstractTestListener
{

	@Override
	public void onTestStart(ITestResult result)
	{
		super.onTestStart(result);
	}

	@Override
	public void onTestSuccess(ITestResult result)
	{
		EmailReportItemCollector.push(createTestResult(result, TestResultType.PASS, null, result.getMethod().getDescription(), false));
		super.onTestSuccess(result);
	}

	@Override
	public void onTestFailure(ITestResult result)
	{
		String errorMessage = getFailureReason(result);

		EmailReportItemCollector.push(createTestResult(result, TestResultType.FAIL, errorMessage, result.getMethod().getDescription(), false));
		super.onTestFailure(result);
	}
	
	@Override
	public void onTestSkipped(ITestResult result)
	{
		String errorMessage = getFailureReason(result);

		EmailReportItemCollector.push(createTestResult(result, TestResultType.SKIP, errorMessage, result.getMethod().getDescription(), false));		
		super.onTestSkipped(result);
	}
}
