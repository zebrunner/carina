package com.qaprosoft.carina.core.foundation.utils.ownership;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

public class Ownership {
	
	protected static final Logger LOGGER = Logger.getLogger(Ownership.class);
	
	public static String getMethodOwner(ITestResult result) {
		//Get a handle to the class and method
		Class<?> testClass;
		String owner = "";
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
				//Extract the MethodOwner owner - if present
				if (testMethod.isAnnotationPresent(MethodOwner.class))
				{
					MethodOwner methodAnnotation = testMethod.getAnnotation(MethodOwner.class);
					owner = methodAnnotation.owner();
					LOGGER.info("Method " + testMethod + " owner is " + owner);
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return owner;
	}
	
	public static String getSuiteOwner(ITestContext context) {
		return context.getSuite().getParameter("suiteOwner");
	}
}