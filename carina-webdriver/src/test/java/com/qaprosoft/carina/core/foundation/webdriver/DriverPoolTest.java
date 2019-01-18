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

import static org.mockito.Mockito.mock;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.mockito.Mock;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.exception.DriverPoolException;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public class DriverPoolTest implements IDriverPool {

    private final static String BEFORE_SUITE_DRIVER_NAME = "custom-0-driver";
    private final static String CUSTOM1 = "custom-1-driver";
    private final static String CUSTOM2 = "custom-2-driver";

    @Mock
    private WebDriver mockDriverSuite;

    @Mock
    private WebDriver mockDriverDefault;

    @Mock
    private WebDriver mockDriverDefault2;

    @Mock
    private WebDriver mockDriverCustom1;

    @Mock
    private WebDriver mockDriverCustom2;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        TestPhase.setActivePhase(Phase.BEFORE_SUITE);
        R.CONFIG.put("driver_type", "desktop");
        R.CONFIG.put("thread_count", "1");
        R.CONFIG.put("data_provider_thread_count", "1");

        this.mockDriverSuite = mock(WebDriver.class);
        registerDriver(mockDriverSuite, BEFORE_SUITE_DRIVER_NAME);
        Assert.assertEquals(driversPool.size(), 1,
                "Driver pool is empty after before suite driver has been registered");
        Assert.assertEquals(getDriver(BEFORE_SUITE_DRIVER_NAME), mockDriverSuite, "Incorrect driver has been returned");
        changeBeforeSuiteDriverThread();

        this.mockDriverDefault = mock(WebDriver.class);
        this.mockDriverCustom1 = mock(WebDriver.class);
        this.mockDriverCustom2 = mock(WebDriver.class);
    }

    @Test()
    public void beforeClassGetSuiteDriver() {
        TestPhase.setActivePhase(Phase.BEFORE_CLASS);
        Assert.assertEquals(getDriver(BEFORE_SUITE_DRIVER_NAME), mockDriverSuite, "Incorrect driver has been returned");
        Assert.assertTrue(getDrivers().containsKey(BEFORE_SUITE_DRIVER_NAME), "Before suite driver has not been returned by getDrivers()");
        Assert.assertTrue(IDriverPool.getStaticDrivers().containsKey(BEFORE_SUITE_DRIVER_NAME), "Before suite driver has not been returned by getStaticDrivers()");
    }

    @Test(dependsOnMethods = { "beforeClassGetSuiteDriver" })
    public void beforeMethodGetSuiteDriver() {
        TestPhase.setActivePhase(Phase.BEFORE_METHOD);
        Assert.assertEquals(getDriver(BEFORE_SUITE_DRIVER_NAME), mockDriverSuite, "Incorrect driver has been returned");
    }

    @Test(dependsOnMethods = { "beforeMethodGetSuiteDriver" })
    public void methodGetSuiteDriver() {
        TestPhase.setActivePhase(Phase.METHOD);
        Assert.assertEquals(getDriver(BEFORE_SUITE_DRIVER_NAME), mockDriverSuite, "Incorrect driver has been returned");
    }

    @Test(dependsOnMethods = { "methodGetSuiteDriver" })
    public void quiteSuiteDriver() {
        deregisterDriver(mockDriverSuite);
        Assert.assertEquals(getDriversCount(), 0, "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "quiteSuiteDriver" })
    public void registerDefaultDriver() {
        R.CONFIG.put("max_driver_count", "2");

        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);
        Assert.assertEquals(getDriversCount(), 1, "Number of registered driver is not valid!");
        Assert.assertTrue(isDriverRegistered(IDriverPool.DEFAULT), "Default driver is not registered!");

        Assert.assertEquals(getDriver(), mockDriverDefault, "Returned driver is not the same as registered!");
    }
    
    @SuppressWarnings("deprecation")
    @Test(dependsOnMethods = { "registerDefaultDriver" })
    public void testStaticMethods() {
        Assert.assertEquals(size(), 1, "Number of registered driver is not valid!");
        Assert.assertEquals(IDriverPool.getDefaultDriver(), mockDriverDefault, "Returned driver is not the same as registered!");
    }
    

    @Test(dependsOnMethods = "registerDefaultDriver", expectedExceptions = {
            AssertionError.class }, expectedExceptionsMessageRegExp = "Driver 'default' is already registered for thread: 1")
    public void registerTwiceDefaultDriver() {
        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);
    }

    @Test(dependsOnMethods = { "registerDefaultDriver", "registerTwiceDefaultDriver" })
    public void deregisterDefaultDriver() {
        quitDriver();
        deregisterDriver(mockDriverDefault);
        Assert.assertFalse(isDriverRegistered(IDriverPool.DEFAULT), "Default driver is not deregistered!");
        LOGGER.info("drivers count: " + getDriversCount());
        Assert.assertEquals(getDriversCount(), 0, "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "deregisterDefaultDriver" })
    public void quitDriverByPhase() {
        TestPhase.setActivePhase(Phase.BEFORE_METHOD);
        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);
        Assert.assertEquals(getDriversCount(), 1, "Number of registered driver is not valid!");
        quitDrivers(Phase.BEFORE_METHOD);
        Assert.assertEquals(getDriversCount(), 0, "Number of registered driver is not valid!");
    }
    
    @Test(dependsOnMethods = { "quitDriverByPhase" })
    public void quitDefaultDriver() {
        TestPhase.setActivePhase(Phase.METHOD);
        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);
        Assert.assertEquals(getDriversCount(), 1, "Number of registered driver is not valid!");
        quitDriver();
        Assert.assertEquals(getDriversCount(), 0, "Number of registered driver is not valid!");
    }
    
    @Test(dependsOnMethods = { "quitDefaultDriver" })
    public void quitDriverByName() {
        TestPhase.setActivePhase(Phase.METHOD);
        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);
        Assert.assertEquals(1, getDriversCount(), "Number of registered driver is not valid!");
        quitDriver(IDriverPool.DEFAULT);
        Assert.assertEquals(0, getDriversCount(), "Number of registered driver is not valid!");
    }
    
    @Test(dependsOnMethods = { "quitDriverByName" })
    public void registerCustom1Driver() {
        registerDriver(mockDriverCustom1, CUSTOM1);
        Assert.assertTrue(isDriverRegistered(CUSTOM1), "Custom1 driver is not registered!");
        Assert.assertEquals(getDriversCount(), 1, "Number of registered driver is not valid!");

    }

    @Test(dependsOnMethods = "registerCustom1Driver")
    public void getCustom1Driver() {
        Assert.assertEquals(getDriver(CUSTOM1), mockDriverCustom1, "Returned driver is not the same as registered!");
    }

    @Test(dependsOnMethods = "getCustom1Driver", expectedExceptions = {
            AssertionError.class }, expectedExceptionsMessageRegExp = "Unable to register driver as you reached max number of drivers per thread: 2")
    public void reachMaxDriverCountTest() {
        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);

        registerDriver(mockDriverCustom2, CUSTOM2);
        Assert.assertFalse(isDriverRegistered(CUSTOM2),
                CUSTOM2 + " driver is registered in spite of the max_drivercount=2");
        Assert.assertEquals(getDriversCount(), 2, "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "reachMaxDriverCountTest" })
    public void deregisterCustom1Driver() {
        deregisterDriver(mockDriverCustom1);
        Assert.assertFalse(isDriverRegistered(CUSTOM1), CUSTOM1 + " driver is not deregistered!");
        Assert.assertEquals(getDriversCount(), 1, "Number of registered driver is not valid!");

        deregisterDriver(mockDriverDefault);
        Assert.assertEquals(getDriversCount(), 0, "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "deregisterCustom1Driver" })
    public void deregisterAllDrivers() {
        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);
        Assert.assertEquals(getDriversCount(), 1, "Number of registered driver is not valid!");
        registerDriver(mockDriverCustom1, CUSTOM1);
        Assert.assertEquals(getDriversCount(), 2, "Number of registered driver is not valid!");
        
        quitDrivers(Phase.ALL);
        Assert.assertEquals(getDriversCount(), 0, "Number of registered driver is not valid!");
    }
    
    @Test(dependsOnMethods = { "deregisterAllDrivers" })
    public void registerDriverWithDevice() {
        WebDriver deviceDriver = mock(WebDriver.class);
        Device device = new Device("name", "type", "os", "osVersion", "udid", "remoteUrl");
        registerDriver(deviceDriver, IDriverPool.DEFAULT, device);
        Assert.assertEquals(getDriversCount(), 1, "Number of registered driver is not valid!");
        
        Assert.assertEquals(getDriver(), deviceDriver, "Returned driver is not the same as registered!");
        Assert.assertEquals(getDevice(), device, "Returned device is not the same as registered!");
        quitDrivers(Phase.ALL);
    }
    
    @SuppressWarnings("deprecation")
    @Test(dependsOnMethods = { "registerDriverWithDevice" }, expectedExceptions = {
            DriverPoolException.class }, expectedExceptionsMessageRegExp = "no default driver detected!")
    public void getDefaultNotExistedDriver() {
        IDriverPool.getDefaultDriver();
    }

   
    private void changeBeforeSuiteDriverThread() {
        for (CarinaDriver cDriver : driversPool) {
            if (Phase.BEFORE_SUITE.equals(cDriver.getPhase())) {
                long newThreadID = cDriver.getThreadId() + 1;
                cDriver.setThreadId(newThreadID);
            }
        }
    }

    /**
     * Register driver in the DriverPool
     * 
     * @param driver
     *            WebDriver
     * @param name
     *            String driver name
     * 
     */
    private void registerDriver(WebDriver driver, String name) {
        registerDriver(driver, name, IDriverPool.getNullDevice());
    }
    /**
     * Register driver in the DriverPool with device
     * 
     * @param driver
     *            WebDriver
     * @param name
     *            String driver name
     * @param device
     *            Device
     * 
     */
    private void registerDriver(WebDriver driver, String name, Device device) {
        Long threadId = Thread.currentThread().getId();
        ConcurrentHashMap<String, CarinaDriver> currentDrivers = getDrivers();

        int maxDriverCount = Configuration.getInt(Parameter.MAX_DRIVER_COUNT);

        if (currentDrivers.size() == maxDriverCount) {
            Assert.fail("Unable to register driver as you reached max number of drivers per thread: " + maxDriverCount);
        }
        if (currentDrivers.containsKey(name)) {
            Assert.fail("Driver '" + name + "' is already registered for thread: " + threadId);
        }

        // new 6.0 approach to manipulate drivers via regular Set
        CarinaDriver carinaDriver = new CarinaDriver(name, driver, device, TestPhase.getActivePhase(), threadId);
        driversPool.add(carinaDriver);
    }

    /**
     * Deregister driver from the DriverPool
     * 
     * @param drv
     *            WebDriver driver
     * 
     */
    private void deregisterDriver(WebDriver drv) {

        Iterator<CarinaDriver> iter = driversPool.iterator();

        while (iter.hasNext()) {
            CarinaDriver carinaDriver = iter.next();

            if (carinaDriver.getDriver().equals(drv)) {
                LOGGER.info("removing driver " + carinaDriver.getName());
                iter.remove();
            }
        }
    }
}
