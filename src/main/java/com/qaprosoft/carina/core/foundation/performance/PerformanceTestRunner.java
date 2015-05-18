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
package com.qaprosoft.carina.core.foundation.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.exception.InvalidArgsException;
import com.qaprosoft.carina.core.foundation.exception.TestCreationException;
import com.qaprosoft.carina.core.foundation.listeners.TestArgsListener;
import com.qaprosoft.carina.core.foundation.performance.PerformanceTestResult.Status;
import com.qaprosoft.carina.core.foundation.report.HtmlReportGenerator;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.email.EmailManager;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/*
 * PerformanceTestRunner - parses test arguments and launches performance test according to speified configuration.
 * 
 * @author Alex Khursevich
 */
@Listeners({ TestArgsListener.class })
public class PerformanceTestRunner
{
	protected static final Logger LOG = Logger.getLogger(PerformanceTestRunner.class);
	
	public static final String TEST_KEY = "testName";
	private static final String SUB_TESTS = "subtests";
	private static final String ARG_GROUP_SEPARATOR = ";";
	private static final String ARG_SEPARATOR = "-";

	private Object[][] loadSettings;
	private ExecutorService taskExecutor;
	private List<TestStatistics> testStatistics;
	private Map<String, String> testParams;
	private List<String> subtests;

	/**
	 * Prepare arguments
	 * 
	 * @throws InvalidArgsException
	 *             if input arguments are invalid.
	 */
	@BeforeSuite
	public void beforeSuite() throws InvalidArgsException
	{
		PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

		loadSettings = parseLoadSettings(Configuration.get(Parameter.LOAD_SETTINGS));
		testStatistics = new ArrayList<TestStatistics>();
		
		LOG.info(Configuration.asString());
		Configuration.validateConfiguration();
		
	}
	
	@SuppressWarnings("deprecation")
	@BeforeMethod(alwaysRun = true)
	public void executeBeforeTestMethod(XmlTest xmlTest) throws InvalidArgsException
	{
		testParams = xmlTest.getParameters();
		if(testParams.containsKey(SUB_TESTS))
		{
			subtests = new ArrayList<String>(Arrays.asList(testParams.get(SUB_TESTS).split(";")));
		}
		else
		{
			throw new InvalidArgsException("Parameter 'subtests' should be specified in XML!");
		}
	}

	@AfterSuite
	public void afterSuite()
	{
		String report = HtmlReportGenerator.generatePerformanceReport(ReportContext.getBaseDir().getAbsolutePath(), testStatistics);
		// Send report for specified emails
		EmailManager.send("Performance report", report, Configuration.get(Parameter.EMAIL_LIST),
				Configuration.get(Parameter.SENDER_EMAIL), Configuration.get(Parameter.SENDER_PASSWORD));
	}

	@DataProvider(name = "args")
	public Object[][] createData()
	{
		return loadSettings;
	}

	@Test(dataProvider = "args")
	public void testRun(String pUserLimit, String pLoopLimit, String pRampUpTime) throws InterruptedException, TestCreationException,
			ExecutionException
	{
		int userLimit = Integer.valueOf(pUserLimit);
		int loopLimit = Integer.valueOf(pLoopLimit);
		long rampUpTime = Long.valueOf(pRampUpTime);

		taskExecutor = Executors.newFixedThreadPool(userLimit);

		final long rampUpInterval = rampUpTime * 1000 / userLimit;

		for (int loop = 0; loop < loopLimit; loop++)
		{

			TestStatistics ts = new TestStatistics(testParams.get(TEST_KEY), userLimit);
			ts.setUsers(userLimit);
			ts.setLoop(loop + 1);
			ts.setRumpup(rampUpTime);
			
			LOG.info(String.format("==================== Loop: %d (%d users) ====================", loop + 1, userLimit));

			CountDownLatch latch = new CountDownLatch(userLimit);

			List<Future<PerformanceTestResult>> testResults = new ArrayList<Future<PerformanceTestResult>>();

			for (int user = 0; user < userLimit; user++)
			{
				testResults.add(taskExecutor.submit(TestCreator.createTask(testParams.get(TEST_KEY), latch, testParams)));
				Thread.sleep(rampUpInterval);
			}

			latch.await();
			
			for(String subtest : subtests)
			{
				ts.getSubTestStatistics().put(subtest, new TestStatistics(subtest, userLimit));
			}
			
			for (Future<PerformanceTestResult> testResult : testResults)
			{
				for(String subtest : subtests)
				{
					if(testResult.get().getSubTestResuts().containsKey(subtest) &&
							Status.PASS.equals(testResult.get().getSubTestResuts().get(subtest).getStatus()))
					{
						ts.getSubTestStatistics().get(subtest).addStatistics(testResult.get().getSubTestResuts().get(subtest).getTimer());
					}
				}
				if(Status.PASS.equals(testResult.get().getStatus()))
				{
					ts.addStatistics(testResult.get().getTimer());
				}
			}

			testStatistics.add(ts);
		}

	}

	/**
	 * Parses incoming test arguments.
	 * 
	 * @param testArgs input arguments
	 * @return parsed arguments
	 * @throws InvalidArgsException
	 *             if invalid arguments
	 */
	private Object[][] parseLoadSettings(String testArgs) throws InvalidArgsException
	{

		if (StringUtils.isEmpty(testArgs) || testArgs.split(ARG_GROUP_SEPARATOR).length == 0)
			throw new InvalidArgsException();

		String[] groups = testArgs.split(ARG_GROUP_SEPARATOR);

		Object[][] args = new Object[groups.length][3];

		for (int i = 0; i < groups.length; i++)
		{

			if (groups[i].split(ARG_SEPARATOR).length != 3)
				throw new InvalidArgsException();

			try
			{

				if (Integer.valueOf(groups[i].split(ARG_SEPARATOR)[0]) <= 0)
					throw new InvalidArgsException("Invalid args: users should be > 0!");
				args[i][0] = groups[i].split(ARG_SEPARATOR)[0];

				if (Integer.valueOf(groups[i].split(ARG_SEPARATOR)[1]) < 1)
					throw new InvalidArgsException("Invalid args: loops should be >= 1!");
				args[i][1] = groups[i].split(ARG_SEPARATOR)[1];

				if (Long.valueOf(groups[i].split(ARG_SEPARATOR)[2]) < 0)
					throw new InvalidArgsException("Invalid args: rampUp should be > 0!");
				args[i][2] = groups[i].split(ARG_SEPARATOR)[2];

			}
			catch (Exception e)
			{
				throw new InvalidArgsException(e.getMessage());
			}
		}
		return args;
	}
}
