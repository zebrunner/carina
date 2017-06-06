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

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.testng.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DriverPool {
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
			LOGGER.warn("Starting new driver as nothing was found in the pool");
			drv = createDriver(name, capabilities, seleniumHost, null);
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
		Device device = DevicePool.getDevice();
		
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
		
		stopRecording();
		executor.screenOff();

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
		
		ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		for (Map.Entry<String, WebDriver> entry : currentDrivers.entrySet()) {
			quitDriver(entry.getKey());
		}
		
		stopProxy();
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
		
		
		if (device == null) {
			device = DevicePool.getNullDevice();
		}
		
		// 1 - is default run without retry
		int maxCount = Configuration.getInt(Parameter.INIT_RETRY_COUNT) + 1;
		while (!init & count++ < maxCount) {
			try {
				LOGGER.debug("initDriver start...");
				
				//TODO: move browsermob startup to this location
				startProxy();

				if (device.isNull()) {
					// find and register device from the DevicePool
					device = DevicePool.registerDevice();
					
					// turn on mobile device display if necessary. action can be done after registering available device with thread
					// there is no sense to clean cache and reinstall app if we request dedicated device
					executor.screenOn();
					
					executor.restartAppium(device);
					executor.clearAppData(device);
					
					// verify if valid build is already installed and uninstall only in case of any difference 
					executor.reinstallApp(device, Configuration.get(Parameter.MOBILE_APP));
				}


				if (!device.isNull()) {
					seleniumHost = device.getSeleniumServer();
					drv = DriverFactory.create(name, device);
				} else if (capabilities != null && seleniumHost != null) {
					// TODO: investigate do we need transfer device to factory or not
					drv = DriverFactory.create(name, capabilities, seleniumHost);
				} else {
					drv = DriverFactory.create(name);
				}
				registerDriver(drv, name);

				init = true;
				long threadId = Thread.currentThread().getId();
				// push custom device name and threadId for log4j default messages
				if (!device.isNull()) {
					NDC.push(" [" + device.getName() + "] [" + threadId + "] ");
				} else {
					NDC.push(" [" + threadId + "] ");
				}

				LOGGER.debug("initDriver finish...");
				
				
			} catch (Throwable thr) {
				// DevicePool.ignoreDevice();
				DevicePool.deregisterDevice();
				LOGGER.error(
						String.format("Driver initialization '%s' FAILED for selenium: %s! Retry %d of %d time - %s",
								name, seleniumHost, count, maxCount, thr.getMessage()));
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

	// ------------------------- BOWSERMOB PROXY ---------------------
	// TODO: investigate possibility to return interface to support JettyProxy
	/**
	 * start BrowserMobProxy Server
	 * 
	 * @return BrowserMobProxy
	 * 
	 */
	public static BrowserMobProxy startProxy() {
		if (!Configuration.getBoolean(Parameter.BROWSERMOB_PROXY)) {
			return null;
		}
		// integrate browserMob proxy if required here
		BrowserMobProxy proxy = null;
		long threadId = Thread.currentThread().getId();

		if (proxies.containsKey(threadId)) {
			proxy = proxies.get(threadId);
		} else {
			proxy = new BrowserMobProxyServer();
			proxies.put(threadId, proxy);
		}

		if (!proxy.isStarted()) {
			LOGGER.info("Starting BrowserMob proxy...");
			proxy.start(Configuration.getInt(Parameter.BROWSERMOB_PORT));
			proxy.newHar();
			proxy.setHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.REQUEST_COOKIES,
					CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_CONTENT, CaptureType.RESPONSE_COOKIES,
					CaptureType.RESPONSE_HEADERS);
		} else {
			LOGGER.info("BrowserMob proxy is already started on port " + proxy.getPort());
		}

		//TODO: we shouldn't use global proxy_host/port/protocols config properties as we should support multi threading
		return proxy;
	}

	/**
	 * stop BrowserMobProxy Server
	 * 
	 */
	public static void stopProxy() {
		long threadId = Thread.currentThread().getId();

		LOGGER.debug("stopProxy starting...");
		if (proxies.containsKey(threadId)) {
			BrowserMobProxy proxy = proxies.get(threadId);
			if (proxy != null) {
				LOGGER.debug("Found registered proxy by thread: " + threadId);
				if (proxy.isStarted()) {
					LOGGER.info("Stopping BrowserMob proxy...");
					proxy.stop();
				} else {
					LOGGER.info("Stopping BrowserMob proxy skipped as it is not started.");
				}
				
				//TODO: we shouldn't use global proxy_host/port/protocols config properties as we should support multi threading
			}
			proxies.remove(threadId);
		} else {
			
		}
		LOGGER.debug("stopProxy finished...");
	}

	/**
	 * get registered BrowserMobProxy Server
	 * 
	 * @return BrowserMobProxy
	 * 
	 */
	public static BrowserMobProxy getProxy() {
		BrowserMobProxy proxy = null;
		long threadId = Thread.currentThread().getId();
		if (proxies.containsKey(threadId)) {
			proxy = proxies.get(threadId);
		} else {
			Assert.fail("There is not registered BrowserMobProxy for thread: " + threadId);
		}
		return proxy;
	}

	/**
	 * register custom BrowserMobProxy Server
	 * 
	 * @param proxy
	 *            custom BrowserMobProxy
	 * 
	 */
	public static void registerProxy(BrowserMobProxy proxy) {
		long threadId = Thread.currentThread().getId();
		if (proxies.containsKey(threadId)) {
			BrowserMobProxy currentProxy = proxies.get(threadId);
			LOGGER.warn("Existing proxy is detected and will be overriten");
			if (currentProxy.isStarted()) {
				currentProxy.stop();
			}
			proxies.remove(threadId);
		}
		
		LOGGER.info("Register custom proxy with thread: " + threadId);
		proxies.put(threadId, proxy);
	}
	
	
	private static void startRecording() {
		if (Configuration.getBoolean(Parameter.VIDEO_RECORDING)) { 
			Integer pid = executor.startRecording(SpecialKeywords.VIDEO_FILE_NAME);
			adbVideoRecorderPid.set(pid);
		}
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
