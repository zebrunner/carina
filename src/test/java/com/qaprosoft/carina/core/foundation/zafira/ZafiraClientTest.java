package com.qaprosoft.carina.core.foundation.zafira;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.zafira.client.ZafiraClient;
import com.qaprosoft.zafira.client.ZafiraClient.Response;
import com.qaprosoft.zafira.client.model.JobType;
import com.qaprosoft.zafira.client.model.TestCaseType;
import com.qaprosoft.zafira.client.model.TestRunType;
import com.qaprosoft.zafira.client.model.TestRunType.Initiator;
import com.qaprosoft.zafira.client.model.TestSuiteType;
import com.qaprosoft.zafira.client.model.TestType;
import com.qaprosoft.zafira.client.model.UserType;

public class ZafiraClientTest
{
	private static final boolean ENABLED = true;
	private UserType user;
	private JobType job;
	private TestSuiteType testSuite;
	TestRunType testRun;
	
	private static final ZafiraClient zc = new ZafiraClient("http://stg.caronfly.com:8080/zafira");
	
	@Test(enabled=ENABLED)
	public void testStatus()
	{
		boolean isAvailable = zc.isAvailable();
		Assert.assertTrue(isAvailable);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateUser()
	{
		// userName:R, email:NR, firstName:NR, lastName:NR
		user = new UserType("vdelendik", "abc@gmail.com", "Vadim", "Delendik");
		Response<UserType> response = zc.createUser(user);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED, dependsOnMethods="testCreateUser")
	public void testCreateJob()
	{
		// name:R, jobURL:R, jenkinsHost:R, userId:R
		job = new JobType("zafira-ws", "http://stg.caronfly.com:8081/view/zafira/job/zafira-ws", "http://stg.caronfly.com:8081", user.getId());
		Response<JobType> response = zc.createJob(job);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED, dependsOnMethods="testCreateUser")
	public void testCreateTestSuite()
	{
		// name:R, description:NR, userId:R
		testSuite = new  TestSuiteType("sanity", user.getId());
		Response<TestSuiteType> response = zc.createTestSuite(testSuite);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestRunByHUMAN()
	{
		// testSuiteId:R, userId:R, scmURL:NR, scmBranch:NR, scmCommit:NR, configXML:NR, jobId:R, buildNumber:R, startedBy:R, workItems:NR
		testRun = new TestRunType(testSuite.getId(), user.getId(), "http://localhost:8081", "master", "sdfs232fs3rwf34f5", "<config></config>", job.getId(), 10, Initiator.HUMAN, null/*"JIRA-1234"*/);
		Response<TestRunType> response = zc.createTestRun(testRun);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestRunByUPSTREAM_JOB()
	{
		// testSuiteId:R, scmURL:NR, scmBranch:NR, scmCommit:NR, configXML:NR, jobId:R, upstreamJobId:R, upstreamJobBuildNumber:R, buildNumber:R, startedBy:R, workItems:NR
		testRun = new TestRunType(testSuite.getId(), "http://localhost:8081", "master", "sdfs232fs3rwf34f5", "<config></config>", job.getId(), job.getId(), 20, 11, Initiator.UPSTREAM_JOB, "JIRA-1234");
		Response<TestRunType> response = zc.createTestRun(testRun);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestRunBySCHEDULER()
	{
		// testSuiteId:R, scmURL:NR, scmBranch:NR, scmCommit:NR, configXML:NR, jobId:R, buildNumber:R, startedBy:R, workItems:NR
		testRun = new TestRunType(testSuite.getId(), "http://localhost:8081", "master", "sdfs232fs3rwf34f5", "<config></config>", job.getId(), 30, Initiator.SCHEDULER, "JIRA-1234");
		Response<TestRunType> response = zc.createTestRun(testRun);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testFinishTestRun()
	{
		Response<TestRunType> response = zc.finishTestRun(testRun.getId());
		Assert.assertEquals(response.getStatus(), 200);
	}

	@Test(enabled=ENABLED)
	public void testCreateTest()
	{
		// name:R, status:R, testArgs:NR, testRunId:R, testCaseId:R, message:NR, startTime:NR, finishTime:NR, demoURL:NR, logURL:NR, workItems:NR
		List<String> workItems = new ArrayList<String>();
		workItems.add("JIRA-1234");
		TestType test = new TestType("Zafira login test", com.qaprosoft.zafira.client.model.TestType.Status.PASSED, "<config></config>", 14L, 1L, "Hello!", new Date().getTime(), new Date().getTime(), "http://localhost:8081/demo", "http://localhost:8081/log", workItems);
		Response<TestType> response = zc.createTest(test);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestWorkItems()
	{
		List<String> workItems = new ArrayList<String>();
		workItems.add("JIRA-1234");
		workItems.add("JIRA-3422");
		Response<TestType> response = zc.createTestWorkItems(24L, workItems);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestCases()
	{
		// testClass:R, testMethod:R, info:NR, testSuiteId:R, userId:R
		TestCaseType[] testCases = new TestCaseType[] {new TestCaseType("com.qaprosoft.zafira.Test", "testLogin", "Hello!", 1L, 1L), new TestCaseType("com.qaprosoft.zafira.Test", "testLogout", "Hello!", 1L, 1L)};
		Response<TestCaseType[]> response = zc.createTestCases(testCases);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestCase()
	{
		// testClass:R, testMethod:R, info:NR, testSuiteId:R, userId:R
		TestCaseType testCase = new TestCaseType("com.qaprosoft.zafira.Test", "testLogin", "Hello!", 3L, 8L);
		Response<TestCaseType> response = zc.createTestCase(testCase);
		Assert.assertEquals(response.getStatus(), 200);
	}
}