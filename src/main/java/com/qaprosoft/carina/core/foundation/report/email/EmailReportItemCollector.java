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
package com.qaprosoft.carina.core.foundation.report.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

/**
 * EmailReportGenerator generates emailable report using data from test suite log.
 * 
 * @author Alex Khursevich
 */
public class EmailReportItemCollector
{
	private static Map<String, TestResultItem> emailResultsMap = new HashMap<String, TestResultItem>();
	private static Map<String, TestResultItem> testResultsMap = new HashMap<String, TestResultItem>();
	private static List<String> createdItems = new ArrayList<String>();

	public static synchronized void push(TestResultItem emailItem)
	{
		if (!emailResultsMap.containsKey(emailItem.hash()))
		{
			emailResultsMap.put(emailItem.hash(), emailItem);
			testResultsMap.put(emailItem.getTest(), emailItem);
		}
	}

	public static synchronized void push(String itemToDelete)
	{
		if (!createdItems.contains(itemToDelete))
		{
			createdItems.add(itemToDelete);
		}
	}
	
	public static synchronized TestResultItem pull(ITestResult result)
	{
		return testResultsMap.get(TestNamingUtil.getCanonicalTestName(result));
	}

	public static List<TestResultItem> getTestResults()
	{
		return new ArrayList<TestResultItem>(emailResultsMap.values());
	}

	public static List<String> getCreatedItems()
	{
		return createdItems;
	}
}
