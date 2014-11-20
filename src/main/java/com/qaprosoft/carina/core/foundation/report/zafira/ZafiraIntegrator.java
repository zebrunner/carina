package com.qaprosoft.carina.core.foundation.report.zafira;

import java.util.Date;
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
	protected static final Logger LOGGER = Logger.getLogger(ZafiraIntegrator.class);
	
	private static UserType user;
	private static JobType job, parentJob;
	private static TestSuiteType suite;
	private static TestRunType run;
	
	Map<String, Long> testStartTime;
	
	
	private static final String zafiraUrl = Configuration.get(Parameter.ZAFIRA_SERVICE_URL);
			
	private static final String ciUrl = Configuration.get(Parameter.CI_URL);
	private static final String ciBuild = Configuration.get(Parameter.CI_BUILD);
	private static final String ciBuildCause = Configuration.get(Parameter.CI_BUILD_CAUSE);
	
	private static final String ciParentUrl = Configuration.get(Parameter.CI_PARENT_URL);
	private static final String ciParentBuild = Configuration.get(Parameter.CI_PARENT_BUILD);
	
	private static final String ciUserId = Configuration.get(Parameter.CI_USER_ID);
	private static final String ciUserFirstName = Configuration.get(Parameter.CI_USER_FIRST_NAME);
	private static final String ciUserLastName = Configuration.get(Parameter.CI_USER_LAST_NAME);
	private static final String ciUserEmail = Configuration.get(Parameter.CI_USER_EMAIL);
	
	private static final String gitBranch = Configuration.get(Parameter.GIT_BRANCH);
	private static final String gitCommit = Configuration.get(Parameter.GIT_COMMIT);
	private static final String gitUrl = Configuration.get(Parameter.GIT_URL);
	
	
	private static final String VIEW_PATTER = "/view/";
	private static final String JOB_PATTER = "/job/";
	
	private static final String ANONYMOUS_USER = "anonymous";
	


	private static final ZafiraClient zc = new ZafiraClient(zafiraUrl);
	
	public static void register(ITestContext context) {
		if (!isValid())
			return;
		
		try {
			if (ciUserId.equals("$BUILD_USER_ID")) {
				user = registerUser(ANONYMOUS_USER);
			} else {
				user = registerUser(ciUserId, ciUserEmail, ciUserFirstName, ciUserLastName);
			}
			
			LOGGER.info("user: " + user.getId() + ", uid: " + user.getUserName() + ", first name:" + user.getFirstName() + ", last name:" + user.getLastName() + ", email:" + user.getEmail());
			
			job = registerJob(ciUrl, user.getId());
			LOGGER.info("job: " + job.getId() + "|" + job.getName() + "|" + job.getJenkinsHost() + "|" + job.getJobURL());
			
			UserType suiteOwner = registerUser(Ownership.getSuiteOwner(context));
			suite = registerTestSuite(context.getSuite().getName(), suiteOwner.getId());
			LOGGER.info("suite: " + suite.getId() + "|" + suite.getName() + "|" + suite.getClass() + "|" + suite.getUserId() + "|" + suite.getDescription());
			
			String workItem = Configuration.get(Parameter.JIRA_SUITE_ID);
			
			String configXML = "";
			
			if (ciBuildCause.equalsIgnoreCase("MANUALTRIGGER")) {
				run = registerTestRunByHUMAN(suite.getId(), user.getId(),
						gitUrl, gitBranch, gitCommit, configXML, job.getId(),
						Integer.valueOf(ciBuild), Initiator.HUMAN, workItem);
			}
			if (ciBuildCause.equalsIgnoreCase("UPSTREAMTRIGGER")) {
				//register/retrieve anonymous
				UserType anonymousUser = registerUser(ANONYMOUS_USER);
				//register parentJob
				parentJob = registerJob(ciParentUrl, anonymousUser.getId());
				
				run = registerTestRunUPSTREAM_JOB(suite.getId(), gitUrl, gitBranch,
						gitCommit, configXML, job.getId(), parentJob.getId(),
						Integer.valueOf(ciParentBuild), Integer.valueOf(ciBuild),
						Initiator.UPSTREAM_JOB, workItem);
			}
			if (ciBuildCause.equalsIgnoreCase("TIMERTRIGGER")) {
				run = registerTestRunBySCHEDULER(suite.getId(), gitUrl,
						gitBranch, gitCommit, configXML, job.getId(),
						Integer.valueOf(ciBuild), Initiator.SCHEDULER, workItem);
			}
			LOGGER.info("run: " + run.getId() + "|" + run.getClass() + "|" + run.getBuildNumber() + "|" + run.getJobId() + "|" + run.getTestSuiteId() + "|" + run.getUserId() + "|" + run.getScmBranch() + "|" + run.getWorkItemJiraId());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void finish() {
		if (!isValid())
			return;
		
		try {
			TestRunType finishedRun = finishTestRun();
			LOGGER.info("run: " + finishedRun.getId() + "|" + finishedRun.getClass() + "|" + finishedRun.getBuildNumber() + "|" + finishedRun.getJobId() + "|" + finishedRun.getTestSuiteId() + "|" + finishedRun.getUserId() + "|" + finishedRun.getScmBranch());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateTest(ITestResult result, Status status, String message) {
		if (!isValid())
			return;
		
		String testClass = result.getMethod().getTestClass().getName();
		String testMethod = result.getMethod().getMethodName();
		
		//get methodOwner and register as separate user
		UserType methodOwner = registerUser(Ownership.getMethodOwner(result));
		TestCaseType testCase = registerTestCase(testClass, testMethod, "", suite.getId(), methodOwner.getId());
		
		String test = TestNamingUtil.getCanonicalTestName(result);
		String testArgs = result.getParameters().toString();
		
	    String demoUrl = ReportContext.getTestScreenshotsLink(test);
	    String logUrl = ReportContext.getTestLogLink(test);

		TestType registeredTest = registerTest(test, status, testArgs, run.getId(), testCase.getId(), message, TestNamingUtil.getTestStartDate(test), new Date().getTime(), demoUrl, logUrl);
		LOGGER.info("registeredTest: " + registeredTest.getId() + "|" + registeredTest.getName() + "|" + registeredTest.getStatus() + "|" + registeredTest.getLogURL() + "|" + registeredTest.getWorkItems());
		
	}

	
	private static boolean isValid() {
		//TODO: add logic to return if jenkinsJobUrl or jenkinsJobBuild is NULL
		return !zafiraUrl.isEmpty() && !zafiraUrl.equalsIgnoreCase("null");
	}
	
	private static UserType registerUser(String userName)
	{
		UserType user = new UserType(userName);
		Response<UserType> response = zc.createUser(user);
		return response.getObject();
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
	
	private static TestRunType registerTestRunByHUMAN(Long testSuiteId, Long userId, String scmURL, String scmBranch, String scmCommit,
			String configXML, Long jobId, Integer buildNumber, Initiator startedBy, String workItem)
	{
		TestRunType testRun = new TestRunType(testSuiteId, userId, scmURL, scmBranch, scmCommit, configXML, jobId, buildNumber, startedBy, workItem);
		Response<TestRunType> response = zc.createTestRun(testRun);
		return response.getObject();
	}

	private static TestRunType registerTestRunBySCHEDULER(Long testSuiteId, String scmURL, String scmBranch, String scmCommit,
			String configXML, Long jobId, Integer buildNumber, Initiator startedBy, String workItem)
	{
		TestRunType testRun = new TestRunType(testSuiteId, scmURL, scmBranch, scmCommit, configXML, jobId, buildNumber, startedBy, workItem);
		Response<TestRunType> response = zc.createTestRun(testRun);
		return response.getObject();
	}
	
	private static TestRunType registerTestRunUPSTREAM_JOB(Long testSuiteId, String scmURL, String scmBranch, String scmCommit,
			String configXML, Long jobId, Long parentJobId, Integer parentBuildNumber, Integer buildNumber, Initiator startedBy, String workItem)
	{
		TestRunType testRun = new TestRunType(testSuiteId, scmURL, scmBranch, scmCommit, configXML, jobId, parentJobId, parentBuildNumber, buildNumber, startedBy, workItem);
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
			Long startTime, Long finishTime, String demoURL, String logURL)
	{
		// name:R, status:R, testArgs:NR, testRunId:R, testCaseId:R, message:NR, startTime:NR, finishTime:NR, demoURL:NR, logURL:NR, workItems:NR
		TestType test = new TestType(name, status, testArgs, testRunId, testCaseId, message, startTime, finishTime, demoURL, logURL, null);
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
		if (jobURL.equalsIgnoreCase("null")) {
			LOGGER.error("CI job URL is null! Verify that ci_url or ci_parent_url properties are specified!");
			return "";
		}
		return jobURL.contains(VIEW_PATTER) ? jobURL.split(VIEW_PATTER)[0] : jobURL.split(JOB_PATTER)[0];
	}
	
	private static String getJenkinsJobName(String jobURL)
	{
		if (jobURL.equalsIgnoreCase("null")) {
			LOGGER.error("CI job URL is null! Verify that ci_url or ci_parent_url properties are specified!");
			return "";
		}
		
		return jobURL.split(JOB_PATTER)[1].replaceAll("/", "");
	}
	
	

}
