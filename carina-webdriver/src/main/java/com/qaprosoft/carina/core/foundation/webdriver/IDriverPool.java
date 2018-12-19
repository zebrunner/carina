/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.webdriver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.testng.Assert;

import com.qaprosoft.carina.browsermobproxy.ProxyPool;
import com.qaprosoft.carina.core.foundation.exception.DriverPoolException;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.DriverFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

public interface IDriverPool {
	static final Logger LOGGER = Logger.getLogger(IDriverPool.class);
	static final String DEFAULT = "default";

	// unified set of Carina WebDrivers
	static final Set<CarinaDriver> driversPool = new HashSet<CarinaDriver>();

	/**
	 * Get default driver. If no default driver discovered it will be created.
	 * 
	 * @return default WebDriver
	 */
	default public WebDriver getDriver() {
		return getDriver(DEFAULT);
	}

	/**
	 * Get driver by name. If no driver discovered it will be created using
	 * default capabilities.
	 * 
	 * @param name
	 *            String driver name
	 * @return WebDriver
	 */
	default public WebDriver getDriver(String name) {
		return getDriver(name, null, null);
	}

	/**
	 * Get driver by name and DesiredCapabilities.
	 * 
	 * @param name
	 *            String driver name
	 * @param capabilities
	 *            DesiredCapabilities capabilities
	 * @return WebDriver
	 */
	default public WebDriver getDriver(String name, DesiredCapabilities capabilities) {
		return getDriver(name, capabilities, null);
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
	 * @return WebDriver
	 */
	default public WebDriver getDriver(String name, DesiredCapabilities capabilities, String seleniumHost) {
		WebDriver drv = null;

		ConcurrentHashMap<String, CarinaDriver> currentDrivers = getDrivers();
		if (currentDrivers.containsKey(name)) {
			CarinaDriver cdrv = currentDrivers.get(name);
			drv = cdrv.getDriver();
			if (Phase.BEFORE_SUITE.equals(cdrv.getPhase())) {
				LOGGER.info("Before suite registered driver will be returned.");
			} else {
				LOGGER.debug(cdrv.getPhase() + " registered driver will be returned.");
			}
		}

		// Long threadId = Thread.currentThread().getId();
		// ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();

		// TODO [VD] do we really need finding by groupThreads?
		/*
		 * if (currentDrivers.containsKey(name)) { drv =
		 * currentDrivers.get(name); } else if
		 * (Configuration.getInt(Parameter.THREAD_COUNT) == 1 &&
		 * Configuration.getInt(Parameter.DATA_PROVIDER_THREAD_COUNT) <= 1) {
		 * Thread[] threads =
		 * getGroupThreads(Thread.currentThread().getThreadGroup());
		 * logger.debug(
		 * "Try to find driver by ThreadGroup id values! Current ThreadGroup count is: "
		 * + threads.length); for (int i = 0; i < threads.length; i++) {
		 * currentDrivers = drivers.get(threads[i].getId()); if (currentDrivers
		 * != null) { if (currentDrivers.containsKey(name)) { drv =
		 * currentDrivers.get(name);
		 * logger.debug("##########        GET ThreadGroupId: " + threadId +
		 * "; driver: " + drv); break; } } } }
		 */

		if (drv == null) {
			LOGGER.debug("Starting new driver as nothing was found in the pool");
			drv = createDriver(name, capabilities, seleniumHost);
		}

		// [VD] do not wrap EventFiringWebDriver here otherwise DriverListener
		// and all logging will be lost!
		return drv;

	}

	/**
	 * Get driver by WebElement.
	 * 
	 * @param sessionId
	 *            - session id to be used for searching a desired driver
	 * 
	 * @return default WebDriver
	 */
	public static WebDriver getDriver(SessionId sessionId) {
		LOGGER.debug("Detecting WebDriver by sessionId...");

		Iterator<CarinaDriver> iter = driversPool.iterator();
		while (iter.hasNext()) {
			CarinaDriver carinaDriver = iter.next();
			WebDriver drv = carinaDriver.getDriver();
			if (drv instanceof EventFiringWebDriver) {
				EventFiringWebDriver eventFirDriver = (EventFiringWebDriver) drv;
				drv = eventFirDriver.getWrappedDriver();
			}

			SessionId drvSessionId = ((RemoteWebDriver) drv).getSessionId();

			LOGGER.debug("analyzing driver: " + drvSessionId.toString());
			if (sessionId.equals(drvSessionId)) {
				LOGGER.debug("Detected WebDriver by sessionId");
				return drv;
			}
		}

		throw new DriverPoolException("Unable to find driver using sessionId artifacts. Returning default one!");
		// TODO: take a look into the replaceDriver case and how sessionId are
		// regenerated on page objects
	}

	/**
	 * Restart default driver
	 * 
	 * @return WebDriver
	 */
	default public WebDriver restartDriver() {
		return restartDriver(false);
	}

	/**
	 * Restart default driver on the same device
	 * 
	 * @param isSameDevice
	 *            boolean restart driver on the same device or not
	 * @return WebDriver
	 */
	default public WebDriver restartDriver(boolean isSameDevice) {
		WebDriver drv = getDriver(DEFAULT);
		Device device = DevicePool.getNullDevice();
		DesiredCapabilities caps = new DesiredCapabilities();

		if (isSameDevice) {
			device = DevicePool.getDevice();
			LOGGER.debug("Added udid: " + device.getUdid() + " to capabilities for restartDriver on the same device.");
			caps.setCapability("udid", device.getUdid());
		}

		Iterator<CarinaDriver> iter = driversPool.iterator();
		while (iter.hasNext()) {
			CarinaDriver carinaDriver = iter.next();
			if (carinaDriver.getDriver().equals(drv)) {
				quitDriver(carinaDriver.getDriver());
				iter.remove();
			}
		}

		return createDriver(DEFAULT, caps, null);
	}

	/**
	 * Quit default driver
	 */
	default public void quitDriver() {
		quitDriver(DEFAULT);
	}

	// TODO: Fix after migrating to java9
	// [VD] quitDriver and quitDrivers has code duplicates as inside interface
	// we can't create private methods!

	/**
	 * Quit driver by name
	 * 
	 * @param name
	 *            String driver name
	 */
	default public void quitDriver(String name) {
		Iterator<CarinaDriver> iter = driversPool.iterator();

		WebDriver drv = null;
		Long threadId = Thread.currentThread().getId();

		while (iter.hasNext()) {
			CarinaDriver carinaDriver = iter.next();

			if (Phase.BEFORE_SUITE.equals(carinaDriver.getPhase()) && name.equals(carinaDriver.getName())) {
				drv = carinaDriver.getDriver();
			} else if (threadId.equals(carinaDriver.getThreadId()) && name.equals(carinaDriver.getName())) {
				drv = carinaDriver.getDriver();
			}
		}

		if (drv == null) {
			throw new RuntimeException("Unable to find driver '" + name + "'!");
		}

		quitDriver(drv);
		iter.remove();
	}

	/**
	 * Quit current drivers by phase
	 */
	default public void quitDrivers(Phase phase) {
		Iterator<CarinaDriver> iter = driversPool.iterator();

		Long threadId = Thread.currentThread().getId();
		while (iter.hasNext()) {
			CarinaDriver carinaDriver = iter.next();

			if (phase.equals(carinaDriver.getPhase()) && threadId.equals(carinaDriver.getThreadId())) {
				quitDriver(carinaDriver.getDriver());
				iter.remove();
			}
		}
	}

	default void quitDriver(WebDriver drv) {
		try {
			DevicePool.deregisterDevice();
			ProxyPool.stopProxy();
			drv.quit();
		} catch (WebDriverException e) {
			LOGGER.debug("Error message detected during driver verification: " + e.getMessage(), e);
			// do nothing
		} catch (Exception e) {
			LOGGER.debug("Error discovered during driver quit: " + e.getMessage(), e);

			// TODO: it seems like BROWSER_TIMEOUT or NODE_FORWARDING should be
			// handled here as well
			if (!e.getMessage().contains("Session ID is null.")) {
				throw e;
			}

		} finally {
			// TODO analyze how to forcibly kill session on device
			MDC.remove("device");
		}
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
	default WebDriver createDriver(String name, DesiredCapabilities capabilities, String seleniumHost) {
		boolean init = false;
		int count = 0;
		WebDriver drv = null;
		Throwable init_throwable = null;
		Device device = DevicePool.getNullDevice();

		// 1 - is default run without retry
		int maxCount = Configuration.getInt(Parameter.INIT_RETRY_COUNT) + 1;
		while (!init && count++ < maxCount) {
			try {
				LOGGER.debug("initDriver start...");

				drv = DriverFactory.create(name, capabilities, seleniumHost);

				// [VD] pay attention that below piece of code is copied into
				// the driverPoolBasetest as registerDriver method!
				// ---------- start driver registration ----
				Long threadId = Thread.currentThread().getId();
				ConcurrentHashMap<String, CarinaDriver> currentDrivers = getDrivers();

				int maxDriverCount = Configuration.getInt(Parameter.MAX_DRIVER_COUNT);

				if (currentDrivers.size() == maxDriverCount) {
					// TODO: after moving driver creation to DriverPoolEx need
					// to add
					// such verification before driver start
					Assert.fail("Unable to register driver as you reached max number of drivers per thread: "
							+ maxDriverCount);
				}
				if (currentDrivers.containsKey(name)) {
					Assert.fail("Driver '" + name + "' is already registered for thread: " + threadId);
				}

				// new 6.0 approach to manipulate drivers via regular Set
				CarinaDriver carinaDriver = new CarinaDriver(name, drv, TestPhase.getActivePhase(), threadId);
				driversPool.add(carinaDriver);
				// ---------- finish driver registration ----

				init = true;

				if (device.isNull()) {
					// During driver creation we choose device and assign it to
					// the test thread
					device = DevicePool.getDevice();
				}
				// push custom device name for log4j default messages
				if (!device.isNull()) {
					MDC.put("device", "[" + device.getName() + "] ");
				}

				// moved proxy start logic here since device will be initialized
				// here only
				if (Configuration.getBoolean(Parameter.BROWSERMOB_PROXY)) {
					int proxyPort = Configuration.getInt(Parameter.BROWSERMOB_PORT);
					if (!device.isNull()) {
						try {
							proxyPort = Integer.parseInt(device.getProxyPort());
						} catch (NumberFormatException e) {
							// use default from _config.properties. Use-case for
							// iOS devices which doesn't have proxy_port as part
							// of capabilities
							proxyPort = Configuration.getInt(Parameter.BROWSERMOB_PORT);
						}
						ProxyPool.startProxy(proxyPort);
					}
				}

				LOGGER.debug("initDriver finish...");

			} catch (Exception e) {
				// DevicePool.ignoreDevice();
				DevicePool.deregisterDevice();
				LOGGER.error(String.format("Driver initialization '%s' FAILED! Retry %d of %d time - %s", name, count,
						maxCount, e.getMessage()), e);
				init_throwable = e;
				CommonUtils.pause(Configuration.getInt(Parameter.INIT_RETRY_INTERVAL));
			}
		}

		if (!init) {
			// TODO: think about this runtime exception
			throw new RuntimeException(init_throwable);
		}

		return drv;
	}

	/**
	 * Verify if driver is registered in the DriverPool
	 * 
	 * @param name
	 *            String driver name
	 *
	 * @return boolean
	 */
	default boolean isDriverRegistered(String name) {
		return getDrivers().containsKey(name);
	}

	// TODO: think about hiding getDriversCount and removing size
	/**
	 * Return number of registered driver per thread
	 * 
	 * @return int
	 */
	default public int getDriversCount() {
		Long threadId = Thread.currentThread().getId();
		int size = getDrivers().size();
		LOGGER.debug("Number of registered drivers for thread '" + threadId + "' is " + size);
		return size;
	}

	/**
	 * @deprecated use {@link #getDriversCount()} instead. Return number of
	 *             registered driver per thread
	 * 
	 * @return int
	 */
	@Deprecated
	default public int size() {
		Long threadId = Thread.currentThread().getId();
		int size = getDrivers().size();
		LOGGER.debug("Number of registered drivers for thread '" + threadId + "' is " + size);
		return size;
	}

	/**
	 * Return all drivers registered in the DriverPool for this thread including
	 * on Before Suite/Class/Method stages
	 * 
	 * @return ConcurrentHashMap of driver names and Carina WebDrivers
	 * 
	 */
	default ConcurrentHashMap<String, CarinaDriver> getDrivers() {
		Long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, CarinaDriver> currentDrivers = new ConcurrentHashMap<String, CarinaDriver>();
		Iterator<CarinaDriver> iter = driversPool.iterator();
		while (iter.hasNext()) {
			CarinaDriver carinaDriver = iter.next();
			if (Phase.BEFORE_SUITE.equals(carinaDriver.getPhase())) {
				LOGGER.debug("Add suite_mode drivers into the getDrivers response: " + carinaDriver.getName());
				currentDrivers.put(carinaDriver.getName(), carinaDriver);
			} else if (threadId.equals(carinaDriver.getThreadId())) {
				LOGGER.debug("Add driver into the getDrivers response: " + carinaDriver.getName() + " by threadId: "
						+ threadId);
				currentDrivers.put(carinaDriver.getName(), carinaDriver);
			}
		}
		return currentDrivers;
	}

	@Deprecated
	public static WebDriver getDefaultDriver() {
		WebDriver drv = null;
		ConcurrentHashMap<String, WebDriver> currentDrivers = getStaticDrivers();

		if (currentDrivers.containsKey(DEFAULT)) {
			drv = currentDrivers.get(DEFAULT);
		}

		if (drv == null) {
			throw new DriverPoolException("no default driver detected!");
		}

		// [VD] do not wrap EventFiringWebDriver here otherwise DriverListener
		// and all logging will be lost!
		return drv;
	}

	@Deprecated
	public static ConcurrentHashMap<String, WebDriver> getStaticDrivers() {
		Long threadId = Thread.currentThread().getId();
		ConcurrentHashMap<String, WebDriver> currentDrivers = new ConcurrentHashMap<String, WebDriver>();
		// look inside driversPool and return all before_suite drivers and
		// drivers mounted to the current thread_id
		Iterator<CarinaDriver> iter = driversPool.iterator();

		while (iter.hasNext()) {
			CarinaDriver carinaDriver = iter.next();

			if (Phase.BEFORE_SUITE.equals(carinaDriver.getPhase())) {
				currentDrivers.put(carinaDriver.getName(), carinaDriver.getDriver());
			} else if (threadId.equals(carinaDriver.getThreadId())) {
				currentDrivers.put(carinaDriver.getName(), carinaDriver.getDriver());
			}
		}

		return currentDrivers;
	}

}
