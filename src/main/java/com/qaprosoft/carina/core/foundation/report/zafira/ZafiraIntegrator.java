package com.qaprosoft.carina.core.foundation.report.zafira;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.utils.ownership.Ownership;
import com.qaprosoft.zafira.client.ZafiraClient;
import com.qaprosoft.zafira.client.ZafiraClient.Response;
import com.qaprosoft.zafira.client.model.JobType;
import com.qaprosoft.zafira.client.model.TestCaseType;
import com.qaprosoft.zafira.client.model.TestRunType;
import com.qaprosoft.zafira.client.model.TestRunType.Initiator;
import com.qaprosoft.zafira.client.model.TestSuiteType;
import com.qaprosoft.zafira.client.model.TestType;
import com.qaprosoft.zafira.client.model.TestType.Status;
import com.qaprosoft.zafira.client.model.UserType;

public class ZafiraIntegrator {
	private static final Logger LOGGER = Logger.getLogger(ZafiraIntegrator.class);
	
	private static UserType user;
	private static JobType job;
	private static TestSuiteType suite;
	private static TestRunType run; 
	
	Map<String, Long> testStartTime;
	//private static final String jenkinsUsername = Configuration.get(Parameter.JENKINS_USER);
	//private static final String jenkinsPassword = Configuration.get(Parameter.JENKINS_PASSWORD);
	private static final String jenkinsJobUrl = Configuration.get(Parameter.CI_JOB_URL);
	private static final Integer jenkinsJobBuild = Integer.valueOf(Configuration.get(Parameter.CI_JOB_BUILD));
	
	
	
	
	private static final String VIEW_PATTER = "/view/";
	private static final String JOB_PATTER = "/job/";


	private static final ZafiraClient zc = new ZafiraClient(Configuration.get(Parameter.ZAFIRA_SERVICE_URL));
	
	public static void register(ITestContext context) {
		if (!isValid())
			return;
		
		try {
			user = registerUser(Configuration.get(Parameter.CI_USER_ID), Configuration.get(Parameter.CI_USER_EMAIL), Configuration.get(Parameter.CI_USER_FIRST_NAME), Configuration.get(Parameter.CI_USER_SECOND_NAME));
			job = registerJob(jenkinsJobUrl, user.getId());
			
			UserType suiteOwner = new UserType(Ownership.getSuiteOwner(context), null, null, null);
			suite = registerTestSuite(context.getSuite().getName(), suiteOwner.getId());
			
			String workItem = Configuration.get(Parameter.JIRA_SUITE_ID);
			
			LOGGER.error("TODO: add implementation to identify scmURL, scmBranch, scmRevision, configXML");
			String scmURL, scmBranch, scmRevision, configXML;
			scmURL=scmBranch=scmRevision=configXML="";
			
			Initiator initiator = null;
			if (Configuration.get(Parameter.CI_BUILD_CAUSE).equalsIgnoreCase("MANUALTRIGGER")) {
				initiator = Initiator.HUMAN;
			}
			if (Configuration.get(Parameter.CI_BUILD_CAUSE).equalsIgnoreCase("UPSTREAMTRIGGER")) {
				initiator = Initiator.UPSTREAM_JOB;
			}
			if (Configuration.get(Parameter.CI_BUILD_CAUSE).equalsIgnoreCase("TIMERTRIGGER")) {
				initiator = Initiator.SCHEDULER;
			}
			run = registerTestRun(suite.getId(), user.getId(), scmURL, scmBranch, scmRevision, configXML, job.getId(), jenkinsJobBuild, initiator, workItem);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void finish() {
		if (!isValid())
			return;
		
		try {
			finishTestRun();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateTest(ITestResult result, Status status, String message, List<String> workItem) {
		if (!isValid())
			return;
		
		String testClass = result.getMethod().getTestClass().getName();
		String testMethod = result.getMethod().getMethodName();
		TestCaseType testCase = registerTestCase(testClass, testMethod, "", suite.getId(), user.getId());
		
		String test = TestNamingUtil.getCanonicalTestName(result);
		String testArgs = result.getParameters().toString();
		
	    String demoUrl = ReportContext.getTestScreenshotsLink(test);
	    String logUrl = ReportContext.getTestLogLink(test);

		registerTest(test, status, testArgs, run.getId(), testCase.getId(), message, TestNamingUtil.getTestStartDate(test), new Date().getTime(), demoUrl, logUrl, workItem);
	}

	
	private static boolean isValid() {
		//TODO: add logic to return if jenkinsJobUrl or jenkinsJobBuild is NULL
		return !Configuration.get(Parameter.ZAFIRA_SERVICE_URL).isEmpty() && !Configuration.get(Parameter.ZAFIRA_SERVICE_URL).equalsIgnoreCase("null");
	}
	private static UserType registerUser(String userName, String email, String firstName, String lastName)
	{
		UserType user = new UserType(userName, email, firstName, lastName);
		Response<UserType> response = zc.createUser(user);
		return response.getObject();
	}

	private static JobType registerJob(String jobUrl, Long userId)
	{
		String jobName = getJenkinsJobName(jobUrl);
		String jenkinsHost = getJenkinsHost(jobUrl);
		JobType job = new JobType(jobName, jobUrl, jenkinsHost, userId);
		Response<JobType> response = zc.createJob(job);
		
		return response.getObject();
	}
	
	
	private static TestSuiteType registerTestSuite(String suiteName, Long userId)
	{
		TestSuiteType testSuite = new  TestSuiteType(suiteName, userId);
		Response<TestSuiteType> response = zc.createTestSuite(testSuite);
		return response.getObject();
	}
	
	private static TestRunType registerTestRun(Long testSuiteId, Long userId, String scmURL, String scmBranch, String scmRevision,
			String configXML, Long jobId, Integer buildNumber, Initiator startedBy, String workItem)
	{
		TestRunType testRun = new TestRunType(testSuiteId, userId, scmURL, scmBranch, scmRevision, configXML, jobId, buildNumber, startedBy, workItem);
		Response<TestRunType> response = zc.createTestRun(testRun);
		return response.getObject();
	}	
	
	
	private static TestCaseType registerTestCase(String testClass, String testMethod, String info, Long testSuiteId, Long userId)
	{
		TestCaseType testCase = new TestCaseType(testClass, testMethod, info, testSuiteId, userId);
		Response<TestCaseType> response = zc.createTestCase(testCase);
		return response.getObject();
	}
	
	private static TestType registerTest(String name, Status status, String testArgs, Long testRunId, Long testCaseId, String message,
			Long startTime, Long finishTime, String demoURL, String logURL, List<String> workItem)
	{
		// name:R, status:R, testArgs:NR, testRunId:R, testCaseId:R, message:NR, startTime:NR, finishTime:NR, demoURL:NR, logURL:NR, workItems:NR
		TestType test = new TestType(name, status, testArgs, testRunId, testCaseId, message, startTime, finishTime, demoURL, logURL, workItem);
		Response<TestType> response = zc.createTest(test);
		return response.getObject();
	}
	
	private static TestRunType finishTestRun()
	{
		Response<TestRunType> response = zc.finishTestRun(run.getId());
		return response.getObject();
	}

	
	/*
	private static JobWithDetails getJenkinsJobDetails(String jobURL) throws URISyntaxException, IOException
	{
		JenkinsServer jenkins = new JenkinsServer(new URI(getJenkinsHost(jobURL)), jenkinsUsername, jenkinsPassword);
		return jenkins.getJob(getJenkinsJobName(jobURL));
	}*/

	
	private static String getJenkinsHost(String jobURL)
	{
		return jobURL.contains(VIEW_PATTER) ? jobURL.split(VIEW_PATTER)[0] : jobURL.split(JOB_PATTER)[0];
	}
	
	private static String getJenkinsJobName(String jobURL)
	{
		return jobURL.split(JOB_PATTER)[1].replaceAll("/", "");
	}
	
	

}
