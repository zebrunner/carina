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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

import net.lightbody.bmp.BrowserMobProxy;

public class DriverPool
{
	private static final Logger LOGGER = Logger.getLogger(DriverPool.class);
	
	static WebDriver single_driver;
	private static final ConcurrentHashMap<Long, WebDriver> threadId2Driver = new ConcurrentHashMap<Long, WebDriver>();
	private static final ConcurrentHashMap<Long, WebDriver> threadId2ExtraDriver = new ConcurrentHashMap<Long, WebDriver>();
	
	
	private static final ConcurrentHashMap<Long, BrowserMobProxy> threadId2Proxy = new ConcurrentHashMap<Long, BrowserMobProxy>();

	public static void registerExtraDriver2Thread(WebDriver driver, Long threadId)
	{
		threadId2ExtraDriver.put(threadId, driver);

		LOGGER.debug("##########   REGISTER threadId: " + threadId + "; extra driver: " + driver);
	}
	
	public static void registerDriver(WebDriver driver)
	{
		registerDriver2Thread(driver, Thread.currentThread().getId());
	}
	
	public static void registerDriver2Thread(WebDriver driver, Long threadId)
	{
		threadId2Driver.put(threadId, driver);

		if (Configuration.getDriverMode() == DriverMode.SUITE_MODE) {
			//init our single driver variable
			single_driver = driver;
		}
		LOGGER.debug("##########   REGISTER threadId: " + threadId + "; driver: " + driver);
	}

	public static WebDriver getDriver()
	{
		return getDriverByThread(Thread.currentThread().getId());
	}
	
	@Deprecated
	public static WebDriver getDriverByThread()
	{
		return getDriverByThread(Thread.currentThread().getId());
	}

	public static WebDriver getDriverByThread(long threadId)
	{
		if (threadId2Driver.size() == 0) {
			// there is no sense to search driver if DriverPool is empty.
			return null;
		}
		WebDriver drv = null;
		DriverMode driverMode = Configuration.getDriverMode();
		if (threadId2Driver.containsKey(threadId)) {
			drv = threadId2Driver.get(threadId);
			LOGGER.debug("##########        GET threadId: " + threadId + "; driver: " + drv);
		}
		else if (driverMode == DriverMode.SUITE_MODE) {
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
				if (threadId2Driver.containsKey(threads[i].getId())) {
					drv = threadId2Driver.get(threads[i].getId());
					LOGGER.debug("##########        GET ThreadGroupId: " + threadId + "; driver: " + drv);
					break;
				}
			}
		}
		return drv;
	}
	
	public static WebDriver getExtraDriverByThread() {
		return getExtraDriverByThread(Thread.currentThread().getId());
	}
	public static WebDriver getExtraDriverByThread(long threadId)
	{
		WebDriver drv = null;
		if (threadId2ExtraDriver.containsKey(threadId)) {
			drv = threadId2ExtraDriver.get(threadId);
			LOGGER.debug("##########        GET threadId: " + threadId + "; extra driver: " + drv);
		}
		return drv;
	}

	public static void registerBrowserMobProxy(BrowserMobProxy proxy)
	{
		threadId2Proxy.put(Thread.currentThread().getId(),  proxy);
	}
	
	public static BrowserMobProxy getBrowserMobProxy()
	{
		BrowserMobProxy proxy = null;
		long threadId = Thread.currentThread().getId();
		if (threadId2Proxy.containsKey(threadId)) {
			proxy = threadId2Proxy.get(threadId);
		} else {
			Assert.fail("There is not registered BrowserMobProxy for thread: " + threadId);
		}
		return proxy;
	}
	
	public static void deregisterBrowserMobProxy()
	{
		long threadId = Thread.currentThread().getId();
	
		if (threadId2Proxy.containsKey(threadId)) {
			threadId2Proxy.get(threadId).stop();
			threadId2Proxy.remove(threadId);
		} else {
			Assert.fail("There is not registered BrowserMobProxy for thread: " + threadId);
		}
	}

	public static void deregisterDriver()
	{
		deregisterDriverByThread(Thread.currentThread().getId());

	}
	
	public static void deregisterDriverByThread(long threadId)
	{
		if (threadId2Driver.containsKey(threadId)) {
			WebDriver drv = threadId2Driver.get(threadId);
			LOGGER.debug("########## DEREGISTER threadId: " + threadId + "; driver: " + drv);
			threadId2Driver.remove(threadId);
		}
	}
	
	public static void deregisterExtraDriverByThread(long threadId)
	{
		if (threadId2ExtraDriver.containsKey(threadId)) {
			WebDriver drv = threadId2ExtraDriver.get(threadId);
			LOGGER.debug("########## DEREGISTER threadId: " + threadId + "; extra driver: " + drv);
			threadId2ExtraDriver.remove(threadId);
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
}
