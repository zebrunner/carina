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
package com.qaprosoft.carina.core.foundation.webdriver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

import net.lightbody.bmp.BrowserMobProxy;

public class DriverPoolEx
{
	private static final Logger LOGGER = Logger.getLogger(DriverPoolEx.class);
	private final static int MAX_DRIVER_COUNT = Configuration.getInt(Parameter.MAX_DRIVER_COUNT);
	private final static String DEFAULT = "default";
	
	static WebDriver single_driver;
	private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>> drivers = new ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>>();
	
	private static final ConcurrentHashMap<Long, BrowserMobProxy> proxies = new ConcurrentHashMap<Long, BrowserMobProxy>();

	public static void registerDriver(WebDriver driver)
	{
		registerDriver(driver, DEFAULT);
	}
	
	public static void registerDriver(WebDriver driver, String name)
	{
		if (Configuration.getDriverMode() == DriverMode.SUITE_MODE) {
			//init our single driver variable
			single_driver = driver;
		}
		
		Long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = getCurrentDrivers();
		if (currentDrivers.size() == MAX_DRIVER_COUNT) {
			// TODO: after moving driver creation to DriverPoolEx need to add such verification before driver start  
			Assert.fail("Unable to register driver as you reached max number of drivers per thread: " + MAX_DRIVER_COUNT);
		}
		if (currentDrivers.containsKey(name)) {
			Assert.fail("Driver '" + name + "' is already registered for thread: " + threadId);
		}
		
		currentDrivers.put(name,  driver);
		Assert.assertTrue(drivers.get(threadId).containsKey(name), "Driver '" + name + "' was not registered in map for thread: " + threadId);
		LOGGER.debug("##########   REGISTER threadId: " + threadId + "; driver: " + driver);
	}

	public static boolean isDriverRegistered() {
		return isDriverRegistered(DEFAULT);
	}
	
	public static boolean isDriverRegistered(String name) {
		Long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = drivers.get(threadId);
		return currentDrivers.containsKey(name);
	}
	
	public static WebDriver getDriver()
	{
		return getDriver(DEFAULT);
	}
	
	public static WebDriver getDriver(String name)
	{
		WebDriver drv = null;
		DriverMode driverMode = Configuration.getDriverMode();
		Long threadId = Thread.currentThread().getId();
		
		ConcurrentHashMap<String, WebDriver> currentDrivers = getCurrentDrivers();

		if (currentDrivers.containsKey(name)) {
			drv = currentDrivers.get(name);
			LOGGER.debug("##########        GET threadId: " + threadId + "; driver: " + drv);
		} else if (driverMode == DriverMode.SUITE_MODE) {
			LOGGER.debug("########## Unable to find driver by threadId: " + threadId);
			//init our single driver variable
			drv = single_driver;
		} else if ((driverMode == DriverMode.CLASS_MODE || driverMode == DriverMode.METHOD_MODE)
				&& Configuration.getInt(Parameter.THREAD_COUNT) == 1
				&& Configuration.getInt(Parameter.DATA_PROVIDER_THREAD_COUNT) <= 1) {
			Thread[] threads = getGroupThreads(Thread.currentThread().getThreadGroup());
			LOGGER.debug(
					"Try to find driver by ThreadGroup id values! Current ThreadGroup count is: " + threads.length);
			for (int i = 0; i < threads.length; i++) {
				currentDrivers = drivers.get(threads[i].getId());
				if (currentDrivers.containsKey(name)) {
					drv = currentDrivers.get(name);
					LOGGER.debug("##########        GET ThreadGroupId: " + threadId + "; driver: " + drv);
					break;
				}
			}
		}
		return drv;
	}
	

	public static void deregisterDriver()
	{
		deregisterDriver(DEFAULT);
	}
	
	public static void deregisterDriver(String name)
	{
		long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = getCurrentDrivers();
		
		if (currentDrivers.containsKey(name)) {
			WebDriver drv = currentDrivers.get(name);
			LOGGER.debug("########## DEREGISTER threadId: " + threadId + "; driver: " + drv);
			currentDrivers.remove(name);
			
			Assert.assertFalse(drivers.get(threadId).containsKey(name), "Driver '" + name + "' was not deregistered from map for thread: " + threadId);
		} else {
			Assert.fail("Unable to find '" + name + "' driver for deregistration!");
		}
	}
	
	public static void deregisterDrivers()
	{
		ConcurrentHashMap<String, WebDriver> currentDrivers = getCurrentDrivers();
		
		for(Map.Entry<String, WebDriver> entry : currentDrivers.entrySet()) {
			deregisterDriver(entry.getKey());
		}
	}
	
	
	public static void replaceDriver(WebDriver driver) {
		replaceDriver(driver, DEFAULT);
	}
	
	public static void replaceDriver(WebDriver driver, String name) {
		deregisterDriver(name);
		registerDriver(driver, name);
	}

	private static ConcurrentHashMap<String, WebDriver> getCurrentDrivers() {
		Long threadId = Thread.currentThread().getId();
		
		if (drivers.get(threadId) == null) {
			ConcurrentHashMap<String, WebDriver> currentDrivers = new ConcurrentHashMap<String, WebDriver>();
			drivers.put(threadId, currentDrivers);
		}
		return drivers.get(threadId);
	}
	
	private static Thread[] getGroupThreads( final ThreadGroup group ) {
	    if ( group == null )
	        throw new NullPointerException( "Null thread group" );
	    int nAlloc = group.activeCount( );
	    int n = 0;
	    Thread[] threads;
	    do {
	        nAlloc *= 2;
	        threads = new Thread[ nAlloc ];
	        n = group.enumerate( threads );
	    } while ( n == nAlloc );
	    return java.util.Arrays.copyOf( threads, n );
	}

	
	// ------------------------- BOWSERMOB PROXY -----------------------------------------------------------
	public static void registerBrowserMobProxy(BrowserMobProxy proxy)
	{
		proxies.put(Thread.currentThread().getId(),  proxy);
	}
	
	public static BrowserMobProxy getBrowserMobProxy()
	{
		BrowserMobProxy proxy = null;
		long threadId = Thread.currentThread().getId();
		if (proxies.containsKey(threadId)) {
			proxy = proxies.get(threadId);
		} else {
			Assert.fail("There is not registered BrowserMobProxy for thread: " + threadId);
		}
		return proxy;
	}
	
	public static void deregisterBrowserMobProxy()
	{
		long threadId = Thread.currentThread().getId();
	
		if (proxies.containsKey(threadId)) {
			proxies.get(threadId).stop();
			proxies.remove(threadId);
		}
	}

}
