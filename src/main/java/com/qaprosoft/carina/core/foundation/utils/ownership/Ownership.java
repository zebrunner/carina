package com.qaprosoft.carina.core.foundation.utils.ownership;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;

public class Ownership 
{
	protected static final Logger LOGGER = Logger.getLogger(Ownership.class);
	
	public enum OwnerType {PRIMARY, SECONDARY};
	
	@SuppressWarnings("unlikely-arg-type")
	public static String getMethodOwner(ITestResult result, OwnerType type) 
	{
		
		@SuppressWarnings("unchecked")
		Map<Object[], String> testMethodOwnerArgsMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.TEST_METHOD_OWNER_ARGS_MAP);
		if (testMethodOwnerArgsMap != null) {
			String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));					
			if (testMethodOwnerArgsMap.containsKey(testHash) && testMethodOwnerArgsMap.get(testHash) != null) {
				return testMethodOwnerArgsMap.get(testHash);
			}
		}
		
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
					switch (type) 
					{
					case PRIMARY:
						owner = methodAnnotation.owner();
						LOGGER.debug("Method " + testMethod + " primary owner is " + owner);
						break;

					case SECONDARY:
						owner = methodAnnotation.secondatyOwner();
						LOGGER.debug("Method " + testMethod + " secondary owner is " + owner);
						break;
					}
					
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return owner;
	}
	
	public static String getSuiteOwner(ITestContext context) {
		String owner = context.getSuite().getParameter("suiteOwner");
		if (owner == null) {
			owner = "";
		}
		return owner;
	}
}