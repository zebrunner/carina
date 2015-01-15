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
package com.qaprosoft.carina.core.foundation.report.testrail;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import org.apache.log4j.Logger;
import org.testng.ITestResult;

/*
 * TODO: Add Java doc
 * TestRail
 * 
 */


public class TestRail
{
	private static final Logger LOGGER = Logger.getLogger(TestRail.class);
	private static ITestRailUpdater updater;
	private static boolean isInitialized = false;
	
	static
	{
		try
		{
			//TODO: add property 
			updater = (ITestRailUpdater) Class.forName(Configuration.get(Parameter.TESTRAIL_UPDATER)).newInstance();
			isInitialized = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOGGER.info("TestRail update utility not initialized for '" + Configuration.get(Parameter.TESTRAIL_UPDATER) + "':" + e.getMessage());
		}
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
				LOGGER.error("TestRail 'updateAfterTest' not performed: " + e.getMessage());
			}
		}
	}

	public synchronized static void updateBeforeSuite(String testClass)
	{
		if(isInitialized)
		{
			try
			{
				 updater.updateBeforeSuite(testClass);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				LOGGER.error("TestRail 'updateAfterSuite' not performed: " + e.getMessage());
			}
		}
	}
}
