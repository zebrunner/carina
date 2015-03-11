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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.dropbox.DropboxClient;
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
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.StringGenerator;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

@SuppressWarnings("deprecation")
public abstract class AbstractTestListener extends TestArgsListener
{
	private static final Logger LOGGER = Logger.getLogger(AbstractTestListener.class);
	
    // Dropbox client
    DropboxClient dropboxClient;
 
    private void startItem(ITestResult result, Messager messager){
		
 		ReportContext.getBaseDir(); //create directory for logging as soon as possible
 		
     	String test = TestNamingUtil.getCanonicalTestName(result);
 		test = TestNamingUtil.accociateTestInfo2Thread(test, Thread.currentThread().getId());
 		
 		String deviceName = getDeviceName();
 		messager.info(deviceName, test, DateUtils.now());
     }
    
    private void passItem(ITestResult result, Messager messager, boolean testItem){
		String test = TestNamingUtil.getCanonicalTestName(result);

		String deviceName = getDeviceName();
		
		messager.info(deviceName, test, DateUtils.now());
		
		EmailReportItemCollector.push(createTestResult(result, TestResultType.PASS, null, result.getMethod().getDescription(), messager.equals(Messager.CONFIG_PASSED)));
		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		
		if (testItem) {
			ZafiraIntegrator.finishTestMethod(result, null);
		}
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
    }
    
    private void failItem(ITestResult result, Messager messager, boolean testItem){
    	String test = TestNamingUtil.getCanonicalTestName(result);

		String errorMessage = getFailureReason(result);
		String deviceName = getDeviceName();

    	//TODO: remove hard-coded text		
    	if (!errorMessage.contains("All tests were skipped! Analyze logs to determine possible configuration issues.")) {
   			messager.info(deviceName, test, DateUtils.now(), errorMessage);
    		EmailReportItemCollector.push(createTestResult(result, TestResultType.FAIL, errorMessage, result.getMethod().getDescription(), messager.equals(Messager.CONFIG_FAILED)));    		
    	}

		if (testItem) {
	    	ZafiraIntegrator.finishTestMethod(result, errorMessage);
		}


    	
		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
    }
    
    private void failRetryItem(ITestResult result, Messager messager, boolean testItem, int count, int maxCount){
    	String test = TestNamingUtil.getCanonicalTestName(result);

		String errorMessage = getFailureReason(result);
		String deviceName = getDeviceName();

		messager.info(deviceName, test, String.valueOf(count), String.valueOf(maxCount), errorMessage);

		if (testItem) {
	    	ZafiraIntegrator.finishTestMethod(result, errorMessage);
		}
		
		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
    }    
 
    private void skipItem(ITestResult result, Messager messager, boolean testItem){
    	String test = TestNamingUtil.getCanonicalTestName(result);

		String errorMessage = getFailureReason(result);
		String deviceName = getDeviceName();
		
		messager.info(deviceName, test, DateUtils.now(), errorMessage);
		
		EmailReportItemCollector.push(createTestResult(result, TestResultType.SKIP, errorMessage, result.getMethod().getDescription(), messager.equals(Messager.CONFIG_SKIPPED)));
		
		if (testItem) {
	    	ZafiraIntegrator.finishTestMethod(result, errorMessage);
		}		
		result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
    }
    
    private String getDeviceName() {
    	String deviceName = "";
    	Device device = DevicePool.getDevice();
    	if (device != null) {
    		deviceName = device.getName();
    	}
    	return deviceName;
    }
    
    @Override
    public void beforeConfiguration(ITestResult result) {
   		startItem(result, Messager.CONFIG_STARTED);
   		super.beforeConfiguration(result);
    }
    
    @Override
    public void onConfigurationSuccess(ITestResult result) {
   		passItem(result, Messager.CONFIG_PASSED, false);
   		super.onConfigurationSuccess(result);
    }
    
    @Override
    public void onConfigurationSkip(ITestResult result) {
   		skipItem(result, Messager.CONFIG_SKIPPED, false);
   		super.onConfigurationSkip(result);
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
    	failItem(result, Messager.CONFIG_FAILED, false);
		String test = TestNamingUtil.getCanonicalTestName(result);
		closeLogAppender(test);
		super.onConfigurationFailure(result);
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
	}

	@Override
	public void onTestSuccess(ITestResult result)
	{
		passItem(result, Messager.TEST_PASSED, true);
		super.onTestSuccess(result);
	}

	@Override
	public void onTestFailure(ITestResult result)
	{
		String test = TestNamingUtil.getCanonicalTestName(result);
		int count = RetryCounter.getRunCount(test);		
		int maxCount = RetryAnalyzer.getMaxRetryCountForTest(result);
		LOGGER.debug("count: " + count + "; maxCount:" + maxCount);

		IRetryAnalyzer retry=result.getMethod().getRetryAnalyzer();
		if (count < maxCount && retry == null) {
			LOGGER.error("retry_count will be ignored as RetryAnalyzer is not declared for " + result.getMethod().getMethodName());
		}
		
		if (count < maxCount && retry != null)
		{
			TestNamingUtil.decreaseRetryCounter(test);
			failRetryItem(result, Messager.RETRY_RETRY_FAILED, true, count, maxCount);
		} else {
			failItem(result, Messager.TEST_FAILED, true);
			closeLogAppender(test);
		}
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
		super.onTestFailure(result);		
	}
	
	@Override
	public void onTestSkipped(ITestResult result)
	{
		skipItem(result, Messager.TEST_SKIPPED, true);
		TestNamingUtil.releaseTestInfoByThread(Thread.currentThread().getId());
		super.onTestSkipped(result);
	}
	
	@Override
	public void onFinish(ITestContext context)
	{
		ZafiraIntegrator.finishSuite();		
		removeIncorrectlyFailedTests(context);
		super.onFinish(context);
	}

	/**
	 * When the test is restarted this method cleans fail statistics in test
	 * context.
	 * 
	 * @param test
	 *            - test context.
	 */
	public static void removeIncorrectlyFailedTests(ITestContext context)
	{
		ITestNGMethod[] methods = context.getAllTestMethods();
		LOGGER.debug("number of all executed methods is: " + methods.length);
		for(int i=0;i<methods.length;i++){
			LOGGER.debug("Analyzed method is: " + methods[i].getMethodName());
			if(methods[i].getCurrentInvocationCount()>1){
				LOGGER.debug("Analyzed method invocation count is: " + methods[i].getCurrentInvocationCount());
				LOGGER.debug("Count of failed method is: " + context.getFailedTests().getResults(methods[i]).size());
				LOGGER.debug("Count of passed method is: " + context.getPassedTests().getResults(methods[i]).size());
				
				if (context.getFailedTests().getResults(methods[i]).size() > 1 && 
						context.getPassedTests().getResults(methods[i]).size() == 1){
					
					while (context.getFailedTests().size() > 0) {
						LOGGER.debug("Removing " + methods[i].getMethodName() + " from failed results as passed result determined.");
						context.getFailedTests().removeResult(methods[i]);
					}
				}
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
				//remove physically all screenshots if test/config pass and KEEP_ALL_SCREENSHOTS=false to improve cooperation with CI tools
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
