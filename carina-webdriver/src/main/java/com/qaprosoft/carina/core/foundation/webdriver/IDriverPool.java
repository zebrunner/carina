/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.exception.DriverPoolException;
import com.qaprosoft.carina.core.foundation.performance.Timer;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.DriverFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public interface IDriverPool {
    static final Logger LOGGER = Logger.getLogger(IDriverPool.class);
    static final String DEFAULT = "default";

    // unified set of Carina WebDrivers
    static final ConcurrentHashMap<CarinaDriver, Integer> driversMap = new ConcurrentHashMap<>();
    @SuppressWarnings("static-access")
    static final Set<CarinaDriver> driversPool = driversMap.newKeySet();
    // static final Set<CarinaDriver> driversPool = new HashSet<CarinaDriver>();
    
    //TODO: [VD] make device related param private after migrating to java 9+
    static final ThreadLocal<Device> currentDevice = new ThreadLocal<Device>();
    static final Device nullDevice = new Device();

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
        for (CarinaDriver carinaDriver : driversPool) {
            WebDriver drv = carinaDriver.getDriver();
            if (drv instanceof EventFiringWebDriver) {
                EventFiringWebDriver eventFirDriver = (EventFiringWebDriver) drv;
                drv = eventFirDriver.getWrappedDriver();
            }

            SessionId drvSessionId = ((RemoteWebDriver) drv).getSessionId();

            if (drvSessionId != null) {
                if (sessionId.equals(drvSessionId)) {
                    return drv;
                }
            }
        }
        throw new DriverPoolException("Unable to find driver using sessionId artifacts. Returning default one!");
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
        Device device = nullDevice;
        DesiredCapabilities caps = new DesiredCapabilities();
        
        boolean keepProxy = false;

        if (isSameDevice) {
            keepProxy = true;
            device = getDefaultDevice();
            LOGGER.debug("Added udid: " + device.getUdid() + " to capabilities for restartDriver on the same device.");
            caps.setCapability("udid", device.getUdid());
        }

        LOGGER.debug("before restartDriver: " + driversPool);
        for (CarinaDriver carinaDriver : driversPool) {
            if (carinaDriver.getDriver().equals(drv)) {
                quitDriver(carinaDriver, keepProxy);
                // [VD] don't remove break or refactor moving removal out of "for" cycle
                driversPool.remove(carinaDriver);
                break;
            }
        }
        LOGGER.debug("after restartDriver: " + driversPool);

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

        WebDriver drv = null;
        CarinaDriver carinaDrv = null;
        Long threadId = Thread.currentThread().getId();

        LOGGER.debug("before quitDriver: " + driversPool);
        for (CarinaDriver carinaDriver : driversPool) {
            if ((Phase.BEFORE_SUITE.equals(carinaDriver.getPhase()) && name.equals(carinaDriver.getName()))
                    || (threadId.equals(carinaDriver.getThreadId()) && name.equals(carinaDriver.getName()))) {
                drv = carinaDriver.getDriver();
                carinaDrv = carinaDriver;
                break;
            }
        }

        if (drv == null || carinaDrv == null) {
            throw new RuntimeException("Unable to find driver '" + name + "'!");
        }

        quitDriver(carinaDrv, false);
        driversPool.remove(carinaDrv);

        LOGGER.debug("after quitDriver: " + driversPool);

    }

    /**
     * Quit current drivers by phase(s). "Current" means assigned to the current test/thread.
     * 
     * @param phase
     *            Comma separated driver phases to quit
     */
    default public void quitDrivers(Phase...phase) {
        List<Phase> phases = Arrays.asList(phase);

        Set<CarinaDriver> drivers4Remove = new HashSet<CarinaDriver>();

        Long threadId = Thread.currentThread().getId();
        for (CarinaDriver carinaDriver : driversPool) {
            if ((phases.contains(carinaDriver.getPhase()) && threadId.equals(carinaDriver.getThreadId()))
                    || phases.contains(Phase.ALL)) {
                quitDriver(carinaDriver, false);
                drivers4Remove.add(carinaDriver);
            }
        }
        driversPool.removeAll(drivers4Remove);

        // don't use modern removeIf as it uses iterator!
        // driversPool.removeIf(carinaDriver -> phase.equals(carinaDriver.getPhase()) && threadId.equals(carinaDriver.getThreadId()));
    }

    //TODO: [VD] make it as private after migrating to java 9+
    default void quitDriver(CarinaDriver carinaDriver, boolean keepProxyDuring) {
        try {
            carinaDriver.getDevice().disconnectRemote();
            if (!keepProxyDuring) {
                ProxyPool.stopProxy();
            }
            LOGGER.debug("start driver quit: " + carinaDriver.getName());
            carinaDriver.getDriver().quit();
            LOGGER.debug("finished driver quit: " + carinaDriver.getName());
            // stop timer to be able to track mobile app session time. It should be started on createDriver!
            Timer.stop(carinaDriver.getDevice().getMetricName(), carinaDriver.getName() + carinaDriver.getDevice().getName());
        } catch (WebDriverException e) {
            LOGGER.debug("Error message detected during driver quit: " + e.getMessage(), e);
            // do nothing
        } catch (Exception e) {
            LOGGER.error("Error discovered during driver quit: " + e.getMessage(), e);
        } finally {
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
        // TODO: meake current method as private after migrating to java 9+
        int count = 0;
        WebDriver drv = null;
        Device device = nullDevice;

        // 1 - is default run without retry
        int maxCount = Configuration.getInt(Parameter.INIT_RETRY_COUNT) + 1;
        while (drv == null && count++ < maxCount) {
            try {
                LOGGER.debug("initDriver start...");
                
                Long threadId = Thread.currentThread().getId();
                ConcurrentHashMap<String, CarinaDriver> currentDrivers = getDrivers();

                int maxDriverCount = Configuration.getInt(Parameter.MAX_DRIVER_COUNT);

                if (currentDrivers.size() == maxDriverCount) {
                    Assert.fail("Unable to create new driver as you reached max number of drivers per thread: " + maxDriverCount + "!" +
                            " Override max_driver_count to allow more drivers per test!");
                }

                // [VD] pay attention that similar piece of code is copied into the DriverPoolTest as registerDriver method!
                if (currentDrivers.containsKey(name)) {
                    // [VD] moved containsKey verification before the driver start
                    Assert.fail("Driver '" + name + "' is already registered for thread: " + threadId);
                }
                
                drv = DriverFactory.create(name, capabilities, seleniumHost);

                if (device.isNull()) {
                    // During driver creation we choose device and assign it to
                    // the test thread
                    device = getDefaultDevice();
                }
                // push custom device name for log4j default messages
                if (!device.isNull()) {
                    MDC.put("device", "[" + device.getName() + "] ");
                }
                
                // moved proxy start logic here since device will be initialized
                // here only
                if (Configuration.getBoolean(Parameter.BROWSERMOB_PROXY)) {
                    if (!device.isNull()) {
                    	int proxyPort;
                        try {
                            proxyPort = Integer.parseInt(device.getProxyPort());
                        } catch (NumberFormatException e) {
                            // use default from _config.properties. Use-case for
                            // iOS devices which doesn't have proxy_port as part
                            // of capabilities
                            proxyPort = ProxyPool.getProxyPortFromConfig();
                        }
                        ProxyPool.startProxy(proxyPort);
                    }
                }

                
                // new 6.0 approach to manipulate drivers via regular Set
                CarinaDriver carinaDriver = new CarinaDriver(name, drv, device, TestPhase.getActivePhase(), threadId);
                
                //start timer to be able to track mobile app session time. It should be stopped on quitDriver!
                Timer.start(device.getMetricName(), carinaDriver.getName() + carinaDriver.getDevice().getName());
                driversPool.add(carinaDriver);

                LOGGER.debug("initDriver finish...");

            } catch (Exception e) {
                device.disconnectRemote();
                //TODO: [VD] think about excluding device from pool for explicit reasons like out of space etc
                // but initially try to implement it on selenium-hub level
                String msg = String.format("Driver initialization '%s' FAILED! Retry %d of %d time - %s", name, count,
                        maxCount, e.getMessage());
                LOGGER.error(msg, e); //TODO: test how 2 messages are displayed in logs and zafira
                if (count == maxCount) {
                    throw e;
                }
                CommonUtils.pause(Configuration.getInt(Parameter.INIT_RETRY_INTERVAL));
            }
        }
        
        if (drv == null) {
            throw new RuntimeException("Undefined exception detected! Analyze above logs for details.");
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

    // TODO: think about hiding getDriversCount and removing size when migration to java 9+ happens
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
        for (CarinaDriver carinaDriver : driversPool) {
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
        for (CarinaDriver carinaDriver : driversPool) {
            if (Phase.BEFORE_SUITE.equals(carinaDriver.getPhase())) {
                LOGGER.debug("Add suite_mode drivers into the getStaticDrivers response: " + carinaDriver.getName());
                currentDrivers.put(carinaDriver.getName(), carinaDriver.getDriver());
            } else if (threadId.equals(carinaDriver.getThreadId())) {
                LOGGER.debug("Add driver into the getStaticDrivers response: " + carinaDriver.getName() + " by threadId: "
                        + threadId);
                currentDrivers.put(carinaDriver.getName(), carinaDriver.getDriver());
            }
        }
        return currentDrivers;
    }

    // ------------------------ DEVICE POOL METHODS -----------------------
    /**
     * Get device registered to default driver. If no default driver discovered nullDevice will be returned.
     * 
     * @return default Device
     */
    default public Device getDevice() {
        return getDevice(DEFAULT);
    }

    /**
     * Get device registered to named driver. If no driver discovered nullDevice will be returned.
     * 
     * @param name
     *            String driver name
     * @return Device
     */
    default public Device getDevice(String name) {
        if (isDriverRegistered(name)) {
            return getDrivers().get(name).getDevice();
        } else {
            return nullDevice;
        }
        
    }

    /**
     * Register device information for current thread by MobileFactory and clear SysLog for Android only
     * 
     * @param device
     *            String Device device
     * 
     * @return Device device
     * 
     */
    public static Device registerDevice(Device device) {

        boolean stfEnabled = R.CONFIG
                .getBoolean(SpecialKeywords.CAPABILITIES + "." + SpecialKeywords.STF_ENABLED);
        if (stfEnabled) {
            device.connectRemote();
        }

        // register current device to be able to transfer it into Zafira at the end of the test
        long threadId = Thread.currentThread().getId();
        LOGGER.debug("Set current device '" + device.getName() + "' to thread: " + threadId);
        currentDevice.set(device);

        LOGGER.debug("register device for current thread id: " + threadId + "; device: '" + device.getName() + "'");

        // clear logcat log for Android devices
        device.clearSysLog();

        return device;
    }

    /**
     * Return last registered device information for current thread.
     * 
     * @return Device device
     * 
     */
    public static Device getDefaultDevice() {
        long threadId = Thread.currentThread().getId();
        Device device = currentDevice.get();
        if (device == null) {
            LOGGER.debug("Current device is null for thread: " + threadId);
            device = nullDevice;
        } else if (device.getName().isEmpty()) {
            LOGGER.debug("Current device name is empty! nullDevice was used for thread: " + threadId);
        } else {
            LOGGER.debug("Current device name is '" + device.getName() + "' for thread: " + threadId);
        }
        return device;
    }

    /**
     * Return nullDevice object to avoid NullPointerException and tons of verification across carina-core modules.
     * 
     * @return Device device
     * 
     */
    public static Device getNullDevice() {
        return nullDevice;
    }

    /**
     * Verify if device is registered in the Pool
     * 
     * 
     * @return boolean
     */
    default public boolean isDeviceRegistered() {
        Device device = currentDevice.get();
        return device != null && device != nullDevice;
    }
}
