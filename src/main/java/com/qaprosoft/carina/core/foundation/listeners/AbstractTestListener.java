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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;

import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.log.ThreadLogAppender;
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
import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.StringGenerator;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.zafira.client.model.TestType;
//import com.qaprosoft.carina.core.foundation.dropbox.DropboxClient;

@SuppressWarnings("deprecation")
public abstract class AbstractTestListener extends TestArgsListener
{
	private static final Logger LOGGER = Logger.getLogger(AbstractTestListener.class);
	
    // Dropbox client
//    DropboxClient dropboxClient;
 
    private void startItem(ITestResult result, Messager messager){
		
 		ReportContext.getBaseDir(); //create directory for logging as soon as possible
 		
     	String test = TestNamingUtil.getCanonicalTestName(result);
 		test = TestNamingUtil.associateTestInfo2Thread(test, Thread.currentThread().getId());
 		
 		String deviceName = getDeviceName();
 		messager.info(deviceName, test, DateUtils.now());
     }
    
    private void passItem(ITestResult result, Messager messager){
		String test = TestNamingUtil.getTestNameByThread();

		String deviceName = getDeviceName();
		
		messager.info(deviceName, test, DateUtils.now());
		
		EmailReportItemCollector.push(createTestResult(result, TestResultType.PASS, null, result.getMethod().getDescription(), messager.equals(Messager.CONFIG_PASSED)));
		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
    }
    
    private String failItem(ITestResult result, Messager messager){
    	String test = TestNamingUtil.getTestNameByThread();

		String errorMessage = getFailureReason(result);
		String deviceName = getDeviceName();

    	//TODO: remove hard-coded text		
    	if (!errorMessage.contains("All tests were skipped! Analyze logs to determine possible configuration issues.")) {
   			messager.info(deviceName, test, DateUtils.now(), errorMessage);
    		EmailReportItemCollector.push(createTestResult(result, TestResultType.FAIL, errorMessage, result.getMethod().getDescription(), messager.equals(Messager.CONFIG_FAILED)));    		
    	}

		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		return errorMessage;
    }
    
    private String failRetryItem(ITestResult result, Messager messager, int count, int maxCount){
    	String test = TestNamingUtil.getTestNameByThread();

		String errorMessage = getFailureReason(result);
		String deviceName = getDeviceName();

		messager.info(deviceName, test, String.valueOf(count), String.valueOf(maxCount), errorMessage);

		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		return errorMessage;
    }    
 
    private String skipItem(ITestResult result, Messager messager){
    	String test = TestNamingUtil.getTestNameByThread();

		String errorMessage = getFailureReason(result);
		String deviceName = getDeviceName();
		
		messager.info(deviceName, test, DateUtils.now(), errorMessage);
		
		EmailReportItemCollector.push(createTestResult(result, TestResultType.SKIP, errorMessage, result.getMethod().getDescription(), messager.equals(Messager.CONFIG_SKIPPED)));
		
		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		return errorMessage;
    }
    
    private String getDeviceName() {
    	String deviceName = "";
    	Device device = DevicePool.getDevice();
    	if (device != null) {
    		deviceName = device.getName();
    		String udid = device.getUdid();
    		if (udid != null) {
    			if (!udid.isEmpty()) {
    				deviceName = deviceName + " - " + udid;
    			}
    		}
    	}
    	
    	return deviceName;
    }
    
    @Override
    public void beforeConfiguration(ITestResult result) {
   		startItem(result, Messager.CONFIG_STARTED);
		// do failure test cleanup in this place as right after the test 
		// context doesn't have up-to-date information. 
   		// This context cleanup is required to launch dependent steps if parent method pass from Nth retry!
		removeIncorrectlyFailedTests(result.getTestContext());
   		super.beforeConfiguration(result);
    }
    
    @Override
    public void onConfigurationSuccess(ITestResult result) {
   		passItem(result, Messager.CONFIG_PASSED);
   		TestNamingUtil.releaseTestInfoByThread();
   		super.onConfigurationSuccess(result);
    }
    
    @Override
    public void onConfigurationSkip(ITestResult result) {
   		skipItem(result, Messager.CONFIG_SKIPPED);
   		TestNamingUtil.releaseTestInfoByThread();
   		super.onConfigurationSkip(result);
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
    	failItem(result, Messager.CONFIG_FAILED);
		String test = TestNamingUtil.getTestNameByThread();
		closeLogAppender(test);
		TestNamingUtil.releaseTestInfoByThread();
		super.onConfigurationFailure(result);
    }
    
	@Override
	public void onStart(ITestContext context)
	{
		String uuid = StringGenerator.generateNumeric(8);
		ParameterGenerator.setUUID(uuid);
		
/*		//dropbox client initialization 
	    if (!Configuration.get(Parameter.DROPBOX_ACCESS_TOKEN).isEmpty())
	    {
	    	dropboxClient = new DropboxClient(Configuration.get(Parameter.DROPBOX_ACCESS_TOKEN));
	    }*/
	    super.onStart(context);
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
		
		startItem(result, Messager.TEST_STARTED);
		
		// Analyze Zafira results for re-run
		if (ZafiraIntegrator.isRerun()) {
			// Analyze TestResult status obligatory inside isrerun if operator because
			// some modifications in Zafira results needed.
			// FAILED status will be removed/unregistered from Zafira database
			TestType testType = ZafiraIntegrator.getTestType();
			
			// if null it means that new test appeared in comparison with registered result
			if (testType != null) {
				if (testType.getStatus().name().equals(SpecialKeywords.PASSED)) {
					// generate already passed exception. Regular eception
					// doesn't work as it stop DataProvider execution etc
					throw new SkipException(SpecialKeywords.ALREADY_PASSED);
				} else {
					// unregister Zafira test result
					ZafiraIntegrator.deleteTest(testType.getId());
				}
			} else {
				LOGGER.warn("New test '" + test + "' appeared which was not registered in Zafira during last execution!");
			}
		}
	}

	@Override
	public void onTestSuccess(ITestResult result)
	{
		passItem(result, Messager.TEST_PASSED);

		ZafiraIntegrator.finishTestMethod(result, null);
		TestNamingUtil.releaseTestInfoByThread();
		super.onTestSuccess(result);
	}

	@Override
	public void onTestFailure(ITestResult result)
	{
		String test = TestNamingUtil.getTestNameByThread();
		int count = RetryCounter.getRunCount(test);		
		int maxCount = RetryAnalyzer.getMaxRetryCountForTest(result);
		LOGGER.debug("count: " + count + "; maxCount:" + maxCount);

		IRetryAnalyzer retry=result.getMethod().getRetryAnalyzer();
		if (count < maxCount && retry == null) {
			LOGGER.error("retry_count will be ignored as RetryAnalyzer is not declared for " + result.getMethod().getMethodName());
		}
		
		String errorMessage = "";
		if (count < maxCount && retry != null && !Jira.isRetryDisabled(result))
		{
			TestNamingUtil.decreaseRetryCounter(test);
			errorMessage = failRetryItem(result, Messager.RETRY_RETRY_FAILED, count, maxCount);
		} else {
			errorMessage = failItem(result, Messager.TEST_FAILED);
			closeLogAppender(test);
		}

		//register test details for zafira data population
    	ZafiraIntegrator.finishTestMethod(result, errorMessage);
		
		TestNamingUtil.releaseTestInfoByThread();
		super.onTestFailure(result);
	}
	
	@Override
	public void onTestSkipped(ITestResult result)
	{
		//handle Zafira already passed exception for re-run and do nothing. maybe return should be enough
		if (result.getThrowable().getMessage().equals(SpecialKeywords.ALREADY_PASSED)) {
			// [VD] it is prohibited to release TestInfoByThread in this place.!
			return;
		}
		
		String errorMessage= skipItem(result, Messager.TEST_SKIPPED);
    	ZafiraIntegrator.finishTestMethod(result, errorMessage);
		TestNamingUtil.releaseTestInfoByThread();
		super.onTestSkipped(result);
	}
	
	@Override
	public void onFinish(ITestContext context)
	{
		removeIncorrectlyFailedTests(context);
		//printContextTestsSummary(context);
		super.onFinish(context);
	}

	/**
	 * When the test is restarted this method cleans fail statistics in test
	 * context.
	 *
     */
	private void removeIncorrectlyFailedTests(ITestContext context) {
		// List of test results which we will delete later
		List<ITestResult> testsToBeRemoved = new ArrayList<>();

		// collect all id's from passed test
		Set<Long> passedTestIds = new HashSet<>();
		for (ITestResult passedTest : context.getPassedTests().getAllResults()) {
			//adding passed test
			long passedTestId = getMethodId(passedTest);
			LOGGER.debug("Adding passedTest info: " + passedTestId + "; " + passedTest.getName());
			passedTestIds.add(passedTestId);
		}

		LOGGER.debug("---------------- ANALYZE FAILED RESULTS FOR DUPLICATES -----------------------");
		
		Set<Long> failedTestIds = new HashSet<>();
		for (ITestResult failedTest : context.getFailedTests().getAllResults()) {

			// id = class + method + dataprovider
			long failedTestId = getMethodId(failedTest);

			// if we saw this test as a failed test before we mark as to be deleted
			// or delete this failed test if there is at least one passed version
			if (failedTestIds.contains(failedTestId)
					|| passedTestIds.contains(failedTestId)) {
				LOGGER.debug("Test to be removed from context: " + failedTestId + "; " + failedTest.getName());
				testsToBeRemoved.add(failedTest);
			} else {
				LOGGER.debug("Test to mark as failed: " + failedTestId + "; " + failedTest.getName());
				failedTestIds.add(failedTestId);
			}
		}
		
		LOGGER.debug("---------------- REMOVE DUPLICATES FAILURES -----------------------");
		// finally delete all tests that are marked for removal
		for (Iterator<ITestResult> iterator = context.getFailedTests()
				.getAllResults().iterator(); iterator.hasNext();) {
			ITestResult testResult = iterator.next();
			if (testsToBeRemoved.contains(testResult)) {
				LOGGER.debug("Removing test from context: " + testResult.getName());
				iterator.remove();
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void printContextTestsSummary(ITestContext context) {
		LOGGER.debug("getAllTestMethods length: " + context.getAllTestMethods().length);
		LOGGER.debug("---------------- PRINT SUMMARIZED SUCCESS -----------------------");
		// print messages about all tests in context
		LOGGER.debug("passed tests size: " + context.getPassedTests().getAllResults().size());
		for (Iterator<ITestResult> iterator = context.getPassedTests()
				.getAllResults().iterator(); iterator.hasNext();) {
			ITestResult testResult = iterator.next();
			
			long testId = getMethodId(testResult);
			LOGGER.debug("Pass test in context: " + testId + "; " 
						+ testResult.getName());
		}
		
		LOGGER.debug("---------------- PRINT SUMMARIZED FAILURE -----------------------");
		// print messages about all tests in context
		LOGGER.debug("failed tests size: " + context.getFailedTests().getAllResults().size());
		for (Iterator<ITestResult> iterator = context.getFailedTests()
				.getAllResults().iterator(); iterator.hasNext();) {
			ITestResult testResult = iterator.next();
			
			long testId = getMethodId(testResult);
			LOGGER.debug("Failed test in context: " + testId + "; " 
						+ testResult.getName());
		}
		
		LOGGER.debug("---------------- PRINT SUMMARIZED SKIP -----------------------");
		// print messages about all tests in context
		LOGGER.debug("skipped tests size: " + context.getSkippedTests().getAllResults().size());
		for (Iterator<ITestResult> iterator = context.getSkippedTests()
				.getAllResults().iterator(); iterator.hasNext();) {
			ITestResult testResult = iterator.next();
			
			long testId = getMethodId(testResult);
			LOGGER.debug("Skipped test in context: " + testId + "; " 
						+ testResult.getName());
		}
		
		LOGGER.debug("---------------- PRINT SUMMARIZED CONFIGURATION SUCCESS -----------------------");
		LOGGER.debug("passed configurations size: " + context.getPassedConfigurations().getAllResults().size());
		for (Iterator<ITestResult> iterator = context.getPassedConfigurations()
				.getAllResults().iterator(); iterator.hasNext();) {
			ITestResult testResult = iterator.next();
			
			long testId = getMethodId(testResult);
			LOGGER.debug("passed configurations in context: " + testId + "; " 
						+ testResult.getName());
		}
		
		LOGGER.debug("---------------- PRINT SUMMARIZED CONFIGURATION FAILURE -----------------------");
		LOGGER.debug("failed configurations size: " + context.getFailedConfigurations().getAllResults().size());
		for (Iterator<ITestResult> iterator = context.getFailedConfigurations()
				.getAllResults().iterator(); iterator.hasNext();) {
			ITestResult testResult = iterator.next();
			
			long testId = getMethodId(testResult);
			LOGGER.debug("failed configurations in context: " + testId + "; " 
						+ testResult.getName());
		}
		
		LOGGER.debug("---------------- PRINT SUMMARIZED CONFIGURATION SKIP -----------------------");
		LOGGER.debug("skipped configurations size: " + context.getSkippedConfigurations().getAllResults().size());
		for (Iterator<ITestResult> iterator = context.getSkippedConfigurations()
				.getAllResults().iterator(); iterator.hasNext();) {
			ITestResult testResult = iterator.next();
			
			long testId = getMethodId(testResult);
			LOGGER.debug("failed configurations in context: " + testId + "; " 
						+ testResult.getName());
		}
	}
	
	private long getMethodId(ITestResult result) {
		long id = result.getTestClass().getName().hashCode();
		id = 31 * id + result.getMethod().getMethodName().hashCode();
		id = 31
				* id
				+ (result.getParameters() != null ? Arrays.hashCode(result
						.getParameters()) : 0);
		//LOGGER.debug("Calculated id for " + result.getMethod().getMethodName() + " is " + id);
		return id;
	}

	protected TestResultItem createTestResult(ITestResult result, TestResultType resultType, String failReason, String description, boolean config)
	{
		String group = TestNamingUtil.getPackageName(result);
		String test = TestNamingUtil.getTestNameByThread();
		String linkToLog = ReportContext.getTestLogLink(test);
		String linkToVideo = ReportContext.getTestVideoLink(test);
		//String linkToScreenshots = ReportContext.getTestScreenshotsLink(testName);
		String linkToScreenshots = null;

		if(!FileUtils.listFiles(ReportContext.getTestDir(test), new String[]{"png"}, false).isEmpty()){
			if (TestResultType.PASS.equals(resultType) && !Configuration.getBoolean(Parameter.KEEP_ALL_SCREENSHOTS)) {
				//remove physically all screenshots if test/config pass and KEEP_ALL_SCREENSHOTS=false to improve cooperation with CI tools
				ReportContext.removeTestScreenshots(test);
			}
			else {
				linkToScreenshots = ReportContext.getTestScreenshotsLink(test);
			}
		}
		TestResultItem testResultItem = new TestResultItem(group, test, resultType, linkToScreenshots, linkToLog, linkToVideo, failReason, config);
		testResultItem.setDescription(description);
		//AUTO-1081 eTAF report does not show linked Jira tickets if test PASSED
		//jira tickets should be used for tracking tasks. application issues will be tracked by planned zafira feature
		testResultItem.setJiraTickets(Jira.getTickets(result));
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
	
	private void closeLogAppender(String test)
	{
		try {
			ThreadLogAppender tla = (ThreadLogAppender) Logger.getRootLogger().getAppender("ThreadLogAppender");
			if(tla != null)
			{
				tla.closeResource(test);
			}
		}
		catch (Exception e) {
			LOGGER.error("close log appender was not successful.");
			e.printStackTrace();
		}
	}
}
