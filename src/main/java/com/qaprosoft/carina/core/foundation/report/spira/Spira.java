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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
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
	
	public synchronized static List<String> getSteps(ITestResult result)
	{
		List<String> steps = getStepsIdFromDataProvider(result); //higher priority
		
		if (steps.size() == 0)
			steps = getStepsIdFromAnnotation(result); //lower priority

		return steps;
	}
	
	private static List<String> getStepsIdFromDataProvider(ITestResult result) {
		List<String> steps = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Map<Object[], String> testNameSpiraMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.SPIRA_ARGS_MAP);
		if (testNameSpiraMap != null) {
			String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));					
			if (testNameSpiraMap.containsKey(testHash) && testNameSpiraMap.get(testHash) != null) {
				steps = new ArrayList<String>(Arrays.asList(testNameSpiraMap.get(testHash).split(",")));
			}
		}
		return steps;
	}
	private static List<String> getStepsIdFromAnnotation(ITestResult result) {
		// Get a handle to the class and method
		Class<?> testClass;
		String testStepsId = "";
		try {
			testClass = Class.forName(result.getMethod().getTestClass()
					.getName());

			// We can't use getMethod() because we may have parameterized tests
			// for which we won't know the matching signature
			String methodName = result.getMethod().getMethodName();
			Method testMethod = null;
			Method[] possibleMethods = testClass.getMethods();
			for (Method possibleMethod : possibleMethods) {
				if (possibleMethod.getName().equals(methodName)) {
					testMethod = possibleMethod;
					break;
				}
			}
			if (testMethod != null) {
				// Extract the SpiraTest test case id - if present
				if (testMethod.isAnnotationPresent(SpiraTestSteps.class)) {
					SpiraTestSteps methodAnnotation = testMethod
							.getAnnotation(SpiraTestSteps.class);
					testStepsId = methodAnnotation.testStepsId();
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return new ArrayList<String>(Arrays.asList(testStepsId.split(",")));
	}
}
