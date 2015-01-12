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
package com.qaprosoft.carina.core.foundation.report.spira;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/*
 * Spira
 * 
 */
public class Spira
{
	private static final Logger LOGGER = Logger.getLogger(Spira.class);
	private static ISpiraUpdater updater;
	private static boolean isInitialized = false;
	
	private static final LinkedHashMap<String, String> spiraStepsResult = new LinkedHashMap<String, String>();
	
	static
	{
		try
		{
			updater = (ISpiraUpdater) Class.forName(Configuration.get(Parameter.SPIRA_UPDATER)).newInstance();
			isInitialized = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOGGER.info("Spira update utility not initialized for '" + Configuration.get(Parameter.SPIRA_UPDATER) + "':" + e.getMessage());
		}
	}

	public static synchronized void putStepResult(String step, String result)
	{
		spiraStepsResult.put(step, result);
	}
	
	public static synchronized String getStepResult(String step)
	{
		return spiraStepsResult.get(step);
	}
	
	public static synchronized String getStepResults() {
		//combine all test steps into single String
		String res = "";
		for (Map.Entry<String,String> entry : spiraStepsResult.entrySet()) {
			res = res + entry.getKey() + " : " + entry.getValue() + "\n"; 
		}
		return res;
	}
	
	public synchronized static void updateAfterTest(ITestResult result)
	{
		updateAfterTest(result, null);
	}
	
	public synchronized static void updateAfterTest(ITestResult result, Throwable thr)
	{
		if(isInitialized)
		{
			try
			{
				updater.updateAfterTest(result, thr);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				LOGGER.error("Spira 'updateAfterTest' not performed: " + e.getMessage());
			}
		}
	}

	public synchronized static void updateAfterSuite(String testClass, TestResultType testResult, String message, String testName, String details, long startDate)
	{
		if(isInitialized)
		{
			try
			{
				 updater.updateAfterSuite(testClass, testResult, message, testName, details, startDate);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				LOGGER.error("Spira 'updateAfterSuite' not performed: " + e.getMessage());
			}
		}
	}
}
