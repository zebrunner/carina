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

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

import net.lightbody.bmp.BrowserMobProxy;

public class DriverPool {
	private static final Logger LOGGER = Logger.getLogger(DriverPool.class);
	private static final int MAX_DRIVER_COUNT = Configuration.getInt(Parameter.MAX_DRIVER_COUNT);

	public static final String DEFAULT = "default";
	protected static WebDriver single_driver;

	private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>> drivers = new ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>>();
	private static final ConcurrentHashMap<Long, BrowserMobProxy> proxies = new ConcurrentHashMap<Long, BrowserMobProxy>();
	
	private static AdbExecutor executor = new AdbExecutor(Configuration.get(Parameter.ADB_HOST), Configuration.get(Parameter.ADB_PORT));
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
			LOGGER.debug("##########        GET threadId: " + threadId + "; driver: " + drv);
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
			LOGGER.warn("Starting new driver as nothing was found in the pool");
			drv = createDriver(name, capabilities, seleniumHost);
		}
		return drv;
	}

	/**
	 * Restart default driver
	 */
	public static void restartDriver() {
		restartDriver(DEFAULT);
	}

	/**
	 * Restart driver by name with default capabilities
	 * 
	 * @param name
	 *            String driver name
	 */
	public static void restartDriver(String name) {
		restartDriver(name, null, null);
	}

	/**
	 * Restart driver by name with custom capabilities
	 * 
	 * @param name
	 *            String driver name
	 * @param capabilities
	 *            DesiredCapabilities
	 * @param seleniumHost
	 *            String
	 */
	public static void restartDriver(String name, DesiredCapabilities capabilities, String seleniumHost) {
		quitDriver(name);
		createDriver(name, capabilities, seleniumHost);
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
		long threadId = Thread.currentThread().getId();
		WebDriver drv = getDriver(name);
		
		stopRecording();
		executor.screenOff();

		try {
			if (drv == null) {
				LOGGER.error("Unable to find valid driver using threadId: " + threadId);
			}

			LOGGER.debug("Driver exiting..." + drv);
			deregisterDriver(name);
			DevicePool.deregisterDevice();
			drv.quit();

			LOGGER.debug("Driver exited..." + drv);
		} catch (Exception e) {
			LOGGER.warn("Error discovered during driver quit: " + e.getMessage());
			LOGGER.debug(
					"======================================================================================================================================");
		} finally {
			// TODO analyze how to forcibly kill session on device
			NDC.pop();
		}
	}

	/**
	 * Quit all drivers registered for current thread/test
	 */
	public static void quitDrivers() {
		
		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		for (Map.Entry<String, WebDriver> entry : currentDrivers.entrySet()) {
			quitDriver(entry.getKey());
		}

		deregisterBrowserMobProxy();
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
	 * @return WebDriver
	 */
	protected static WebDriver createDriver(String name, DesiredCapabilities capabilities, String seleniumHost) {
		boolean init = false;
		int count = 0;
		WebDriver drv = null;
		Throwable init_throwable = null;

		
		// 1 - is default run without retry
		int maxCount = Configuration.getInt(Parameter.INIT_RETRY_COUNT) + 1;
		while (!init & count++ < maxCount) {
			try {
				LOGGER.debug("initDriver start...");

				Device device = DevicePool.registerDevice();
				
				// turn on mobile device display if necessary. action can be
				// done after registering available device with thread
				executor.screenOn();
				if (Configuration.getBoolean(Parameter.MOBILE_APP_REINSTALL)) {
					// explicit reinstall the apk

					String[] apkVersions = executor.getApkVersion(Configuration.get(Parameter.MOBILE_APP));
					if (apkVersions != null) {
						String appPackage = apkVersions[0];

						// TODO: verify if the same version is already installed
						// and skip uninstallation in this case

						executor.uninstallApp(appPackage);
						executor.clearAppData(appPackage);
					}
				}

				if (capabilities == null && seleniumHost == null) {
					drv = DriverFactory.create(name, device);
				} else {
					// TODO: investigate do we need transfer device to factory
					// or not
					drv = DriverFactory.create(name, capabilities, seleniumHost);
				}
				registerDriver(drv, name);

				init = true;
				// push custom device name for log4j default messages
				if (device != null) {
					NDC.push(" [" + device.getName() + "] ");
				}

				LOGGER.debug("initDriver finish...");
				
				
			} catch (Throwable thr) {
				// DevicePool.ignoreDevice();
				DevicePool.deregisterDevice();
				LOGGER.error(String.format("Driver initialization '%s' FAILED! Retry %d of %d time - %s", name, count,
						maxCount, thr.getMessage()));
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
		return currentDrivers.size();
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
			Assert.fail("Unable to find '" + name + "' driver for deregistration!");
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

	// ------------------------- BOWSERMOB PROXY ---------------------
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

	
	private static void startRecording() {
		Integer pid = executor.startRecording(SpecialKeywords.VIDEO_FILE_NAME);
		adbVideoRecorderPid.set(pid);
	}
	
	private static void stopRecording() {
		Integer adb_pid = adbVideoRecorderPid.get();
		if (Configuration.getBoolean(Parameter.VIDEO_RECORDING) && adb_pid != null) {
			executor.stopRecording(adb_pid); //stop recording
			pause(3); //very often video from device is black. trying to wait before pulling the file
			
			String videoDir = ReportContext.getBaseDir().getAbsolutePath();			
			videoDir = ReportContext.getArtifactsFolder().getAbsolutePath();
 
			
			//TODO: refactor video recorder to make it happen for each driver if necessary
			executor.pullFile(SpecialKeywords.VIDEO_FILE_NAME, videoDir + "/video.mp4");
		}	
	}

}
