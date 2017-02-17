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
package com.qaprosoft.carina.core.foundation.utils.naming;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.zafira.models.dto.TestType;

/**
 * Common naming utility for unique test method identification.
 * 
 * @author Aliaksei_Khursevich (hursevich@gmail.com)
 */
public class TestNamingUtil
{
	private static final Logger LOGGER = Logger.getLogger(TestNamingUtil.class);

	private static INamingStrategy namingStrategy;

	// private static final ConcurrentHashMap<Long, String> threadId2TestName = new ConcurrentHashMap<Long, String>();
	private static final ConcurrentHashMap<Long, Stack<String>> threadId2TestName = new ConcurrentHashMap<Long, Stack<String>>();
	private static final ConcurrentHashMap<Long, String> threadId2CanonicTestName = new ConcurrentHashMap<Long, String>(); // map
																															// to
																															// store
																															// test
																															// names
																															// without
																															// configuration
																															// steps

	private static final ConcurrentHashMap<Long, TestType> threadId2ZafiraTest = new ConcurrentHashMap<Long, TestType>();

	private static final ConcurrentHashMap<String, Long> testName2StartDate = new ConcurrentHashMap<String, Long>();
	private static final ConcurrentHashMap<String, Integer> testName2Counter = new ConcurrentHashMap<String, Integer>();

	private static final ConcurrentHashMap<String, String> testName2Bug = new ConcurrentHashMap<String, String>();

	static
	{
		try
		{
			namingStrategy = (INamingStrategy) Class.forName(R.CONFIG.get("naming_strategy")).newInstance();
		} catch (Exception e)
		{
			throw new RuntimeException("Can't create naming strategy: " + R.CONFIG.get("naming_strategy"));
		}
	}

	public static String getCanonicalTestNameBeforeTest(XmlTest xmlTest, Method testMethod)
	{
		return StringEscapeUtils.escapeHtml4(namingStrategy.getCanonicalTestNameBeforeTest(xmlTest, testMethod));
	}

	public static String getCanonicalTestName(ITestResult result)
	{
		return StringEscapeUtils.escapeHtml4(namingStrategy.getCanonicalTestName(result));
	}

	public static String getCanonicalTestMethodName(ITestResult result)
	{
		return StringEscapeUtils.escapeHtml4(namingStrategy.getCanonicalTestMethodName(result));
	}

	public static String getPackageName(ITestResult result)
	{
		return StringEscapeUtils.escapeHtml4(namingStrategy.getPackageName(result));
	}

	public static synchronized String associateTestInfo2Thread(String test, Long threadId)
	{
		// introduce invocation count calculation here as in multi threading mode TestNG doesn't provide valid
		// getInvocationCount() value
		int count = 1;
		if (testName2Counter.containsKey(test))
		{
			count = testName2Counter.get(test) + 1;
			LOGGER.warn(test + " test was already registered. Incrementing invocation count to " + count);
		}
		testName2Counter.put(test, count);

		if (count > 1)
		{
			// TODO: analyze if "InvCount=nnnn" is already present in name and don't append it one more time
			test = test + String.format(SpecialKeywords.INVOCATION_COUNTER, String.format("%04d", count));
		}

		Stack<String> stack = new Stack<String>();

		if (threadId2TestName.containsKey(threadId))
		{
			// not the first time
			stack = threadId2TestName.get(threadId);
		}
		stack.push(test);
		threadId2TestName.put(threadId, stack);
		testName2StartDate.put(test, new Date().getTime());
		return test;
	}

	public static synchronized void decreaseRetryCounter(String test)
	{
		// introduce invocation count calculation here as in multi threading mode TestNG doesn't provide valid
		// getInvocationCount() value
		if (testName2Counter.containsKey(test))
		{
			int count = testName2Counter.get(test) - 1;
			testName2Counter.put(test, count);
		}
	}

	public static synchronized void releaseTestInfoByThread()
	{
		long threadId = Thread.currentThread().getId();
		if (!isTestNameRegistered())
		{
			LOGGER.warn("There is no TestInfo for release in threadId: " + threadId);
			return;
		}

		Stack<String> stack = threadId2TestName.get(threadId);
		String test = stack.pop();
		LOGGER.debug("Releasing information about test: " + test);

		if (stack.isEmpty())
		{
			threadId2TestName.remove(threadId);
		}
	}

	public static boolean isTestNameRegistered()
	{
		long threadId = Thread.currentThread().getId();
		if (threadId2TestName.get(threadId) != null)
		{
			Stack<String> stack = threadId2TestName.get(threadId);
			if (stack.size() > 0)
			{
				String test = stack.get(stack.size() - 1);
				return test != null;
			}
		}
		return false;
	}

	public static String getTestNameByThread()
	{
		long threadId = Thread.currentThread().getId();

		Stack<String> stack = threadId2TestName.get(threadId);
		if (stack == null)
		{
			LOGGER.error("Unable to find registered test name for threadId: " + threadId + ". stack is null!");
			return null;
		}

		if (stack.size() == 0)
		{
			LOGGER.error("Unable to find registered test name for threadId from stack: " + threadId);
			return null;
		}

		return stack.get(stack.size() - 1);
	}

	public static Long getTestStartDate(String test)
	{
		Long startDate = testName2StartDate.get(test);
		if (startDate == null)
		{
			LOGGER.warn("Unable to find start date for test: '" + test + "'!");
		}
		return startDate;
	}

	public static synchronized void associateZafiraTest(TestType zafiraTest)
	{
		long threadId = Thread.currentThread().getId();
		if (zafiraTest == null)
		{
			LOGGER.error("NULL ZafiraTest is registered for thread: " + threadId);
			return;
		}
		LOGGER.debug("Associate ZafiraTest id: " + zafiraTest.getId() + "; name: " + zafiraTest.getName()
				+ " with thread: " + threadId);
		threadId2ZafiraTest.put(threadId, zafiraTest);
	}

	public static TestType getZafiraTest()
	{
		return threadId2ZafiraTest.get(Thread.currentThread().getId());
	}

	public static synchronized void releaseZafiraTest()
	{
		long threadId = Thread.currentThread().getId();
		TestType zafiraTest = threadId2ZafiraTest.get(threadId);
		if (zafiraTest != null)
		{
			LOGGER.debug("Releasing ZafiraTest id: " + zafiraTest.getId() + "; name: " + zafiraTest.getName()
					+ " from thread: " + threadId);
		} else
		{
			LOGGER.error("Unable to release ZafiraTest by threadId: " + threadId);
		}
		threadId2ZafiraTest.remove(threadId);
	}

	public static synchronized void associateCanonicTestName(String test)
	{
		threadId2CanonicTestName.put(Thread.currentThread().getId(), test);
	}

	public static String getCanonicTestNameByThread()
	{
		return getCanonicTestNameByThread(Thread.currentThread().getId());
	}

	public static String getCanonicTestNameByThread(long threadId)
	{
		return threadId2CanonicTestName.get(threadId);
	}

	public static synchronized void associateBug(String testName, String id)
	{
		testName2Bug.put(testName, id);
	}

	public static synchronized String getBug(String testName)
	{
		if (testName == null)
		{
			return null;
		}
		return testName2Bug.get(testName);
	}
}
