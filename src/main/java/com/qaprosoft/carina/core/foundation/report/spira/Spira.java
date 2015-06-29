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
package com.qaprosoft.carina.core.foundation.report.spira;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	
	private static ThreadLocal<String> spiraSteps = new ThreadLocal<String>();
	
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

	public synchronized static void updateAfterTest(ITestResult result)
	{
		updateAfterTest(result, null);
	}
	
	public synchronized static void updateAfterTest(ITestResult result, String errorMessage)
	{
		if(isInitialized)
		{
			try
			{
				updater.updateAfterTest(result, errorMessage);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				LOGGER.error("Spira 'updateAfterTest' not performed: " + e.getMessage());
			}
		}
	}

	public synchronized static void updateAfterSuite(String testClass, TestResultType testResult, String message, String testName, long startDate)
	{
		if(isInitialized)
		{
			try
			{
				 updater.updateAfterSuite(testClass, testResult, message, testName, startDate);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				LOGGER.error("Spira 'updateAfterSuite' not performed: " + e.getMessage());
			}
		}
	}
	
	public static void registerStepsFromAnnotation(Method testMethod)
	{
		String testStepsId = "";
		// Extract the SpiraTest test case id - if present
		if (testMethod.isAnnotationPresent(SpiraTestSteps.class)) {
			SpiraTestSteps methodAnnotation = testMethod
					.getAnnotation(SpiraTestSteps.class);
			testStepsId = methodAnnotation.testStepsId();
		}
		
		spiraSteps.set(testStepsId);
	}
	
	/**
	 * Return current SpiraTest step(s) for Test Method.
	 * 
	 */
	
	public synchronized static List<String> getSteps() {
		List<String> stepList = new ArrayList<String>();
		String steps = spiraSteps.get();
		if (steps != null && !steps.isEmpty()) {
			stepList = Arrays.asList(spiraSteps.get().split(","));
		}
		
		for (int i = 0; i < stepList.size(); i++) {
			stepList.set(i, stepList.get(i).trim());
		}

		return stepList;
	}
	
	/**
	 * Set current Test Method SpiraTest step(s).
	 * 
	 * @param steps
	 *            to set
	 */
	public static void setSteps(String steps) {
		spiraSteps.set(steps);
	}
	
	/**
	 * Clear information about SpiraTest step(s).
	 * 
	 */
	public static void clear() {
		spiraSteps.remove();
	}
}
