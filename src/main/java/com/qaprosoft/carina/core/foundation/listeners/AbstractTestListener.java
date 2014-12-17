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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.dropbox.DropboxClient;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.log.GlobalTestLog;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.spira.SpiraTestIntegrator;
import com.qaprosoft.carina.core.foundation.report.zafira.ZafiraIntegrator;
import com.qaprosoft.carina.core.foundation.retry.RetryAnalyzer;
import com.qaprosoft.carina.core.foundation.retry.RetryCounter;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.DateUtils;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.StringGenerator;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.utils.parser.XLSDSBean;

@SuppressWarnings("deprecation")
public abstract class AbstractTestListener extends TestArgsListener
{
    // Dropbox client
    DropboxClient dropboxClient;	
	
	@Override
	public void onStart(ITestContext context)
	{
		context.setAttribute(SpecialKeywords.UUID, StringGenerator.generateNumeric(8));
		//dropbox client initialization 
	    if (!Configuration.get(Parameter.DROPBOX_ACCESS_TOKEN).isEmpty())
	    {
	    	dropboxClient = new DropboxClient(Configuration.get(Parameter.DROPBOX_ACCESS_TOKEN));
	    }
	    
	    ZafiraIntegrator.startSuite(context);
	}
	
	@Override
	public void onTestStart(ITestResult result)
	{
		super.onTestStart(result);
		
		if (!result.getTestContext().getCurrentXmlTest().getTestParameters().containsKey(SpecialKeywords.EXCEL_DS_CUSTOM_PROVIDER) &&
				result.getParameters().length > 0) //set parameters from XLS only if test contains any parameter at all)
		{
			if (result.getTestContext().getCurrentXmlTest().getTestParameters().containsKey(SpecialKeywords.EXCEL_DS_ARGS))
			{				
				XLSDSBean dsBean = new XLSDSBean(result.getTestContext());
				int index = 0;
				for (String arg : dsBean.getArgs())
				{
					dsBean.getTestParams().put(arg, (String) result.getParameters()[index++]);
				}
				result.getTestContext().getCurrentXmlTest().setParameters(dsBean.getTestParams());

			}
		}

		result.setAttribute(GlobalTestLog.KEY, new GlobalTestLog());
		
		String test = TestNamingUtil.getCanonicalTestName(result);
		TestNamingUtil.accociateTest2Thread(test, Thread.currentThread().getId());
		TestNamingUtil.accociateTestStartDate(test);
		
		RetryCounter.initCounter(test);

		Messager.TEST_STARTED.info(test, DateUtils.now());
	}

	@Override
	public void onTestSuccess(ITestResult result)
	{
		String test = TestNamingUtil.getCanonicalTestName(result);
		Messager.TEST_PASSED.info(test, DateUtils.now());
		
	    //Spira test steps integration
	    SpiraTestIntegrator.logTestStepsInfo(result);	

	    ZafiraIntegrator.finishTestMethod(result, com.qaprosoft.zafira.client.model.TestType.Status.PASSED, "");
		super.onTestSuccess(result);
	}

	@Override
	public void onTestFailure(ITestResult result)
	{
		String test = TestNamingUtil.getCanonicalTestName(result);
		int count = RetryCounter.getRunCount(test);		
		int maxCount = RetryAnalyzer.getMaxRetryCountForTest(result);
		
		if (count >= maxCount && result.getThrowable().getMessage() != null)
		{
			String errorMessage = "";
			Throwable thr = (Throwable) result.getTestContext().getAttribute(SpecialKeywords.INITIALIZATION_FAILURE);
			if (thr != null) {
				errorMessage = getFullStackTrace(thr);
			}
			
			if (result.getThrowable() != null) {
				thr = result.getThrowable();
				errorMessage = getFullStackTrace(thr);
			}			
			

			Messager.TEST_FAILED.error(test, DateUtils.now(), errorMessage);
			
			SpiraTestIntegrator.logTestStepsInfo(result, thr);
			
			ZafiraIntegrator.finishTestMethod(result, com.qaprosoft.zafira.client.model.TestType.Status.FAILED, errorMessage);
		}
		super.onTestFailure(result);
	}
	
	@Override
	public void onTestSkipped(ITestResult result)
	{
		String test = TestNamingUtil.getCanonicalTestName(result);
		int count = RetryCounter.getRunCount(test);
		
		int maxCount = RetryAnalyzer.getMaxRetryCountForTest(result);
		if (count >= maxCount)
		{
			String errorMessage = "";
			Throwable thr = (Throwable) result.getTestContext().getAttribute(SpecialKeywords.INITIALIZATION_FAILURE);
			if (thr != null) {
				//errorMessage = thr.getMessage();
				errorMessage = getFullStackTrace(thr);
			}
			if (result.getThrowable() != null) {
				thr = result.getThrowable();
				errorMessage = getFullStackTrace(thr);
			}			
			
			Messager.TEST_SKIPPED.error(test, DateUtils.now(), errorMessage);

			ZafiraIntegrator.finishTestMethod(result, com.qaprosoft.zafira.client.model.TestType.Status.SKIPPED, errorMessage);
		}
		super.onTestSkipped(result);
	}
	
	@Override
	public void onFinish(ITestContext context)
	{
		ZafiraIntegrator.finishSuite();
		//removeIncorrectlyFailedTests(testContext);
		TestNamingUtil.releaseTestFromThread(Thread.currentThread().getId());
		super.onFinish(context);
	}

	/**
	 * When the test is restarted this method cleans fail statistics in test
	 * context.
	 * 
	 * @param test
	 *            - test context.
	 */
	public static void removeIncorrectlyFailedTests(ITestContext test)
	{
		IResultMap failedTests = test.getFailedTests();
		IResultMap skippedTests = test.getSkippedTests();

		int countOfSkippedResults = test.getSkippedTests().getAllResults().size();
		int countOfPassedResults = test.getPassedTests().getAllResults().size();
		int countOfFailedResults = test.getFailedTests().getAllResults().size();
		int countOfAllResults = test.getAllTestMethods().length;

		List<ITestNGMethod> failsToRemove = new ArrayList<ITestNGMethod>();
		for (ITestResult result : test.getFailedTests().getAllResults())
		{
			long failedResultTime = result.getEndMillis();

			for (ITestResult resultToCheck : test.getSkippedTests().getAllResults())
			{
				if (failedResultTime == resultToCheck.getEndMillis())
				{
					failsToRemove.add(resultToCheck.getMethod());
					break;
				}
			}

			for (ITestResult resultToCheck : test.getPassedTests().getAllResults())
			{
				if (failedResultTime == resultToCheck.getEndMillis())
				{
					failsToRemove.add(resultToCheck.getMethod());
					break;
				}
			}
		}

		for (ITestNGMethod method : failsToRemove)
		{
			failedTests.removeResult(method);
		}

		// remove rerun skipped tests
		if ((countOfAllResults == countOfPassedResults || countOfAllResults == countOfFailedResults) && countOfSkippedResults != 0)
		{
			for (ITestNGMethod method : test.getSkippedTests().getAllMethods())
			{
				skippedTests.removeResult(method);
			}
		}
	}

	protected TestResultItem createTestResult(ITestResult result, TestResultType resultType, String failReason, String description)
	{
		String group = TestNamingUtil.getPackageName(result);
		String testName = TestNamingUtil.getCanonicalTestName(result);
		String linkToLog = ReportContext.getTestLogLink(testName);
		String linkToVideo = ReportContext.getTestVideoLink(testName);
		//String linkToScreenshots = ReportContext.getTestScreenshotsLink(testName);
		String linkToScreenshots = null;
		if(!FileUtils.listFiles(ReportContext.getTestDir(testName), new String[]{"png"}, false).isEmpty()
			&& Configuration.getBoolean(Parameter.AUTO_SCREENSHOT)
			&& (TestResultType.FAIL.equals(resultType) || Configuration.getBoolean(Parameter.KEEP_ALL_SCREENSHOTS)))
		{
			linkToScreenshots = ReportContext.getTestScreenshotsLink(testName);
		}
		TestResultItem testResultItem = new TestResultItem(group, testName, resultType, linkToScreenshots, linkToLog, linkToVideo, failReason);
		testResultItem.setDescription(description);
		if (!resultType.equals(TestResultType.PASS)) {
			testResultItem.setJiraTickets(Jira.getTickets(result));
		}
		return testResultItem;
	}
	
	protected String getFullStackTrace(Throwable thr) {
		String stackTrace = "";
		
	    if (thr != null) {
	    	stackTrace = thr.getMessage() + "\n";
	    	
            StackTraceElement[] elems = thr.getStackTrace();
	        for (StackTraceElement elem : elems) {
	        	stackTrace = stackTrace + "\n" + elem.toString();
            }
	    }
	    return stackTrace;
	}	
}
