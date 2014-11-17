package com.qaprosoft.carina.core.foundation.report.zafira;

import org.testng.ITestContext;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
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

public class ZafiraIntegrator {
	private static JobType job;
	private static TestSuiteType suite;
	private static TestRunType run; 

	private static final ZafiraClient zc = new ZafiraClient(Configuration.get(Parameter.ZAFIRA_SERVICE_URL));
	
	public static void register(ITestContext context) {
		job = registerJob(Configuration.get(Parameter.JENKINS_JOB_URL), "anonymous");
		suite = registerTestSuite(context.getSuite().getName(), Ownership.getSuiteOwner(context));
		
		//run = registerTestRun(suite.getId(), userName, scmURL, scmBranch, scmRevision, configXML, job.getId(), buildNumber, Initiator startedBy)		
		
	}
	
	public static JobType registerJob(String jobUrl, String userName)
	{
		JobType job = new JobType(jobUrl, userName);
		Response<JobType> response = zc.createJob(job);
		return response.getObject();
	}
	
	public static TestSuiteType registerTestSuite(String suiteName, String userName)
	{
		return registerTestSuite(suiteName, "", userName);
	}
	
	public static TestSuiteType registerTestSuite(String suiteName, String description, String userName)
	{
		TestSuiteType testSuite = new  TestSuiteType(suiteName, userName);
		Response<TestSuiteType> response = zc.createTestSuite(testSuite);
		return response.getObject();
	}
	
	public static TestCaseType[] registerTestCase(String testClass, String testMethod, String info, Long testSuiteId, String userName)
	{
		TestCaseType[] testCases = new TestCaseType[] {new TestCaseType(testClass, testMethod, info, testSuiteId, userName)};
		Response<TestCaseType[]> response = zc.createTestCases(testCases);
		return response.getObject();
	}
	
	public static TestRunType registerTestRun(Long testSuiteId, String userName, String scmURL, String scmBranch, String scmRevision,
			String configXML, Long jobId, Integer buildNumber, Initiator startedBy)
	{
		TestRunType testRun = new TestRunType(testSuiteId, userName, scmURL, scmBranch, scmRevision, configXML, jobId, buildNumber, startedBy);
		Response<TestRunType> response = zc.createTestRun(testRun);
		return response.getObject();
	}

	public static TestType registerTest(String name, Status status, String testArgs, Long testRunId, Long testCaseId, String message,
			Long startTime, Long finishTime, String demoURL, String logURL)
	{
		TestType test = new TestType(name, status, testArgs, testRunId, testCaseId, message, startTime, finishTime, demoURL, logURL);
		Response<TestType> response = zc.createTest(test);
		return response.getObject();
	}
	
	public static TestRunType finishTestRun(Long runId)
	{
		Response<TestRunType> response = zc.finishTestRun(runId);
		return response.getObject();
	}

}
