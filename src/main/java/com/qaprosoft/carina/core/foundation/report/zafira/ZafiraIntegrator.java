package com.qaprosoft.carina.core.foundation.report.zafira;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.retry.RetryCounter;
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

	private static UserType user = null;
	private static JobType job, parentJob;
	private static TestSuiteType suite = null;
	private static TestRunType run = null;
	
	private static TestType[] tests = null;

	private static final String zafiraUrl = Configuration.get(Parameter.ZAFIRA_SERVICE_URL);
	private static final Boolean rerunFailures = Configuration.getBoolean(Parameter.RERUN_FAILURES);

	private static String ciUrl = Configuration.get(Parameter.CI_URL);
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

	private static List<String> uniqueKeys;

	private static final ZafiraClient zc = new ZafiraClient(zafiraUrl);
	
	public static void startSuite(ITestContext context, String suiteFileName) {
		if (!isValid())
			return;
		if (isRegistered) // AUTO-731 jobs with several test classes are not registered in zafira reporting service
			return;

		try {
			// remove slash at the end of ciUrl if any to register data in zafira without double slashing:
			// http://jenkins:8080/job/my_job//10
			// ->
			// http://jenkins:8080/job/my_job/10
			if (ciUrl.length() > 1) {
				if (ciUrl.endsWith("/")) {
					ciUrl = ciUrl.substring(0, ciUrl.length() - 1);
					LOGGER.debug("Updated ciUrl wihtout slash at end: " + ciUrl);
				}
			}
			user = registerUser(ciUserId, ciUserEmail, ciUserFirstName, ciUserLastName);

			job = registerJob(ciUrl, user.getId());

			// register suiteOwner
			UserType suiteOwner = registerUser(Ownership.getSuiteOwner(context));
			suite = registerTestSuite(context.getSuite().getName(), suiteFileName, suiteOwner.getId());

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

			
			UserType anonymousUser = null;
			
			if (ciBuildCause.toUpperCase().contains("UPSTREAMTRIGGER")) {
				// register/retrieve anonymous
				anonymousUser = registerUser(ANONYMOUS_USER);
				// register parentJob
				parentJob = registerJob(ciParentUrl, anonymousUser.getId());
			}
			
			if (ciBuildCause.toUpperCase().contains("UPSTREAMTRIGGER")) {
				run = registerTestRunUPSTREAM_JOB(suite.getId(), gitUrl,
						gitBranch, gitCommit, configXML, job.getId(),
						parentJob.getId(), parentBuild, build,
						Initiator.UPSTREAM_JOB, workItem);
			} else if (ciBuildCause.toUpperCase().contains("TIMERTRIGGER")) {
				run = registerTestRunBySCHEDULER(suite.getId(), gitUrl,
						gitBranch, gitCommit, configXML, job.getId(), build,
						Initiator.SCHEDULER, workItem);
			} else if (ciBuildCause.toUpperCase().contains("MANUALTRIGGER")) {
				run = registerTestRunByHUMAN(suite.getId(), user.getId(),
						gitUrl, gitBranch, gitCommit, configXML, job.getId(),
						build, Initiator.HUMAN, workItem);
			} else {
				throw new RuntimeException("Unable to register test run for zafira service: "
								+ zafiraUrl + " due to the misses build cause: '" + ciBuildCause + "'");
			}

			if (run == null) {
				throw new RuntimeException("Unable to register test run for zafira service: " + zafiraUrl);
			}
			isRegistered = true;
			
			if (rerunFailures) {
				tests = zc.getTestRunResults(run.getId()).getObject();
			}
		} catch (Exception e) {
			isRegistered = false;
			LOGGER.error("Undefined error during test run registration!", e);
		}

	}

	public static void finishSuite() {
		if (!isValid() || !isRegistered)
			return;

		try {
			finishTestRun();
		} catch (Exception e) {
			LOGGER.error("Undefined error during test run finish!", e);
		}
	}

	public static TestType finishTestMethod(ITestResult result, String message) {
		if (!isValid() || !isRegistered)
			return null;

		Status status = null;
		switch (result.getStatus()) {
		case ITestResult.SUCCESS:
			status = com.qaprosoft.zafira.client.model.TestType.Status.PASSED;
			break;
		case ITestResult.SKIP:
			status = com.qaprosoft.zafira.client.model.TestType.Status.SKIPPED;
			break;
		case ITestResult.FAILURE:
			status = com.qaprosoft.zafira.client.model.TestType.Status.FAILED;
			break;
		default:
			new RuntimeException("Undefined test result status! " + result.getStatus());
			break;
		}

		TestType registeredTest = null;
		try {
			String testClass = result.getMethod().getTestClass().getName();
			
			String test = TestNamingUtil.getCanonicalTestName(result);
			String testMethod = TestNamingUtil.getCanonicalTestMethodName(result);

			// if method owner is not specified then try to use suite owner. If
			// both are not declared then ANONYMOUS will be used
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
			TestNamingUtil.associateZafiraTest(registeredTest, Thread.currentThread().getId());
			
		} catch (Exception e) {
			isRegistered = false;
			LOGGER.error("Undefined error during test case/method finish!", e);
		}
		return registeredTest;
	}

	public static TestType registerWorkItems(Long testId, List<String> workItems) {
		if (!isValid() || !isRegistered)
			return null;

		Response<TestType> response = zc.createTestWorkItems(testId, workItems);
		return response.getObject();
	}
	
	public static long getRunId() {
		long runId = -1L;
		if (run != null) {
			LOGGER.debug("Run is not null");
			runId = run.getId();
		}
		LOGGER.debug("runId: " + runId);
		return runId;
	}
	
	public static boolean isRerun() {
		return rerunFailures;
	}

	public static TestType getTestType() {
		String testName = TestNamingUtil.getCanonicTestNameByThread();
		TestType res = null;
		for (TestType test : tests) {
			if (testName.equals(test.getName())) {
				res = test;
				break;
			}
		}
		return res;
	}

	public static void deleteTest(long id) {
		zc.deleteTest(id);
	}
	private static boolean isValid() {
		return !zafiraUrl.isEmpty() && !ciUrl.isEmpty() && zc.isAvailable();
	}

	private static UserType registerUser(String userName) {
		return registerUser(userName, null, null, null);
	}

	private static UserType registerUser(String userName, String email,
			String firstName, String lastName) {
		if (userName == null)
			userName = ANONYMOUS_USER;
		if (userName.isEmpty())
			userName = ANONYMOUS_USER;
		if (userName.equals("$BUILD_USER_ID"))
			userName = ANONYMOUS_USER;

		userName = userName.toLowerCase();
		
		String userDetails = "userName: %s, email: %s, firstName: %s, lastName: %s";
		LOGGER.debug("User details for registration:" + String.format(userDetails, userName, email, firstName, lastName));
		UserType regUser = new UserType(userName, email, firstName, lastName);
		Response<UserType> response = zc.createUser(regUser);
		regUser = response.getObject();

		if (regUser == null) {
			throw new RuntimeException("Unable to register user '" + userName + "' for zafira service: " + zafiraUrl);
		} else {
			LOGGER.debug("Registered user details:" + String.format(userDetails, regUser.getUserName(),
							regUser.getEmail(), regUser.getFirstName(), regUser.getLastName()));
		}

		return regUser;
	}

	private static JobType registerJob(String jobUrl, Long userId) {
		String jobName = getJenkinsJobName(jobUrl);
		String jenkinsHost = getJenkinsHost(jobUrl);
		String jobDetails = "jobName: %s, jenkinsHost: %s, userId: %s";
		LOGGER.debug("Job details for registration:" + String.format(jobDetails, jobName, jenkinsHost, userId));
		JobType regJob = new JobType(jobName, jobUrl, jenkinsHost, userId);
		Response<JobType> response = zc.createJob(regJob);

		regJob = response.getObject();

		if (regJob == null) {
			throw new RuntimeException("Unable to register job '" + ciUrl + "' for zafira service: " + zafiraUrl);
		} else {
			LOGGER.debug("Registered job details:" + String.format(jobDetails, regJob.getName(),
							regJob.getJenkinsHost(), regJob.getUserId()));
		}

		return regJob;
	}

	private static TestSuiteType registerTestSuite(String suiteName,
			String fileName, Long userId) {
		TestSuiteType testSuite = new TestSuiteType(suiteName, fileName, userId);
		String testSuiteDetails = "suiteName: %s, fileName: %s, userId: %s";
		LOGGER.debug("Test Suite details for registration:" + String.format(testSuiteDetails, suiteName, fileName, userId));
		Response<TestSuiteType> response = zc.createTestSuite(testSuite);
		testSuite = response.getObject();

		if (testSuite == null) {
			throw new RuntimeException("Unable to register test suite '" + suiteName + "' for zafira service: " + zafiraUrl);
		} else {
			LOGGER.debug("Registered test suite details:" + String.format(testSuiteDetails, testSuite.getName(),
							testSuite.getFileName(), testSuite.getUserId()));
		}

		return testSuite;
	}

	private static TestRunType registerTestRunByHUMAN(Long testSuiteId, Long userId, String scmURL, String scmBranch, String scmCommit,
			String configXML, Long jobId, Integer buildNumber, Initiator startedBy, String workItem) {
		TestRunType testRun = new TestRunType(testSuiteId, userId, scmURL, scmBranch, scmCommit, configXML, jobId, buildNumber, startedBy, workItem);
		String testRunDetails = "testSuiteId: %s, userId: %s, scmURL: %s, scmBranch: %s, scmCommit: %s, jobId: %s, buildNumber: %s, startedBy: %s, workItem";
		LOGGER.debug("Test Run details for registration:" + String.format(testRunDetails, testSuiteId, userId, scmURL,
						scmBranch, scmCommit, jobId, buildNumber, startedBy, workItem));
		
		testRun.setRerun(rerunFailures);
		Response<TestRunType> response = zc.createTestRun(testRun);
		testRun = response.getObject();
		if (testRun == null) {
			throw new RuntimeException("Unable to register test run '" + String.format(testRunDetails, testSuiteId, userId,
							scmURL, scmBranch, scmCommit, jobId, buildNumber, startedBy, workItem) + "' for zafira service: " + zafiraUrl);
		} else {
			LOGGER.debug("Registered test run details:" 
							+ String.format(testRunDetails, testSuiteId, userId, scmURL, scmBranch, scmCommit, jobId, buildNumber, startedBy, workItem));
		}
		return testRun;
	}

	private static TestRunType registerTestRunBySCHEDULER(Long testSuiteId, String scmURL, String scmBranch, String scmCommit,
			String configXML, Long jobId, Integer buildNumber, Initiator startedBy, String workItem) {
		TestRunType testRun = new TestRunType(testSuiteId, scmURL, scmBranch, scmCommit, configXML, jobId, buildNumber, startedBy, workItem);
		String testRunDetails = "testSuiteId: %s, scmURL: %s, scmBranch: %s, scmCommit: %s, jobId: %s, buildNumber: %s, startedBy: %s, workItem";
		LOGGER.debug("Test Run details for registration:" + String.format(testRunDetails, testSuiteId, scmURL, scmBranch,
						scmCommit, jobId, buildNumber, startedBy, workItem));

		testRun.setRerun(rerunFailures);
		Response<TestRunType> response = zc.createTestRun(testRun);
		testRun = response.getObject();
		if (testRun == null) {
			throw new RuntimeException("Unable to register test run '"
							+ String.format(testRunDetails, testSuiteId, scmURL,scmBranch, scmCommit, jobId, buildNumber, startedBy, workItem)
							+ "' for zafira service: " + zafiraUrl);
		} else {
			LOGGER.debug("Registered test run details:" 
							+ String.format(testRunDetails, testSuiteId, scmURL, scmBranch, scmCommit, jobId, buildNumber, startedBy, workItem));
		}
		return testRun;
	}

	private static TestRunType registerTestRunUPSTREAM_JOB(Long testSuiteId, String scmURL, String scmBranch, String scmCommit,
			String configXML, Long jobId, Long parentJobId, Integer parentBuildNumber, Integer buildNumber, Initiator startedBy, String workItem) {
		TestRunType testRun = new TestRunType(testSuiteId, scmURL, scmBranch, scmCommit, configXML, jobId, parentJobId, parentBuildNumber,
				buildNumber, startedBy, workItem);
		String testRunDetails = "testSuiteId: %s, scmURL: %s, scmBranch: %s, scmCommit: %s, jobId: %s, parentJobId: %s, parentBuildNumber: %s, buildNumber: %s, startedBy: %s, workItem";
		LOGGER.debug("Test Run details for registration:"
				+ String.format(testRunDetails, testSuiteId, scmURL, scmBranch, scmCommit, jobId, parentJobId, parentBuildNumber, buildNumber, startedBy, workItem));

		testRun.setRerun(rerunFailures);
		Response<TestRunType> response = zc.createTestRun(testRun);
		testRun = response.getObject();
		if (testRun == null) {
			throw new RuntimeException("Unable to register test run '"
					+ String.format(testRunDetails, testSuiteId, scmURL, scmBranch, scmCommit, jobId, parentJobId, parentBuildNumber, buildNumber, startedBy, workItem) 
							+ "' for zafira service: " + zafiraUrl);
		} else {
			LOGGER.debug("Registered test run details:"
					+ String.format(testRunDetails, testSuiteId, scmURL,scmBranch, scmCommit, jobId,parentJobId, parentBuildNumber, buildNumber, startedBy, workItem));
		}
		return testRun;
	}

	private static TestCaseType registerTestCase(String testClass, String testMethod, String info, Long testSuiteId, Long userId) {
		TestCaseType testCase = new TestCaseType(testClass, testMethod, info, testSuiteId, userId);
		String testCaseDetails = "testClass: %s, testMethod: %s, info: %s, testSuiteId: %s, userId: %s";
		LOGGER.debug("Test Case details for registration:"
				+ String.format(testCaseDetails, testClass, testMethod, info, testSuiteId, userId));
		Response<TestCaseType> response = zc.createTestCase(testCase);
		testCase = response.getObject();
		if (testCase == null) {
			throw new RuntimeException("Unable to register test case '"
					+ String.format(testCaseDetails, testClass, testMethod, info, testSuiteId, userId)
					+ "' for zafira service: " + zafiraUrl);
		} else {
			LOGGER.debug("Registered test case details:"
					+ String.format(testCaseDetails, testClass, testMethod, info, testSuiteId, userId));
		}
		return testCase;
	}

	private static TestType registerTest(String name, Status status,String testArgs, Long testRunId, Long testCaseId, String message,
			Long startTime, Long finishTime, String demoURL, String logURL) {
		
		int retry = RetryCounter.getRunCount(name);

		String testDetails = "name: %s, status: %s, testArgs: %s, testRunId: %s, testCaseId: %s, message: %s, startTime: %s, finishTime: %s, demoURL: %s, logURL: %s, retry: %d";
		
		//AUTO-1466; AUTO-1468
		if (retry > 0) {
			// delete previous test results from Zafira
			LOGGER.debug("Test details for removal due to the retry:"
					+ String.format(testDetails, name, status, testArgs, testRunId,
							testCaseId, message, startTime, finishTime, demoURL,
							logURL, retry));
			
			TestType test = new TestType(name, status, testArgs, testRunId, testCaseId, message, startTime, finishTime, demoURL, logURL, null, retry - 1);
			zc.deleteTestDuplicates(test);
		}
		
		// name:R, status:R, testArgs:NR, testRunId:R, testCaseId:R, message:NR,
		// startTime:NR, finishTime:NR, demoURL:NR, logURL:NR, workItems:NR
		TestType test = new TestType(name, status, testArgs, testRunId, testCaseId, message, startTime, finishTime, demoURL, logURL, null, retry);
		LOGGER.debug("Test details for registration:"
				+ String.format(testDetails, name, status, testArgs, testRunId,
						testCaseId, message, startTime, finishTime, demoURL,
						logURL, retry));

		Response<TestType> response = zc.createTest(test);
		test = response.getObject();
		if (test == null) {
			throw new RuntimeException("Unable to register test '"
					+ String.format(testDetails, name, status, testArgs, testRunId, testCaseId, message, startTime, finishTime, demoURL, logURL, retry)
					+ "' for zafira service: " + zafiraUrl);
		} else {
			LOGGER.debug("Registered test details:"
					+ String.format(testDetails, name, status, testArgs, testRunId,
							testCaseId, message, startTime, finishTime, demoURL,
							logURL, retry));			
		}
		return test;
	}

	private static TestRunType finishTestRun() {
		Response<TestRunType> response = zc.finishTestRun(run.getId());
		return response.getObject();
	}

	private static String getJenkinsHost(String jobURL) {
		if (jobURL.isEmpty()) {
			LOGGER.error("CI job URL is not valid! Verify that ci_url or ci_parent_url properties are specified!");
			return "";
		}
		return jobURL.contains(VIEW_PATTERN) ? jobURL.split(VIEW_PATTERN)[0] : jobURL.split(JOB_PATTERN)[0];
	}

	private static String getJenkinsJobName(String jobURL) {
		String[] items = jobURL.split("/");
		jobURL = items[items.length - 1]; // there is no need to verify on length > 0 as it always equals 1 even if string is empty

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
		arg.setUnique(isUnique(parameter.getKey()));
		arg.setValue(Configuration.get(parameter));
		return arg;
	}

	private static boolean isUnique(String key){

		if (uniqueKeys == null){
			String uniqueParams = Configuration.get(Configuration.Parameter.UNIQUE_TESTRUN_FIELDS);
			uniqueKeys = new ArrayList<>();
			if (!uniqueParams.isEmpty()){
				String[] split = uniqueParams.split(",");
				uniqueKeys.addAll(Arrays.asList(split));
			}
		}
		return uniqueKeys.contains(key);
	}

	@SuppressWarnings("unused")
	private static void setConfiguration(String configXML) {
		ConfigurationBin conf = MarshallerHelper.unmarshall(configXML, ConfigurationBin.class);
		// TODO: implement setter methods for all Configuration parameters
		for (ArgumentType arg : conf.getArg()) {
			LOGGER.debug("Key: " + arg.getKey() + " Value: " + arg.getValue());
		}
	}

}
