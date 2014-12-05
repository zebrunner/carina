package com.qaprosoft.carina.core.foundation.report.zafira;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.configuration.ArgumentType;
import com.qaprosoft.carina.core.foundation.utils.configuration.ConfigurationBin;
import com.qaprosoft.carina.core.foundation.utils.marshaller.MarshallerHelper;
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
	
	
	private static final String VIEW_PATTERN = "/view/";
	private static final String JOB_PATTERN = "/job/";
	
	private static final String ANONYMOUS_USER = "anonymous";
	private static Boolean isRegistered = false;


	private static final ZafiraClient zc = new ZafiraClient(zafiraUrl);
	
	public static void startSuite(ITestContext context) {
		if (!isValid())
			return;
		
		try {
			user = registerUser(ciUserId, ciUserEmail, ciUserFirstName, ciUserLastName);
			
			//LOGGER.debug("user: " + user.getId() + ", uid: " + user.getUserName() + ", first name:" + user.getFirstName() + ", last name:" + user.getLastName() + ", email:" + user.getEmail());
			
			job = registerJob(ciUrl, user.getId());
			//LOGGER.debug("job: " + job.getId() + "|" + job.getName() + "|" + job.getJenkinsHost() + "|" + job.getJobURL());
			
			//register suiteOwner
			UserType suiteOwner = registerUser(Ownership.getSuiteOwner(context));
			suite = registerTestSuite(context.getSuite().getName(), suiteOwner.getId());

			
			//LOGGER.debug("suite: " + suite.getId() + "|" + suite.getName() + "|" + suite.getClass() + "|" + suite.getUserId() + "|" + suite.getDescription());
			
			String workItem = !Configuration.get(Parameter.JIRA_SUITE_ID).isEmpty() ? Configuration.get(Parameter.JIRA_SUITE_ID) : null;

			Integer build = 0;
			try {
				build = Integer.valueOf(ciBuild);
			} catch (NumberFormatException e) {
				LOGGER.warn("Unable to parse build number: '" + ciBuild + "'");
			}
			
			Integer parentBuild = 0;
			try {
				parentBuild = Integer.valueOf(ciParentBuild);
			} catch (NumberFormatException e) {
				LOGGER.warn("Unable to parse parent build number: '" + ciParentBuild + "'");
			}
			
			
			String configXML = getConfiguration();
			
			if (ciBuildCause.equalsIgnoreCase("MANUALTRIGGER")) {
				run = registerTestRunByHUMAN(suite.getId(), user.getId(),
						gitUrl, gitBranch, gitCommit, configXML, job.getId(),
						build, Initiator.HUMAN, workItem);
			} else if (ciBuildCause.equalsIgnoreCase("UPSTREAMTRIGGER")) {
				//register/retrieve anonymous
				UserType anonymousUser = registerUser(ANONYMOUS_USER);
				//register parentJob
				parentJob = registerJob(ciParentUrl, anonymousUser.getId());
				
				run = registerTestRunUPSTREAM_JOB(suite.getId(), gitUrl, gitBranch,
						gitCommit, configXML, job.getId(), parentJob.getId(),
						parentBuild, build,
						Initiator.UPSTREAM_JOB, workItem);
			} else if (ciBuildCause.equalsIgnoreCase("TIMERTRIGGER")) {
				run = registerTestRunBySCHEDULER(suite.getId(), gitUrl,
						gitBranch, gitCommit, configXML, job.getId(),
						build, Initiator.SCHEDULER, workItem);
			} else {
				throw new RuntimeException("Unable to register test run for zafira service: " + zafiraUrl + " due to the misses build cause: '" + ciBuildCause +"'");
			}
			
			//LOGGER.debug("run: " + run.getId() + "|" + run.getClass() + "|" + run.getBuildNumber() + "|" + run.getJobId() + "|" + run.getTestSuiteId() + "|" + run.getUserId() + "|" + run.getScmBranch());
			
			if (run == null) {
				throw new RuntimeException("Unable to register test run for zafira service: " + zafiraUrl);
			}
			isRegistered = true;
		}
		catch (Exception e) {
			isRegistered = false;
			LOGGER.error(e.getMessage());
		}

	}
	
	public static void finishSuite() {
		if (!isValid() || !isRegistered)
			return;
		
		try {
			finishTestRun();
			//TestRunType finishedRun = finishTestRun();
			//LOGGER.debug("run: " + finishedRun.getId() + "|" + finishedRun.getClass() + "|" + finishedRun.getBuildNumber() + "|" + finishedRun.getJobId() + "|" + finishedRun.getTestSuiteId() + "|" + finishedRun.getUserId() + "|" + finishedRun.getScmBranch());
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	public static TestType finishTestMethod(ITestResult result, Status status, String message) {
		if (!isValid() || !isRegistered)
			return null;
		
		TestType registeredTest = null;
		try {		
		
			String test = TestNamingUtil.getCanonicalTestName(result);
			
			
			String testClass = result.getMethod().getTestClass().getName();
			String testMethod = result.getMethod().getMethodName();
			
			//if method owner is not specified then try to use suite owner. If both are not declared then ANONYMOUS will be used
			String owner = !Ownership.getMethodOwner(result).isEmpty() ? Ownership.getMethodOwner(result) : Ownership.getSuiteOwner(result.getTestContext());
			UserType methodOwner = registerUser(owner);
			
			
			TestCaseType testCase = registerTestCase(testClass, testMethod, "", suite.getId(), methodOwner.getId());
			if (testCase == null) {
				throw new RuntimeException("Unable to register tetscase '" + testMethod + "' for zafira service: " + zafiraUrl);
			}		
			
			
			String testArgs = result.getParameters().toString();
			
		    String demoUrl = ReportContext.getTestScreenshotsLink(test);
		    String logUrl = ReportContext.getTestLogLink(test);
	
			registeredTest = registerTest(test, status, testArgs, run.getId(), testCase.getId(), message, TestNamingUtil.getTestStartDate(test), new Date().getTime(), demoUrl, logUrl);
			TestNamingUtil.accociateZafiraTest(test, registeredTest);
			
			//LOGGER.debug("registeredTest: " + registeredTest.getId() + "|" + registeredTest.getName() + "|" + registeredTest.getStatus() + "|" + registeredTest.getLogURL());
		}
		catch (Exception e) {
			isRegistered = false;
			LOGGER.error(e.getMessage());
		}
		return registeredTest;
	}

	public static TestType registerWorkItems(Long testId, List<String> workItems)
	{
		if (!isValid() || !isRegistered)
			return null;
		
		Response<TestType> response = zc.createTestWorkItems(testId, workItems);
		return response.getObject();
	}

	private static boolean isValid() {
		return !zafiraUrl.isEmpty() && !ciUrl.isEmpty() && zc.isAvailable();
	}
	
	private static UserType registerUser(String userName)
	{
		return registerUser(userName, null, null, null);
	}
	
	private static UserType registerUser(String userName, String email, String firstName, String lastName)
	{
		if (userName == null)
			userName = ANONYMOUS_USER;
		if(userName.isEmpty())
			userName = ANONYMOUS_USER;
		if (userName.equals("$BUILD_USER_ID"))
			userName = ANONYMOUS_USER;

			
		UserType regUser = new UserType(userName, email, firstName, lastName);
		Response<UserType> response = zc.createUser(regUser);
		regUser = response.getObject();
		
		if (regUser == null)
			throw new RuntimeException("Unable to register user '" + userName + "' for zafira service: " + zafiraUrl);
		
		return regUser;
	}

	private static JobType registerJob(String jobUrl, Long userId)
	{
		String jobName = getJenkinsJobName(jobUrl);
		String jenkinsHost = getJenkinsHost(jobUrl);
		JobType regJob = new JobType(jobName, jobUrl, jenkinsHost, userId);
		Response<JobType> response = zc.createJob(regJob);
		
		regJob = response.getObject();

		if (regJob == null) {
			throw new RuntimeException("Unable to register job '" + ciUrl + "' for zafira service: " + zafiraUrl);
		}

		
		return regJob;
	}
	
	
	private static TestSuiteType registerTestSuite(String suiteName, Long userId)
	{
		TestSuiteType testSuite = new  TestSuiteType(suiteName, userId);
		Response<TestSuiteType> response = zc.createTestSuite(testSuite);
		testSuite = response.getObject();
		
		if (testSuite == null) {
			throw new RuntimeException("Unable to register test suite '" + suiteName + "' for zafira service: " + zafiraUrl);
		}
		
		return testSuite;
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

	
	private static String getJenkinsHost(String jobURL)
	{
		if (jobURL.isEmpty()) {
			LOGGER.error("CI job URL is not valid! Verify that ci_url or ci_parent_url properties are specified!");
			return "";
		}
		return jobURL.contains(VIEW_PATTERN) ? jobURL.split(VIEW_PATTERN)[0] : jobURL.split(JOB_PATTERN)[0];
	}
	
	private static String getJenkinsJobName(String jobURL)
	{
		String[] items = jobURL.split("/");
		jobURL = items[items.length - 1]; //there is no need to verify on length > 0 as it always equals 1 even if string is empty

		if (jobURL.isEmpty()) {
			LOGGER.error("CI job URL is not valid! Verify that ci_url or ci_parent_url properties are specified!");
		}

		return jobURL;
	}
	
	private static String getConfiguration() {
        ConfigurationBin conf = new ConfigurationBin();
        for (Configuration.Parameter parameter : Configuration.Parameter.values()) {
            conf.getArg().add(getArgByParameter(parameter));
        }

        return MarshallerHelper.marshall(conf);
	}
	
    private static ArgumentType getArgByParameter(Configuration.Parameter parameter) {
        ArgumentType arg = new ArgumentType();
        arg.setKey(parameter.getKey());
        arg.setValue(Configuration.get(parameter));       
        return arg;
    }
	
	@SuppressWarnings("unused")
	private static void setConfiguration(String configXML) {
        ConfigurationBin conf = MarshallerHelper.unmarshall(configXML, ConfigurationBin.class);
        //TODO: implement setter methods for all Configuration parameters
        for (ArgumentType arg : conf.getArg()) {
            LOGGER.debug("Key: " + arg.getKey() + " Value: " + arg.getValue());
        }
	}    

}
