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
package com.qaprosoft.carina.core.foundation.webdriver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverPool
{
	private static Map<WebDriver, String> driverTestMap = Collections.synchronizedMap(new HashMap<WebDriver, String>());

	private static Map<String, WebDriver> sessionIdDriverMap = Collections.synchronizedMap(new HashMap<String, WebDriver>());

	public static synchronized String registerDriverSession(WebDriver driver)
	{
		String sessionId = ((RemoteWebDriver) driver).getSessionId().toString();
		sessionIdDriverMap.put(sessionId, driver);
		return sessionId;
	}
	
	public static synchronized void associateTestNameWithDriver(String test, WebDriver driver)
	{
		//testDriverMap.put(testInClass, driver);
		driverTestMap.put(driver, test);
	}

/*	public static synchronized WebDriver getDriverByTestName(String test)
	{
		return testDriverMap.get(test);
	}*/

	public static WebDriver getDriverBySessionId(String sessionId)
	{
		return sessionIdDriverMap.get(sessionId);
	}

	/*public static String getSessionIdByTestName(String test)
	{
		if (testDriverMap.containsKey(test))
		{
			RemoteWebDriver driver = (RemoteWebDriver) testDriverMap.get(test);
			return driver.getSessionId().toString();
		} else
		{
			return null;
		}
	}*/

	public static String getTestNameByDriver(WebDriver driver)
	{
		return driverTestMap.containsKey(driver) ? driverTestMap.get(driver) : null;
	}
}
