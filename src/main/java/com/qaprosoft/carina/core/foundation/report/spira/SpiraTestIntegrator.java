package com.qaprosoft.carina.core.foundation.report.spira;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class SpiraTestIntegrator {
	protected static final Logger LOGGER = Logger.getLogger(SpiraTestIntegrator.class);

    //spira logging
	public static void logTestCaseInfo(String className) {
		//Extract the SpiraTest configuration data - if present
		Class<?> testClass;
		try {
			testClass = Class.forName(className);
			if (testClass.isAnnotationPresent(SpiraTestCase.class))
			{
				SpiraTestCase classAnnotation = testClass.getAnnotation (SpiraTestCase.class);
				int testcaseId = classAnnotation.testCaseId();
				
				LOGGER.info(SpecialKeywords.SPIRA_RELEASE_ID + "::" + Configuration.get(Parameter.SPIRA_RELEASE_ID));
				LOGGER.info(SpecialKeywords.SPIRA_TESTSET_ID + "::" + Configuration.get(Parameter.SPIRA_TESTSET_ID));
				LOGGER.info(SpecialKeywords.SPIRA_TESTCASE_ID + "::" + testcaseId);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void logTestStepsInfo(ITestResult result) {
		logTestStepsInfo(result, null);
	}
	
	public static void logTestStepsInfo(ITestResult result, Throwable thr) {
	    //Spira test steps integration
		//Get a handle to the class and method
		Class<?> testClass;
		try {
			testClass = Class.forName(result.getMethod().getTestClass().getName());
			
			//We can't use getMethod() because we may have parameterized tests
			//for which we won't know the matching signature
			String methodName = result.getMethod().getMethodName();
			Method testMethod = null;
			Method[] possibleMethods = testClass.getMethods();
			for (Method possibleMethod : possibleMethods)
			{
				if (possibleMethod.getName().equals(methodName))
				{
					testMethod = possibleMethod;
					break;
				}
			}
		
			if (testMethod != null)
			{
				//Extract the SpiraTest test case id - if present
				if (testMethod.isAnnotationPresent(SpiraTestSteps.class))
				{
					SpiraTestSteps methodAnnotation = testMethod.getAnnotation(SpiraTestSteps.class);
					String testStepsIs = methodAnnotation.testStepsId();
					String[] steps = testStepsIs.split(",");
					if (thr == null) {
						//all steps passed
						for (String step : steps) {
							LOGGER.info(SpecialKeywords.SPIRA_TESTSTEP_ID + "::" + step.trim() + "::Passed");
						}
					} else {
						boolean foundFailureStep = false;
						String errorMessage = thr.getMessage();
						
						//identify failure step and mark before steps as Passed, after them as Blocked
						//if there is no way to identify failure step then mark all steps as Failed
						for (String step : steps) {
							if (errorMessage.contains(step.trim())) {
								foundFailureStep = true;
							}
						}
						if (!foundFailureStep) {
							for (String step : steps) {
								LOGGER.info(SpecialKeywords.SPIRA_TESTSTEP_ID + "::" + step.trim() + "::N/A::" + errorMessage);
							}							
						} else
						{
							foundFailureStep = false;		
							for (String step : steps) {
								step = step.trim();
								if (errorMessage.contains(step)) {
									foundFailureStep = true;
									LOGGER.info(SpecialKeywords.SPIRA_TESTSTEP_ID + "::" + step + "::Failed::" + errorMessage);
								}
								else
								{
									if (!foundFailureStep) {
										LOGGER.info(SpecialKeywords.SPIRA_TESTSTEP_ID + "::" + step.trim() + "::Passed");
									}
									else {
										LOGGER.info(SpecialKeywords.SPIRA_TESTSTEP_ID + "::" + step.trim() + "::Blocked::" + errorMessage);
									}
								}
							}
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}	
}
