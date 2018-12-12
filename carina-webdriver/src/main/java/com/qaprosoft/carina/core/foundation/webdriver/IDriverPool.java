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
import java.util.Map;
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
    static final Logger logger = Logger.getLogger(IDriverPool.class);
    static final String DEFAULT = "default";

    // unified set of Carina WebDrivers 
    static final Set<CarinaDriver> driversPool = new HashSet<CarinaDriver>();
  
    // the most popular drivers started inside tests
    //static final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>> drivers = new ConcurrentHashMap<Long, ConcurrentHashMap<String, WebDriver>>();

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
     * @return WebDriver by name
     */
    default public WebDriver getDriver(String name) {
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
    default public WebDriver getDriver(String name, DesiredCapabilities capabilities, String seleniumHost) {
        WebDriver drv = null;

        ConcurrentHashMap<String, CarinaDriver> currentDrivers = getDrivers();
        if (currentDrivers.containsKey(name)) {
        	CarinaDriver cdrv = currentDrivers.get(name);
            drv = cdrv.getDriver();
            if (Phase.BEFORE_SUITE.equals(cdrv.getPhase())) {
            	logger.info("Before suite registered driver will be returned.");
            } else {
            	logger.debug(cdrv.getPhase() + " registered driver will be returned.");
            } 
        }

        //Long threadId = Thread.currentThread().getId();
        //ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();
        
        //TODO [VD] do we really need finding by groupThreads?
/*        if (currentDrivers.containsKey(name)) {
            drv = currentDrivers.get(name);
        } else if (Configuration.getInt(Parameter.THREAD_COUNT) == 1
                && Configuration.getInt(Parameter.DATA_PROVIDER_THREAD_COUNT) <= 1) {
            Thread[] threads = getGroupThreads(Thread.currentThread().getThreadGroup());
            logger.debug(
                    "Try to find driver by ThreadGroup id values! Current ThreadGroup count is: " + threads.length);
            for (int i = 0; i < threads.length; i++) {
                currentDrivers = drivers.get(threads[i].getId());
                if (currentDrivers != null) {
                    if (currentDrivers.containsKey(name)) {
                        drv = currentDrivers.get(name);
                        logger.debug("##########        GET ThreadGroupId: " + threadId + "; driver: " + drv);
                        break;
                    }
                }
            }
        }*/

        if (drv == null) {
            logger.debug("Starting new driver as nothing was found in the pool");
            drv = createDriver(name, capabilities, seleniumHost);
        }

        // [VD] do not wrap EventFiringWebDriver here otherwise DriverListener and all logging will be lost!
        return drv;
        
    }

    /**
     * Get driver by WebElement.
     * 
     * @param sessionId - session id to be used for searching a desired driver
     * 
     * @return default WebDriver
     */
    public static WebDriver getDriver(SessionId sessionId) {
    	logger.debug("Detecting WebDriver by sessionId...");
    	
    	//TODO: [VD] do we need to get preliminary current drivers here to skip analysis for the drivers from different threads?
    	for (CarinaDriver carinDriver : driversPool) {
    		WebDriver drv = carinDriver.getDriver();
    		if (drv instanceof EventFiringWebDriver) {
    			EventFiringWebDriver eventFirDriver = (EventFiringWebDriver) drv;
    			drv = eventFirDriver.getWrappedDriver();
    		}
    		
    		SessionId drvSessionId = ((RemoteWebDriver)drv).getSessionId();
    		
    		logger.debug("analyzing driver: " + drvSessionId.toString());
    		if (sessionId.equals(drvSessionId)) {
    			logger.debug("Detected WebDriver by sessionId");
    			return drv;
    		}
    	}

    	throw new DriverPoolException("Unable to find driver using sessionId artifacts. Returning default one!");
    	//TODO: take a look into the replaceDriver case and how sessionId are regenerated on page objects
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
        	logger.debug("Get current device as driver is going to be restarted on the same device...");
            device = DevicePool.getDevice();
            logger.debug("Current device is: " + device.getName());
            logger.debug("Add udid: " + device.getUdid() + " to capabilities.");
            caps.setCapability("udid", device.getUdid());
        }

        try {
            logger.debug("Driver restarting...");
            deregisterDriver(DEFAULT);
            if (!isSameDevice) {
                DevicePool.deregisterDevice();
            }
            
            drv.quit();

            logger.debug("Driver exited during restart...");
        } catch (WebDriverException e) {
            logger.debug("Error message detected during driver restart: " + e.getMessage(), e);
            // do nothing
        } catch (Exception e) {
            logger.debug("Error discovered during driver restart: " + e.getMessage(), e);

            // TODO: it seems like BROWSER_TIMEOUT or NODE_FORWARDING should be handled here as well
            if (!e.getMessage().contains("Session ID is null.")) {
                throw e;
            }

        } finally {
            MDC.remove("device");
        }

        return createDriver(DEFAULT, caps, null);
    }

    /**
     * Quit default driver
     */
    default public void quitDriver() {
        quitDriver(DEFAULT);
    }

    /**
     * Quit driver by name
     * 
     * @param name
     *            String driver name
     */
    default public void quitDriver(String name) {
        WebDriver drv = getDriver(name);

        // TODO: try to understand valid place for recorded video file to support method/class and suite mode
        // 1. create for each driver their own video file.
        // 2. save it using unique driver/test/thread name - maybe time in ms
        // 3. register link onto the video as test artifact

        try {
            logger.debug("Driver exiting..." + name);
//          driver deregistration should be performed a bit later than device deregistration
//          since there some driver related action on method deregister device
            DevicePool.deregisterDevice();
            deregisterDriver(name);
            
            
            logger.debug("Driver starting quit..." + name);
            drv.quit();
            logger.debug("Driver finsihed quite." + name);
        } catch (WebDriverException e) {
            logger.debug("Error message detected during driver verification: " + e.getMessage(), e);
            // do nothing
        } catch (Exception e) {
            logger.debug("Error discovered during driver quit: " + e.getMessage(), e);

            // TODO: it seems like BROWSER_TIMEOUT or NODE_FORWARDING should be handled here as well
            if (!e.getMessage().contains("Session ID is null.")) {
                throw e;
            }

        } finally {
            // TODO analyze how to forcibly kill session on device
            MDC.remove("device");
        }
    }

    /**
     * Quit all drivers registered for current thread/test
     */
    default public void quitDrivers() {

        //ConcurrentHashMap<String, WebDriver> currentDrivers = getDrivers();
        ConcurrentHashMap<String, CarinaDriver> currentDrivers = getDrivers();

        for (Map.Entry<String, CarinaDriver> entry : currentDrivers.entrySet()) {
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
                logger.debug("initDriver start...");

                drv = DriverFactory.create(name, capabilities, seleniumHost);
                
                registerDriver(drv, name);

                init = true;

                if (device.isNull()) {
                    // During driver creation we choose device and assign it to the test thread
                    device = DevicePool.getDevice();
                }
                // push custom device name for log4j default messages
                if (!device.isNull()) {
                    MDC.put("device", "[" + device.getName() + "] ");
                }
                
				// moved proxy start logic here since device will be initialized here only
				if (Configuration.getBoolean(Parameter.BROWSERMOB_PROXY)) {
					int proxyPort = Configuration.getInt(Parameter.BROWSERMOB_PORT);
					if (!device.isNull()) {
						try{
							proxyPort = Integer.parseInt(device.getProxyPort());
						} catch(NumberFormatException e) {
							// use default from _config.properties. Use-case for
							// iOS devices which doesn't have proxy_port as part
							// of capabilities
							proxyPort = Configuration.getInt(Parameter.BROWSERMOB_PORT);
						}
						ProxyPool.startProxy(proxyPort);
					}
				}

                logger.debug("initDriver finish...");

            } catch (Exception e) {
                // DevicePool.ignoreDevice();
                DevicePool.deregisterDevice();
				logger.error(String.format("Driver initialization '%s' FAILED! Retry %d of %d time - %s", name, count,
						maxCount, e.getMessage()), e);
                init_throwable = e;
                CommonUtils.pause(Configuration.getInt(Parameter.INIT_RETRY_INTERVAL));
            }
        }

        if (!init) {
        	//TODO: think about this runtime exception 
            throw new RuntimeException(init_throwable);
        }

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
    default void registerDriver(WebDriver driver, String name) {
        Long threadId = Thread.currentThread().getId();
        ConcurrentHashMap<String, CarinaDriver> currentDrivers = getDrivers();
        
        int maxDriverCount = Configuration.getInt(Parameter.MAX_DRIVER_COUNT);
        
        if (currentDrivers.size() == maxDriverCount) {
            // TODO: after moving driver creation to DriverPoolEx need to add
            // such verification before driver start
            Assert.fail(
                    "Unable to register driver as you reached max number of drivers per thread: " + maxDriverCount);
        }
        if (currentDrivers.containsKey(name)) {
            Assert.fail("Driver '" + name + "' is already registered for thread: " + threadId);
        }

        //new 6.0 approach to manipulate drivers via regular Set
        CarinaDriver carinaDriver = new CarinaDriver(name, driver, TestPhase.getActivePhase(), threadId);
        driversPool.add(carinaDriver);
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

    
    //TODO: think about hiding getDriversCount and removing size
    /**
     * Return number of registered driver per thread
     * 
     * @return int
     */
    default public int getDriversCount() {
        Long threadId = Thread.currentThread().getId();
        int size = getDrivers().size();
        logger.debug("Number of registered drivers for thread '" + threadId + "' is " + size);
        return size;
    }
    
    /**
     * @deprecated use {@link #getDriversCount()} instead.
     * Return number of registered driver per thread
     * 
     * @return int
     */
    @Deprecated
    default public int size() {
        Long threadId = Thread.currentThread().getId();
        int size = getDrivers().size();
        logger.debug("Number of registered drivers for thread '" + threadId + "' is " + size);
        return size;
    }


    /**
     * Deregister driver by name from the DriverPool
     * 
     * @param name
     *            String driver name
     * 
     */
    default void deregisterDriver(String name) {
        long threadId = Thread.currentThread().getId();
        ConcurrentHashMap<String, CarinaDriver> currentDrivers = getDrivers();

        if (currentDrivers.containsKey(name)) {
            logger.debug("Deregister '" + name + "' driver from pool for threadId: " + threadId );
            driversPool.remove(currentDrivers.get(name));
        } else {
            logger.error("Unable to find '" + name + "' driver for deregistration in thread: " + threadId);
        }
        ProxyPool.stopProxy();
    }
    
    /**
     * Return all drivers registered in the DriverPool for this thread including on Before Suite/Class/Method stages 
     * 
     * @return ConcurrentHashMap of driver names and Carina WebDrivers
     * 
     */
    default public ConcurrentHashMap<String, CarinaDriver> getDrivers() {
        Long threadId = Thread.currentThread().getId();
        ConcurrentHashMap<String, CarinaDriver> currentDrivers = new ConcurrentHashMap<String, CarinaDriver>();
        // look inside driversPool and return all before_suite drivers and drivers mounted to the current thread_id
        for (CarinaDriver drv : driversPool) {
        	if (Phase.BEFORE_SUITE.equals(drv.getPhase())) {
        		// append all existing drivers in beforeSuite mode into the current list
        		currentDrivers.put(drv.getName(), drv);
        	} else if (threadId.equals(drv.getThreadId())) {
        		currentDrivers.put(drv.getName(), drv);
        	}
        }
        return currentDrivers;
    }
    
    @Deprecated
    public static WebDriver getDefaultDriver() {
        WebDriver drv = null;
        String name = DEFAULT;
        ConcurrentHashMap<String, WebDriver> currentDrivers = getStaticDrivers();

        if (currentDrivers.containsKey(name)) {
            drv = currentDrivers.get(name);
        }

        if (drv == null) {
        	throw new DriverPoolException("no default driver detected!");
        }

        // [VD] do not wrap EventFiringWebDriver here otherwise DriverListener and all logging will be lost!
        return drv;
    }
    
    @Deprecated
    public static ConcurrentHashMap<String, WebDriver> getStaticDrivers() {
        Long threadId = Thread.currentThread().getId();
        ConcurrentHashMap<String, WebDriver> currentDrivers = new ConcurrentHashMap<String, WebDriver>();
        // look inside driversPool and return all before_suite drivers and drivers mounted to the current thread_id
        for (CarinaDriver drv : driversPool) {
        	if (Phase.BEFORE_SUITE.equals(drv.getPhase())) {
        		// append all existing drivers in beforeSuite mode into the current list
        		currentDrivers.put(drv.getName(), drv.getDriver());
        	} else if (threadId.equals(drv.getThreadId())) {
        		currentDrivers.put(drv.getName(), drv.getDriver());
        	}
        }
        return currentDrivers;
    }
    
}
