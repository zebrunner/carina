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
import java.util.Arrays;
import java.util.Map;

import org.testng.ITestResult;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;


public class XMLNameStrategy implements INamingStrategy
{
    private static final ThreadLocal<String> testLogId = new ThreadLocal<String>();
	
	@Override
	public String getCanonicalTestNameBeforeTest(XmlTest xmlTest, Method testMethod)
	{
		return xmlTest.getName();
	}

	@Override
	public String getCanonicalTestName(ITestResult result) {
		//verify if testName is already registered with thread then return it back
		String testName = TestNamingUtil.getTestNameByThread(Thread.currentThread().getId());
		if (testName != null) {
			return testName;
		}
		
		@SuppressWarnings("unchecked")
		Map<Object[], String> testnameMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP);
		
		if (testnameMap != null) {
			String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));			
			if (testnameMap.containsKey(testHash)) {
				testName = testnameMap.get(testHash);
			}
		}
		
		if (testName == null)
			testName = result.getTestContext().getCurrentXmlTest().getName();
		
		
		if (result.getTestContext().getCurrentXmlTest().getTestParameters().containsKey(SpecialKeywords.EXCEL_DS_CUSTOM_PROVIDER) || 
				result.getTestContext().getCurrentXmlTest().getTestParameters().containsKey(SpecialKeywords.DS_CUSTOM_PROVIDER)) {
			//LC - AUTO-274 "Pass"ing status set on emailable report when a test step fails
			String methodUID = "";
			for (int i=0; i<result.getParameters().length; i++) {
				  if (result.getParameters()[i] != null) {
					  if (result.getParameters()[i].toString().contains(SpecialKeywords.TUID + ":")) {
						  methodUID = result.getParameters()[i].toString().replace(SpecialKeywords.TUID + ":", "");
						  break; //first TUID: parameter is used
					  }
				  }
			}
			if (!methodUID.isEmpty()) {
				testName = methodUID + " - " + testName;
			}
		}
		
		int invocationID = -1;
		if (result.getMethod().getInvocationCount() > 1){
			invocationID = result.getMethod().getCurrentInvocationCount() + 1; 
		}
		
		if (invocationID != -1) {
			testName = testName + " - " +  result.getMethod().getMethodName() + String.format(SpecialKeywords.INVOCATION_COUNTER, String.format("%03d", invocationID));
		}
		else {
			testName = testName + " - " +  result.getMethod().getMethodName();
		}

		return testName;
	}

	@Override
	public String getPackageName(ITestResult result)
	{
		return result.getMethod().getRealClass().getPackage().getName();
	}
	
    public static void startThread(String id) {
        testLogId.set(id);
    }
 
    public static String getThreadId() {
        return testLogId.get();
    }
 
    public static void endThread() {
    	testLogId.remove();
    }
}
