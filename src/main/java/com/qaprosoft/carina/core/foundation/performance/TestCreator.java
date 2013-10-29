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
package com.qaprosoft.carina.core.foundation.performance;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.exception.TestCreationException;
import com.qaprosoft.carina.core.foundation.webdriver.DriverFactory;

/*
 * TestCreator - creates performance test instance using reflection.
 * 
 * @author Alex Khursevich
 */
public class TestCreator
{
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Callable<PerformanceTestResult> createTask(String testName, CountDownLatch latch, Map<String, String> testParams)
			throws TestCreationException
	{
		Object test = null;
		
		try
		{
			Class cls = Class.forName(testName);
			Class[] partypes = new Class[3];
			partypes[0] = WebDriver.class;
			partypes[1] = CountDownLatch.class;
			partypes[2] = Map.class;
			Constructor ct = cls.getConstructor(partypes);
			test = ct.newInstance(DriverFactory.create(testName), latch, testParams);
		}
		catch (Exception e)
		{
			throw new TestCreationException(e.getMessage());
		}

		return (Callable<PerformanceTestResult>) test;
	}
}
