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

import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.dropbox.DropboxClient;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;
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
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

@SuppressWarnings("deprecation")
public abstract class AbstractTestListener extends TestArgsListener
{
	private final static String TEST = "TEST";
	private final static String CONFIGURATION = "CONFIGURATION";
    // Dropbox client
    DropboxClient dropboxClient;
    
    private synchronized void startItem(ITestResult result, String name){
		
		ReportContext.getBaseDir(); //create directory for logging as soon as possible
		
    	String test = TestNamingUtil.getCanonicalTestName(result);
		test = TestNamingUtil.accociateTestInfo2Thread(test, Thread.currentThread().getId());
		
		String deviceName = getDeviceName();		
		if (name.equals(TEST)) {
			Messager.TEST_STARTED.info(deviceName, test, DateUtils.now());
		} else {
			Messager.CONFIG_STARTED.info(deviceName, test, DateUtils.now());
		}		
    }
    
    private void passItem(ITestResult result, String name){
		String test = TestNamingUtil.getCanonicalTestName(result);
//		if (test.endsWith("executeBeforeTestSuite")) {
//			//exit as test folder was not created for this action
//			return;
//		}
		String deviceName = getDeviceName();
		
		if (name.equals(TEST)) {
			Messager.TEST_PASSED.info(deviceName, test, DateUtils.now());
		} else {
			Messager.CONFIG_PASSED.info(deviceName, test, DateUtils.now());
		}
		EmailReportItemCollector.push(createTestResult(result, TestResultType.PASS, null, result.getMethod().getDescription(), name.equals(CONFIGURATION)));
		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
    }
    
    private void failItem(ITestResult result, String name){
    	String test = TestNamingUtil.getCanonicalTestName(result);
//		if (test.endsWith("executeBeforeTestSuite")) {
//			//exit as test folder was not created for this action
//			return;
//		}
		String errorMessage = getFailureReason(result);
		String deviceName = getDeviceName();

    	//TODO: remove hard-coded text		
    	if (!errorMessage.contains("Skipped tests detected! Analyze logs to determine possible configuration issues.")) {
    		if (name.equals(TEST)) {
    			Messager.TEST_FAILED.info(deviceName, test, DateUtils.now(), errorMessage);
    		} else {
    			Messager.CONFIG_FAILED.info(deviceName, test, DateUtils.now(), errorMessage);
    		}		

    		EmailReportItemCollector.push(createTestResult(result, TestResultType.FAIL, errorMessage, result.getMethod().getDescription(), name.equals(CONFIGURATION)));    		
    	}

		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
    }
 
    private void skipItem(ITestResult result, String name){
    	String test = TestNamingUtil.getCanonicalTestName(result);
//		if (test.endsWith("executeBeforeTestSuite")) {
//			//exit as test folder was not created for this action
//			return;
//		}
		String errorMessage = getFailureReason(result);
		String deviceName = getDeviceName();
		
		if (name.equals(TEST)) {
			Messager.TEST_SKIPPED.info(deviceName, test, DateUtils.now(), errorMessage);
		} else {
			Messager.CONFIG_SKIPPED.info(deviceName, test, DateUtils.now(), errorMessage);
		}			
		EmailReportItemCollector.push(createTestResult(result, TestResultType.SKIP, errorMessage, result.getMethod().getDescription(), name.equals(CONFIGURATION)));
		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
    }
    
    protected String getDeviceName() {
    	String deviceName = "";
    	Device device = DevicePool.getDevice();
    	if (device != null) {
    		deviceName = device.getName();
    	}
    	return deviceName;
    }
    
    @Override
    public void beforeConfiguration(ITestResult result) {
   		startItem(result, CONFIGURATION);
    }
    
    @Override
    public void onConfigurationSuccess(ITestResult result) {
   		passItem(result, CONFIGURATION);
    }
    
    @Override
    public void onConfigurationSkip(ITestResult result) {
   		skipItem(result, CONFIGURATION);
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
    	failItem(result, CONFIGURATION);
    }
    
	@Override
	public void onStart(ITestContext context)
	{
		context.setAttribute(SpecialKeywords.UUID, StringGenerator.generateNumeric(8));
		//dropbox client initialization 
	    if (!Configuration.get(Parameter.DROPBOX_ACCESS_TOKEN).isEmpty())
	    {
	    	dropboxClient = new DropboxClient(Configuration.get(Parameter.DROPBOX_ACCESS_TOKEN));
	    }
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
				DSBean dsBean = new DSBean(result.getTestContext());
				int index = 0;
				for (String arg : dsBean.getArgs())
				{
					dsBean.getTestParams().put(arg, (String) result.getParameters()[index++]);
				}
				result.getTestContext().getCurrentXmlTest().setParameters(dsBean.getTestParams());

			}
		}				

		String test = TestNamingUtil.getCanonicalTestName(result);
		RetryCounter.initCounter(test);

		startItem(result, TEST);
	}

	@Override
	public void onTestSuccess(ITestResult result)
	{
		passItem(result, TEST);
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
			failItem(result, TEST);
		}
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
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
			skipItem(result, TEST);
		}
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
		super.onTestSkipped(result);
	}
	
	@Override
	public void onFinish(ITestContext context)
	{
		ZafiraIntegrator.finishSuite();
		//removeIncorrectlyFailedTests(testContext);
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

	protected TestResultItem createTestResult(ITestResult result, TestResultType resultType, String failReason, String description, boolean config)
	{
		String group = TestNamingUtil.getPackageName(result);
		String test = TestNamingUtil.getCanonicalTestName(result);
		String linkToLog = ReportContext.getTestLogLink(test);
		String linkToVideo = ReportContext.getTestVideoLink(test);
		//String linkToScreenshots = ReportContext.getTestScreenshotsLink(testName);
		String linkToScreenshots = null;

		if(!FileUtils.listFiles(ReportContext.getTestDir(test), new String[]{"png"}, false).isEmpty()){
			if (TestResultType.PASS.equals(resultType) && !Configuration.getBoolean(Parameter.KEEP_ALL_SCREENSHOTS)) {
				//TODO: remove physically all screenshots if test/config pass and KEEP_ALL_SCREENSHOTS=false to improve cooperation with CI tools
				ReportContext.removeTestScreenshots(test);
			}
			else {
				linkToScreenshots = ReportContext.getTestScreenshotsLink(test);
			}
		}
		TestResultItem testResultItem = new TestResultItem(group, test, resultType, linkToScreenshots, linkToLog, linkToVideo, failReason, config);
		testResultItem.setDescription(description);
		if (!resultType.equals(TestResultType.PASS)) {
			testResultItem.setJiraTickets(Jira.getTickets(result));
		}
		return testResultItem;
	}
	
	protected String getFailureReason(ITestResult result) {
		String errorMessage = "";
		String message = "";
		
		
		if (result.getThrowable() != null) {
			Throwable thr = result.getThrowable();
			errorMessage = getFullStackTrace(thr);
			message = thr.getMessage();
			result.getTestContext().setAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE, message);
		}
		
		return errorMessage;
	}
	
	private String getFullStackTrace(Throwable thr) {
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
