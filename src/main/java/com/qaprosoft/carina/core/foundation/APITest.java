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
package com.qaprosoft.carina.core.foundation;

import java.lang.reflect.Method;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.api.APIMethodBuilder;
import com.qaprosoft.carina.core.foundation.listeners.APITestListener;

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
    	apiMethodBuilder = new APIMethodBuilder();
    }
    
    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result)
    {
		super.executeAfterTestMethod(result);    	
    }
	
}
