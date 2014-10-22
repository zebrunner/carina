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
package com.qaprosoft.carina.core.foundation;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.api.APIMethodBuilder;
import com.qaprosoft.carina.core.foundation.listeners.APITestListener;
import com.qaprosoft.carina.core.foundation.log.GlobalTestLog;
import com.qaprosoft.carina.core.foundation.log.GlobalTestLog.Type;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

@Listeners({ APITestListener.class })
public abstract class APITest extends AbstractTest
{
    protected APIMethodBuilder apiMethodBuilder;
    
	@Override
	protected boolean isUITest()
	{
		return false;
	}
	
    @BeforeMethod(alwaysRun = true)
    public void executeBeforeTestMethod(XmlTest xmlTest, Method testMethod, ITestContext context) throws Throwable
    {
    	super.executeBeforeTestMethod(xmlTest, testMethod, context);
    	
		try
		{
		    TEST_EXECUTER_LOG = executionContext.initBeforeTest(xmlTest.getName());
		    apiMethodBuilder = new APIMethodBuilder();
		}
		catch (Throwable thr)
		{
			LOGGER.error(thr.getMessage());
		    context.setAttribute(SpecialKeywords.INITIALIZATION_FAILURE, thr);
		    throw thr;
		}
    }
    
    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result)
    {
		super.executeAfterTestMethod(result);    	
		try
		{
			GlobalTestLog glblLog = ((GlobalTestLog) result.getAttribute(GlobalTestLog.KEY));
			String testName = TestNamingUtil.getCanonicalTestName(result);		    
		    File testLogFile = new File(ReportContext.getTestDir(testName) + "/test.log");
		    if (!testLogFile.exists()) testLogFile.createNewFile();
		    FileWriter fw = new FileWriter(testLogFile);
		    
		    try
		    {
			    if (!StringUtils.isEmpty(glblLog.readLog(Type.SOAP)))
			    {
					fw.append("\r\n************************** SoapUI logs **************************\r\n\r\n");
					fw.append(glblLog.readLog(Type.SOAP));
			    }
			    
				
			    if (apiMethodBuilder != null)
			    {
			    	if (apiMethodBuilder.getTempFile().exists())
			    	{
						String tempLog = FileUtils.readFileToString(apiMethodBuilder.getTempFile());
						if (!StringUtils.isEmpty(glblLog.readLog(Type.REST)) || !StringUtils.isEmpty(tempLog))
						{
						    fw.append("\r\n*********************** Rest-Assured logs ***********************\r\n\r\n");
						    fw.append(tempLog);
						    fw.append(glblLog.readLog(Type.REST));
						}
			    	} 
			    	apiMethodBuilder.close();
			    }
					
		    }
		    catch (Exception e)
		    {
		    	//LOGGER.error("Error during FileWriter close. " + e.getMessage());
		    	LOGGER.debug("Error during FileWriter append. " + e.getMessage(), e.getCause());
		    	//e.printStackTrace();
		    }
		    finally {
		    	try {
		    		fw.close();
		    	} catch (Exception e) {
		    		LOGGER.debug(e.getMessage(), e.getCause());
		    	}
		    }
		    
		}
		catch (Exception e)
		{
		    LOGGER.error("Exception in API->executeAfterTestMethod");
		    e.printStackTrace();
		}		
    }
	
}
