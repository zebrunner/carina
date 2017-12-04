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
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.testng.Assert;

import com.qaprosoft.carina.browsermobproxy.ProxyPool;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.report.Artifacts;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

public final class DriverPool {
	private static final Logger LOGGER = Logger.getLogger(DriverPool.class);
	private static final int MAX_DRIVER_COUNT = Configuration.getInt(Parameter.MAX_DRIVER_COUNT);

	public static final String DEFAULT = "default";
	protected static WebDriver single_driver;

	private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>> drivers = new ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>>();

	protected static ThreadLocal<Integer> adbVideoRecorderPid = new ThreadLocal<Integer>();
	
	/**
	 * Get global suite driver. For driver_mode=suite_mode only.
	 * 
	 * @return Suite mode WebDriver
	 */
	public static WebDriver getSingleDriver() {
		return single_driver;
	}

	/**
	 * Get default driver. If no default driver discovered it will be created.
	 * 
	 * @return default WebDriver
	 */
	public static WebDriver getDriver() {
		return getDriver(DEFAULT);
	}
	
	/**
	 * Get first registered driver from Pool.
	 * 
	 * @return default WebDriver
	 */
	public static WebDriver getExistingDriver() {
		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();
		if (currentDrivers.size() == 0) {
			throw new RuntimeException("Unable to find exiting river in DriverPool!");
		}
		
		if (currentDrivers.size() > 0) {
			return currentDrivers.get(0);
		}
		
		return getDriver(DEFAULT);
	}

	/**
	 * Get driver by name. If no driver discovered it will be created using
	 * default capabilities.
	 * 
	 * @param name
	 *            String driver name
	 * @return WebDriver by name
	 */
	public static WebDriver getDriver(String name) {
		return getDriver(name, null, null);
	}

	/**
	 * Get driver by name. If no driver discovered it will be created using
	 * custom capabilities and selenium server.
	 * 
	 * @param name
	 *            String driver name
	 * @param capabilities
	 *            DesiredCapabilities
	 * @param seleniumHost
	 *            String
	 * @return WebDriver by name
	 */
	public static WebDriver getDriver(String name, DesiredCapabilities capabilities, String seleniumHost) {
		WebDriver drv = null;
		DriverMode driverMode = Configuration.getDriverMode();
		Long threadId = Thread.currentThread().getId();

		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		if (currentDrivers.containsKey(name)) {
			drv = currentDrivers.get(name);
		} else if (driverMode == DriverMode.SUITE_MODE && DEFAULT.equals(name)) {
			LOGGER.debug("########## Unable to find suite driver by threadId: " + threadId);
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
				if (currentDrivers != null) {
					if (currentDrivers.containsKey(name)) {
						drv = currentDrivers.get(name);
						LOGGER.debug("##########        GET ThreadGroupId: " + threadId + "; driver: " + drv);
						break;
					}
				}
			}
		}

		if (drv == null) {
			LOGGER.debug("Starting new driver as nothing was found in the pool");
			drv = createDriver(name, capabilities, seleniumHost, DevicePool.getNullDevice());
		}
		return drv;
	}

	/**
	 * Restart default driver
	 * @return WebDriver
	 */
	public static WebDriver restartDriver() {
		return restartDriver(false);
	}

	/**
	 * Restart default driver on the same device
	 * 
	 * @param isSameDevice
	 *            boolean restart driver on the same device or not
	 * @return WebDriver
	 */
	public static WebDriver restartDriver(boolean isSameDevice) {
		WebDriver drv = getDriver(DEFAULT);
		Device device = DevicePool.getNullDevice();
		if (isSameDevice) {
			device = DevicePool.getDevice();
		}
		
		try {
			LOGGER.debug("Driver restarting..." + drv);
			deregisterDriver(DEFAULT);
			if (!isSameDevice) {
				DevicePool.deregisterDevice();
			}

			drv.quit();
			LOGGER.debug("Driver exited during restart..." + drv);
		} catch (UnreachableBrowserException e) {
			//do not remove this handler as AppiumDriver still has it
			LOGGER.debug("unable to quit as sesion was not found" + drv);
		} catch (Exception e) {
			LOGGER.warn("Error discovered during driver restart: ", e);
		} finally {
			NDC.pop();
		}

		//start default driver. Device can be nullDevice...
		return createDriver(DEFAULT, null, null, device);

	}

	/**
	 * Quit default driver
	 */
	public static void quitDriver() {
		quitDriver(DEFAULT);
	}

	/**
	 * Quit driver by name
	 * 
	 * @param name
	 *            String driver name
	 */
	public static void quitDriver(String name) {
		WebDriver drv = getDriver(name);

		//TODO: try to understand valid place for recorded video file to support method/class and suite mode
		// 1. create for each driver their own video file. 
		// 2. save it using unique driver/test/thread name - maybe time in ms
		// 3. register link onto the video as test artifact
		Device device = DevicePool.getDevice();
		if (!device.isNull()) {
			device.screenOff();
		}

		try {
			LOGGER.debug("Driver exiting..." + drv);
			deregisterDriver(name);
			DevicePool.deregisterDevice();
			drv.quit();
			LOGGER.debug("Driver exited..." + drv);
		} catch (UnreachableBrowserException e) {
			//do not remove this handler as AppiumDriver still has it
			LOGGER.debug("unable to quit as sesion was not found" + drv);
		} catch (Exception e) {
			LOGGER.warn("Error discovered during driver quit: " + e.getMessage());
		} finally {
			// TODO analyze how to forcibly kill session on device
			NDC.pop();
		}
	}

	/**
	 * Quit all drivers registered for current thread/test
	 */
	public static void quitDrivers() {
		
		stopRecording();
		
		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		for (Map.Entry<String, WebDriver> entry : currentDrivers.entrySet()) {
			quitDriver(entry.getKey());
		}
		
		// stopProxy();
	}

	/**
	 * Create driver with custom capabilities
	 * 
	 * @param name
	 *            String driver name
	 * @param capabilities
	 *            DesiredCapabilities
	 * @param seleniumHost
	 *            String
	 * @param device
	 *            Device where we want to start driver
	 * @return WebDriver
	 */
	private static WebDriver createDriver(String name, DesiredCapabilities capabilities, String seleniumHost, Device device) {
		boolean init = false;
		int count = 0;
		WebDriver drv = null;
		Throwable init_throwable = null;
		
		
		// 1 - is default run without retry
		int maxCount = Configuration.getInt(Parameter.INIT_RETRY_COUNT) + 1;
		while (!init && count++ < maxCount) {
			try {
				LOGGER.debug("initDriver start...");
				
				//TODO: move browsermob startup to this location
				ProxyPool.startProxy();

				drv = DriverFactory.create(name, device, capabilities, seleniumHost);
				registerDriver(drv, name);

				init = true;
				// push custom device name  for log4j default messages
				if (!device.isNull()) {
					NDC.push(" [" + device.getName() + "] ");
				}

				LOGGER.debug("initDriver finish...");
				
				
			} catch (Throwable thr) {
				// DevicePool.ignoreDevice();
				DevicePool.deregisterDevice();
				LOGGER.error(
						String.format("Driver initialization '%s' FAILED for selenium: %s! Retry %d of %d time - %s",
								name, seleniumHost, count, maxCount, thr.getMessage()), thr);
				init_throwable = thr;
				pause(Configuration.getInt(Parameter.INIT_RETRY_INTERVAL));
			}
		}

		if (!init) {
			throw new RuntimeException(init_throwable);
		}
		
		startRecording();
		return drv;
	}
	
	private static void startRecording() {
		if (!Configuration.getBoolean(Parameter.VIDEO_RECORDING)) {
			return;
		}
		
		Integer pid = adbVideoRecorderPid.get();
		if (pid == null) {
			// video recording is not started yet for current thread
			pid = DevicePool.getDevice().startRecording(SpecialKeywords.VIDEO_FILE_NAME);
			adbVideoRecorderPid.set(pid);
		} else {
			LOGGER.warn("Video recording is already started for current thread.");
		}

	}
	
	private static void stopRecording() {
		if (!Configuration.getBoolean(Parameter.VIDEO_RECORDING)) {
			return;
		}
		
		
		Device device = DevicePool.getDevice();
		if (!device.isNull()) {
			device.stopRecording(adbVideoRecorderPid.get());
			pause(3); // very often video from device is black. waiting
						// before pulling the file

			String videoDir = ReportContext.getArtifactsFolder().getAbsolutePath();
			String uniqueFileName = "VIDEO-" + System.currentTimeMillis() + ".mp4";
			device.pullFile(SpecialKeywords.VIDEO_FILE_NAME, videoDir + "/" + uniqueFileName);

			String artifactsLink = ReportContext.getTestArtifactsLink();

			// TODO: use expiration date as current_date + 30
			Artifacts.add("VIDEO", artifactsLink + "/" + uniqueFileName);
		}

	}

	/**
	 * Register driver in the DriverPool
	 * 
	 * @param driver
	 *            WebDriver
	 * 
	 * @param name
	 *            String driver name
	 * 
	 */
	protected static void registerDriver(WebDriver driver, String name) {
		if (Configuration.getDriverMode() == DriverMode.SUITE_MODE && DEFAULT.equals(name)) {
			// replace single_driver only for default one!
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

	/**
	 * Verify if driver is registered in the DriverPool
	 * 
	 * @param name
	 *            String driver name
	 *
	 * @return boolean
	 */
	protected static boolean isDriverRegistered(String name) {
		Long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = drivers.get(threadId);

		if (currentDrivers == null) {
			return false;
		}
		return currentDrivers.containsKey(name);
	}

	/**
	 * Return number of registered driver per thread
	 * 
	 * @return int
	 */
	protected static int size() {
		Long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = drivers.get(threadId);
		int size = currentDrivers.size();
		LOGGER.debug("Number of registered drivers for thread '" + threadId + "' is " + size);
		return size;
	}

	/**
	 * Deregister driver by name from the DriverPool
	 * 
	 * @param name
	 *            String driver name
	 * 
	 */
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
			LOGGER.error("Unable to find '" + name + "' driver for deregistration in thread: " + threadId);
		}
	}

	/**
	 * Deregister all drivers from the DriverPool for current thread
	 * 
	 */
	protected static void deregisterDrivers() {
		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		for (Map.Entry<String, WebDriver> entry : currentDrivers.entrySet()) {
			deregisterDriver(entry.getKey());
		}
	}

	/**
	 * Replace default driver in the DriverPool
	 * 
	 * @param driver
	 *            WebDriver
	 * 
	 */
	public static void replaceDriver(WebDriver driver) {
		replaceDriver(driver, DEFAULT);
	}

	/**
	 * Replace named driver in the DriverPool
	 * 
	 * @param driver
	 *            WebDriver
	 * 
	 * @param name
	 *            String driver name
	 * 
	 */
	public static void replaceDriver(WebDriver driver, String name) {
		deregisterDriver(name);
		registerDriver(driver, name);
	}

	/**
	 * Return all drivers registered in the DriverPool for this thread
	 * 
	 * @return ConcurrentHashMap of driver names and WebDrivers
	 * 
	 */
	public static ConcurrentHashMap<String, WebDriver> getDrivers() {
		Long threadId = Thread.currentThread().getId();

		if (drivers.get(threadId) == null) {
			ConcurrentHashMap<String, WebDriver> currentDrivers = new ConcurrentHashMap<String, WebDriver>();
			drivers.put(threadId, currentDrivers);
		}
		return drivers.get(threadId);
	}

	/**
	 * Return all threads associated with current multithreading test
	 * 
	 * @return Thread[]
	 * 
	 */
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

}
