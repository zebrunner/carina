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
package com.qaprosoft.carina.core.foundation.utils.naming;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Common naming utility for unique test method identification.
 * 
 * @author: Aliaksei_Khursevich (hursevich@gmail.com)
 */
public class TestNamingUtil
{
	private static INamingStrategy namingStrategy;

	private static final ConcurrentHashMap<Long, String> threadId2TestName = new ConcurrentHashMap<Long, String>();
	private static final ConcurrentHashMap<String, Long> testName2StartDate = new ConcurrentHashMap<String, Long>();
	
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

	public static String getPackageName(ITestResult result)
	{
		return StringEscapeUtils.escapeHtml4(namingStrategy.getPackageName(result));
	}
	
	public static synchronized void accociateTest2Thread(String test, Long threadId)
	{
		threadId2TestName.put(threadId, test);
	}
	
	public static synchronized void releaseTestFromThread(Long threadId)
	{
		threadId2TestName.remove(threadId);
	}
	
	public static String getTestByThread(Long threadId)
	{
		return threadId2TestName.get(threadId);
	}
	
	public static synchronized void accociateTestStartDate(String test)
	{
		testName2StartDate.put(test, new Date().getTime());
	}
	
	public static Long getTestStartDate(String test)
	{
		return testName2StartDate.get(test);
	}
	
	public static synchronized void releaseTestStartDate(String test)
	{
		testName2StartDate.remove(test);
	}	
	
}
