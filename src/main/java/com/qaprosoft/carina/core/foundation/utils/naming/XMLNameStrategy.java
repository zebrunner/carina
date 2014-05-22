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
import java.util.HashMap;
import java.util.Map;

import org.testng.ITestResult;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.parser.XLSDSBean;

public class XMLNameStrategy implements INamingStrategy
{
	private Map<String, String> testNameMappedToID = new HashMap<String, String>();
    private static final ThreadLocal<String> testLogId = new ThreadLocal<String>();
	
	@Override
	public String getCanonicalTestNameBeforeTest(XmlTest xmlTest, Method testMethod)
	{
		return xmlTest.getName();
	}

	@Override
	public String getCanonicalTestName(ITestResult result)
	{
		String logID = result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.TEST_LOG_ID);
		
		Thread thread = Thread.currentThread();
		if (thread.getName().contains("PoolService"))
		{
			logID = thread.getId() + "-" + logID;
		}
		
		startThread(logID);
		
		if(!testNameMappedToID.containsKey(logID))
		{
			String testName = result.getTestContext().getCurrentXmlTest().getName();
			XLSDSBean ds = new XLSDSBean(result.getTestContext());
		
			if(!ds.getArgs().isEmpty())
			{
				if(ds.getArgs().contains(SpecialKeywords.TUID))
				{
					testName = ds.getTestParams().get(SpecialKeywords.TUID) + " - " + testName + " [" + ds.argsToString() + "]";
				}
				else
				{
					if (ds.argsToString()!= null && !ds.argsToString().isEmpty())
						testName = testName + " [" + ds.argsToString() + "]";
				}
			} 
			
			if(ds.getArgs().isEmpty() && !ds.getUidArgs().isEmpty())
			{
				// retrieve {excel_ds_uid} fields from results->parameters->dynamicArgs Map
				// 0th element should be Map<String, String> as we generate such structure in AbstractTest::createTestArgSets2 for XLS Data provider
				if (result.getParameters().length > 0){
					if (result.getParameters()[0] instanceof Map)
					{
						@SuppressWarnings("unchecked")
						Map<String, String> testParams = (Map<String, String>) result.getParameters()[0];	
						String sTUID = testParams.get(SpecialKeywords.TUID);
						if (!sTUID.isEmpty())
						{
							testName = sTUID + " - " + testName + " [" + ds.argsToString(testParams) + "]";
						}
						else
						{
							testName = testName + " [" + ds.argsToString(testParams) + "]";						
						}
					}
				}
			}
		
			//LC - AUTO-274 "Pass"ing status set on emailable report when a test step fails
			String methodUID = "";
			for (int i = 0; i < result.getParameters().length; i++)
			{
				if (result.getParameters()[i] instanceof String)
				{
					if (result.getParameters()[i].toString().contains(SpecialKeywords.TUID + ":"))
					{
						methodUID = result.getParameters()[i].toString().replace(SpecialKeywords.TUID + ":", "");
					}
				}
			}

			if (result.getMethod().getInvocationCount() > 1){
				methodUID = String.valueOf(result.getMethod().getCurrentInvocationCount() + 1); 
			}
			

			if (!methodUID.isEmpty()){
				testNameMappedToID.put(logID, methodUID + " - " + testName + " - " +  result.getMethod().getMethodName());
			} else
			{
				testNameMappedToID.put(logID, testName + " - " +  result.getMethod().getMethodName());
			}
			
		}
		return testNameMappedToID.get(logID);
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
