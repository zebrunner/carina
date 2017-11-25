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
package com.qaprosoft.carina.core.foundation.report.testrail;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
	private static ThreadLocal<List<String>> casesIds =  ThreadLocal.withInitial(ArrayList::new);
	
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
				LOGGER.error("TestRail 'updateAfterTest' not performed: " + e.getMessage());
			}
		}
	}

	public synchronized static void updateBeforeSuite(ITestContext context, String testClass, String title)
	{
		if(isInitialized)
		{
			try
			{
				 updater.updateBeforeSuite(context, testClass, title);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				LOGGER.error("TestRail 'updateAfterSuite' not performed: " + e.getMessage());
			}
		}
	}
	
	public synchronized static List<String> getCases(ITestResult result)
	{

		if(casesIds.get().isEmpty()){
			casesIds.set(getCasesIdFromDataProvider(result));

		}
		if (casesIds.get().isEmpty()) {
			casesIds.set(getCasesIdFromAnnotation(result));
		}

		return casesIds.get();



	}

	public static void setCasesID(String... cases){
		for (String _case : cases) {
			casesIds.get().add(_case);
		}
	}

	private static List<String> getCasesIdFromDataProvider(ITestResult result) {
		List<String> cases = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Map<Object[], String> testNameTestRailMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.TESTRAIL_ARGS_MAP);
		if (testNameTestRailMap != null) {
			String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));					
			if (testNameTestRailMap.containsKey(testHash) && testNameTestRailMap.get(testHash) != null) {
				cases = new ArrayList<String>(Arrays.asList(testNameTestRailMap.get(testHash).split(",")));
			}
		}
		return cases;
	}
	
	private static List<String> getCasesIdFromAnnotation(ITestResult result) {
        Class<?> testClass;
        String testCasesId = "";
        try {
            testClass = Class.forName(result.getMethod().getTestClass().getName());

            //We can't use getMethod() because we may have parameterized tests
            //for which we won't know the matching signature
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
                //Extract the TestRailCases test case id - if present
                if (testMethod.isAnnotationPresent(TestRailCases.class)) {
                    TestRailCases methodAnnotation = testMethod.getAnnotation(TestRailCases.class);
                    testCasesId = methodAnnotation.testCasesId();
                }
            }
                
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
		
		return new ArrayList<String>(Arrays.asList(testCasesId.split(",")));
	}

	public static void clearCases() {

		casesIds.set(new ArrayList<String>());
	}
}