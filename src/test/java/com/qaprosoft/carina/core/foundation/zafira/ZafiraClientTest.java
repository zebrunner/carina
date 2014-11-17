package com.qaprosoft.carina.core.foundation.zafira;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.qaprosoft.carina.core.foundation.report.HtmlReportGenerator;
import com.qaprosoft.carina.core.foundation.report.zafira.ZafiraIntegrator;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
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
	private static final Logger LOGGER = Logger.getLogger(ZafiraClientTest.class);
	
	private static final boolean ENABLED = false;
	
	private static final ZafiraClient zc = new ZafiraClient(Configuration.get(Parameter.ZAFIRA_SERVICE_URL));
	
/*	private static final String jenkinsUsername = Configuration.get(Parameter.JENKINS_USER);
	private static final String jenkinsPassword = Configuration.get(Parameter.JENKINS_PASSWORD);
	private static final String jenkinsJobUrl = Configuration.get(Parameter.JENKINS_JOB_URL);
	private static final String jenkinsJobBuild = Configuration.get(Parameter.JENKINS_JOB_BUILD);*/
	
/*	@Test(enabled=true)
	public void Jenkinstest() throws URISyntaxException, IOException
	{
		JenkinsServer jenkins = new JenkinsServer(new URI("http://82.209.196.137:8081/jenkins"), jenkinsUsername, jenkinsPassword); //by default it should be jenkins url
		Map<String, Job> jobs = jenkins.getJobs();
		JobWithDetails jobDetails = jenkins.getJob(ZafiraIntegrator.getJenkinsJobName(jenkinsJobUrl));
		List<Job> upstreamJobs = jobDetails.getUpstreamProjects();
		
		Map<String, String> desc = jobDetails.getLastBuild().details().getParameters();
		String fullDisplayName = jobDetails.getLastBuild().details().getFullDisplayName();
		Build build = jobDetails.getBuilds().get(Integer.valueOf(jenkinsJobBuild));
		
	}*/
	@Test(enabled=ENABLED)
	public void testCreateUser()
	{
		// userName:R, email:NR, firstName:NR, lastName:NR
		UserType user = new UserType("vdelendik", "abc@gmail.com", "Vadim", "Delendik");
		Response<UserType> response = zc.createUser(user);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateJob()
	{
		// name:R, jobURL:R, jenkinsHost:R, userId:R
		JobType job = new JobType("zafira-ws", "http://stg.caronfly.com:8081/view/zafira/job/zafira-ws", "http://stg.caronfly.com:8081", 1L);
		Response<JobType> response = zc.createJob(job);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestSuite()
	{
		// name:R, description:NR, userId:R
		TestSuiteType testSuite = new  TestSuiteType("sanity", 1L);
		Response<TestSuiteType> response = zc.createTestSuite(testSuite);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestRunByHUMAN()
	{
		// testSuiteId:R, userId:R, scmURL:NR, scmBranch:NR, scmRevision:NR, configXML:NR, jobId:R, buildNumber:R, startedBy:R, workItemJiraId:NR
		TestRunType testRun = new TestRunType(1L, 1L, "http://localhost:8081", "master", "sdfs232fs3rwf34f5", "<config></config>", 1L, 10, Initiator.HUMAN, "JIRA-1234");
		Response<TestRunType> response = zc.createTestRun(testRun);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestRunByUPSTREAM_JOB()
	{
		// testSuiteId:R, scmURL:NR, scmBranch:NR, scmRevision:NR, configXML:NR, jobId:R, upstreamJobId:R, upstreamJobBuildNumber:R, buildNumber:R, startedBy:R, workItemJiraId:NR
		TestRunType testRun = new TestRunType(1L, "http://localhost:8081", "master", "sdfs232fs3rwf34f5", "<config></config>", 1L, 1L, 20, 11, Initiator.UPSTREAM_JOB, "JIRA-1234");
		Response<TestRunType> response = zc.createTestRun(testRun);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestRunBySCHEDULER()
	{
		// testSuiteId:R, scmURL:NR, scmBranch:NR, scmRevision:NR, configXML:NR, jobId:R, buildNumber:R, startedBy:R, workItemJiraId:NR
		TestRunType testRun = new TestRunType(1L, "http://localhost:8081", "master", "sdfs232fs3rwf34f5", "<config></config>", 1L, 30, Initiator.SCHEDULER, "JIRA-1234");
		Response<TestRunType> response = zc.createTestRun(testRun);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testFinishTestRun()
	{
		Response<TestRunType> response = zc.finishTestRun(4);
		Assert.assertEquals(response.getStatus(), 200);
	}

	@Test(enabled=ENABLED)
	public void testCreateTest()
	{
		// TODO: work items not created yet
		// name:R, status:R, testArgs:NR, testRunId:R, testCaseId:R, message:NR, startTime:NR, finishTime:NR, demoURL:NR, logURL:NR, workItems:NR
		TestType test = new TestType("Zafira login test", com.qaprosoft.zafira.client.model.TestType.Status.PASSED, "<config></config>", 1L, 1L, "Hello!", new Date().getTime(), new Date().getTime(), "http://localhost:8081/demo", "http://localhost:8081/log", new String[]{"JIRA-1234"});
		Response<TestType> response = zc.createTest(test);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestCase()
	{
		// testClass:R, testMethod:R, info:NR, testSuiteId:R, userId:R
		TestCaseType testCase = new TestCaseType("com.qaprosoft.zofira.Test", "testLogin", "Hello!", 1L, 1L);
		Response<TestCaseType> response = zc.createTestCase(testCase);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
	@Test(enabled=ENABLED)
	public void testCreateTestCases()
	{
		// testClass:R, testMethod:R, info:NR, testSuiteId:R, userId:R
		TestCaseType[] testCases = new TestCaseType[] {new TestCaseType("com.qaprosoft.zofira.Test", "testLogin", "Hello!", 1L, 1L), new TestCaseType("com.qaprosoft.zofira.Test", "testLogout", "Hello!", 1L, 1L)};
		Response<TestCaseType[]> response = zc.createTestCases(testCases);
		Assert.assertEquals(response.getStatus(), 200);
	}
}