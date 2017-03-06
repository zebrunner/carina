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
import org.apache.log4j.NDC;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

import net.lightbody.bmp.BrowserMobProxy;

public class DriverPoolEx {
	private static final Logger LOGGER = Logger.getLogger(DriverPoolEx.class);
	private static final int MAX_DRIVER_COUNT = Configuration.getInt(Parameter.MAX_DRIVER_COUNT);
	
	public static final String DEFAULT = "default";
	protected static WebDriver single_driver;
	
	private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>> drivers = new ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>>();

	private static final ConcurrentHashMap<Long, BrowserMobProxy> proxies = new ConcurrentHashMap<Long, BrowserMobProxy>();

	
	public static WebDriver createDriver() {
		return createDriver(DEFAULT);
	}
	
	public static WebDriver createDriver(String name) {
    	boolean init = false;
    	int count = 0;
    	WebDriver drv = null;
    	Throwable init_throwable = null;
    	
    	int maxCount = Configuration.getInt(Parameter.INIT_RETRY_COUNT) + 1; //1 - is default run without retry
    	while (!init & count++ < maxCount) {
    		try {
    			LOGGER.debug("initDriver start...");
    			
    			Device device = DevicePool.registerDevice2Thread();
    			
    			drv = DriverFactory.create(name, device);
    			registerDriver(drv, name);
    			
    			init = true;
    			// push custom device name for log4j default messages
    			if (device != null) {
    				NDC.push(" [" + device.getName() + "] ");
    			}
    			
    			LOGGER.debug("initDriver finish...");
    		}
    		catch (Throwable thr) {
    			//DevicePool.ignoreDevice();
    			DevicePool.deregisterDeviceFromThread();
    			LOGGER.error(String.format("Driver initialization '%s' FAILED! Retry %d of %d time - %s", name, count, maxCount, thr.getMessage()));
    			init_throwable = thr;
    			pause(Configuration.getInt(Parameter.INIT_RETRY_INTERVAL));
    		}
    	}
    	
    	if (!init) {
    		throw new RuntimeException(init_throwable);
    	}

    	return drv;
	}
	
	public static WebDriver createDriver(String name, DesiredCapabilities capabilities, String selenium_host) {
		WebDriver extraDriver = null;
		try {
			if (capabilities == null && selenium_host == null) {
				Device device = DevicePool.registerDevice2Thread();
				extraDriver = DriverFactory.create(name, device);	
			}
			else {
				extraDriver = DriverFactory.create(name, capabilities, selenium_host);
			}
	    	
	    	if (extraDriver == null ) {
	    		Assert.fail("Unable to initialize extra driver: " + name + "!");
	    	}
		}
		catch (Throwable thr) {
			thr.printStackTrace();
			LOGGER.debug(String.format("Extra Driver initialization '%s' FAILED! Reason: %s", name, thr.getMessage()), thr);
			DevicePool.deregisterDeviceFromThread();
			LOGGER.error(String.format("Extra Driver initialization '%s' FAILED! Reason: %s", name, thr.getMessage()));
			throw new RuntimeException (thr);
		}    	
		
		registerDriver(extraDriver, name);
		return extraDriver;		
	}


	public static void quitDrivers() {
		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		for (Map.Entry<String, WebDriver> entry : currentDrivers.entrySet()) {
			quitDriver(entry.getKey());
		}
		
		deregisterBrowserMobProxy();
	}
	
	public static void quitDriver() {
		quitDriver(DEFAULT);
	}
	
	public static void quitDriver(String name) {
		long threadId = Thread.currentThread().getId();
		WebDriver drv = getDriver(name);
		
		try {
			if (drv == null) {
				LOGGER.error("Unable to find valid driver using threadId: " + threadId);
			}

			LOGGER.debug("Driver exiting..." + drv);
	    	deregisterDriver(name);
	    	DevicePool.deregisterDeviceFromThread();
			drv.quit();
			
	    	LOGGER.debug("Driver exited..." + drv);
		} catch (Exception e) {
    		LOGGER.warn("Error discovered during driver quit: " + e.getMessage());
    		LOGGER.debug("======================================================================================================================================");
		} finally {
    		//TODO analyze how to forcibly kill session on device
			NDC.pop();
		}
    }

	
	protected static void registerDriver(WebDriver driver) {
		registerDriver(driver, DEFAULT);
	}

	protected static void registerDriver(WebDriver driver, String name) {
		if (Configuration.getDriverMode() == DriverMode.SUITE_MODE && DEFAULT.equals(name)) {
			//replace single_driver only for default one!
			// init our single driver variable
			single_driver = driver;
		}

		Long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();
		if (currentDrivers.size() == MAX_DRIVER_COUNT) {
			// TODO: after moving driver creation to DriverPoolEx need to add
			// such verification before driver start
			Assert.fail(
					"Unable to register driver as you reached max number of drivers per thread: " + MAX_DRIVER_COUNT);
		}
		if (currentDrivers.containsKey(name)) {
			Assert.fail("Driver '" + name + "' is already registered for thread: " + threadId);
		}

		currentDrivers.put(name, driver);
		Assert.assertTrue(drivers.get(threadId).containsKey(name),
				"Driver '" + name + "' was not registered in map for thread: " + threadId);
		LOGGER.debug("##########   REGISTER threadId: " + threadId + "; driver: " + driver);
	}

	protected static boolean isDriverRegistered() {
		return isDriverRegistered(DEFAULT);
	}

	protected static boolean isDriverRegistered(String name) {
		Long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = drivers.get(threadId);
		
		if (currentDrivers == null) {
			return false;
		}
		return currentDrivers.containsKey(name);
	}
	
	protected static int size() {
		Long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = drivers.get(threadId);
		return currentDrivers.size();
	}

	public static WebDriver getSingleDriver() {
		return single_driver;
	}

	
	public static WebDriver getDriver() {
		return getDriver(DEFAULT);
	}

	public static WebDriver getDriver(String name) {
		WebDriver drv = null;
		DriverMode driverMode = Configuration.getDriverMode();
		Long threadId = Thread.currentThread().getId();

		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		if (currentDrivers.containsKey(name)) {
			drv = currentDrivers.get(name);
			LOGGER.debug("##########        GET threadId: " + threadId + "; driver: " + drv);
		} else if (driverMode == DriverMode.SUITE_MODE && DEFAULT.equals(name)) {
			LOGGER.debug("########## Unable to find driver by threadId: " + threadId);
			// init our single driver variable
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

	protected static void deregisterDriver() {
		deregisterDriver(DEFAULT);
	}

	protected static void deregisterDriver(String name) {
		long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		if (currentDrivers.containsKey(name)) {
			WebDriver drv = currentDrivers.get(name);
			LOGGER.debug("########## DEREGISTER threadId: " + threadId + "; driver: " + drv);
			currentDrivers.remove(name);
			
			if (Configuration.getDriverMode() == DriverMode.SUITE_MODE && DEFAULT.equals(name)) {
				single_driver = null;
			}

			Assert.assertFalse(drivers.get(threadId).containsKey(name),
					"Driver '" + name + "' was not deregistered from map for thread: " + threadId);
		} else {
			Assert.fail("Unable to find '" + name + "' driver for deregistration!");
		}
	}

	protected static void deregisterDrivers() {
		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		for (Map.Entry<String, WebDriver> entry : currentDrivers.entrySet()) {
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
	
	public static ConcurrentHashMap<String, WebDriver> getDrivers() {
		Long threadId = Thread.currentThread().getId();

		if (drivers.get(threadId) == null) {
			ConcurrentHashMap<String, WebDriver> currentDrivers = new ConcurrentHashMap<String, WebDriver>();
			drivers.put(threadId, currentDrivers);
		}
		return drivers.get(threadId);
	}

	private static Thread[] getGroupThreads(final ThreadGroup group) {
		if (group == null)
			throw new NullPointerException("Null thread group");
		int nAlloc = group.activeCount();
		int n = 0;
		Thread[] threads;
		do {
			nAlloc *= 2;
			threads = new Thread[nAlloc];
			n = group.enumerate(threads);
		} while (n == nAlloc);
		return java.util.Arrays.copyOf(threads, n);
	}

	/**
	 * Pause for specified timeout.
	 * 
	 * @param timeout
	 *            in seconds.
	 */

	private static void pause(long timeout) {
		try {
			Thread.sleep(timeout * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// ------------------------- BOWSERMOB PROXY ---------------------------------
	public static void registerBrowserMobProxy(BrowserMobProxy proxy) {
		proxies.put(Thread.currentThread().getId(), proxy);
	}

	public static BrowserMobProxy getBrowserMobProxy() {
		BrowserMobProxy proxy = null;
		long threadId = Thread.currentThread().getId();
		if (proxies.containsKey(threadId)) {
			proxy = proxies.get(threadId);
		} else {
			Assert.fail("There is not registered BrowserMobProxy for thread: " + threadId);
		}
		return proxy;
	}

	public static void deregisterBrowserMobProxy() {
		long threadId = Thread.currentThread().getId();

		if (proxies.containsKey(threadId)) {
			proxies.get(threadId).stop();
			proxies.remove(threadId);
		}
	}

}
