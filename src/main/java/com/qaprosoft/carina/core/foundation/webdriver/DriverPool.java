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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class DriverPool
{
	private static final Logger LOGGER = Logger.getLogger(DriverPool.class);
	
	static WebDriver single_driver;
	private static final ConcurrentHashMap<Long, WebDriver> threadId2Driver = new ConcurrentHashMap<Long, WebDriver>();

	public static synchronized void registerDriver2Thread(WebDriver driver, Long threadId)
	{
		threadId2Driver.put(threadId, driver);
		if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.SUITE_MODE) {
			//init our single driver variable
			single_driver = driver;
		}

		LOGGER.debug("##########   REGISTER threadId: " + threadId + "; driver: " + driver);
	}
	
	public static WebDriver getDriverByThread(long threadId)
	{
		WebDriver drv = null;
		if (threadId2Driver.containsKey(threadId)) {
			drv = threadId2Driver.get(threadId);
			LOGGER.debug("##########        GET threadId: " + threadId + "; driver: " + drv);
		}
		else if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.SUITE_MODE) {
			LOGGER.debug("########## Unable to find driver by threadId: " + threadId);
			//init our single driver variable
			drv = single_driver;
		}
		return drv;
	}
	
	public static synchronized void deregisterDriverByThread(long threadId)
	{
		if (threadId2Driver.containsKey(threadId)) {
			WebDriver drv = threadId2Driver.get(threadId);
			LOGGER.debug("########## DEREGISTER threadId: " + threadId + "; driver: " + drv);
			threadId2Driver.remove(threadId);
		}
	}
	
	public static void replaceDriver(WebDriver driver) {
		long threadId = Thread.currentThread().getId();
		threadId2Driver.remove(threadId);
		registerDriver2Thread(driver, threadId);
	}
	
	public static WebDriver getSingleDriver() {
		return single_driver;
	}
	
}
