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

import static org.mockito.Mockito.mock;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.mockito.Mock;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;

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
        Assert.assertEquals(0, getDriversCount(), "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "quiteSuiteDriver" })
    public void registerDefaultDriver() {
        R.CONFIG.put("max_driver_count", "2");

        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);
        Assert.assertEquals(1, getDriversCount(), "Number of registered driver is not valid!");
        Assert.assertTrue(isDriverRegistered(IDriverPool.DEFAULT), "Default driver is not registered!");

        Assert.assertEquals(mockDriverDefault, getDriver(), "Returned driver is not the same as registered!");
    }

    @Test(dependsOnMethods = "registerDefaultDriver", expectedExceptions = {
            AssertionError.class }, expectedExceptionsMessageRegExp = "Driver 'default' is already registered for thread: 1")
    public void registerTwiceDefaultDriver() {
        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);
    }

    @Test(dependsOnMethods = { "registerDefaultDriver", "registerTwiceDefaultDriver" })
    public void deregisterDefaultDriver() {
        deregisterDriver(mockDriverDefault);
        Assert.assertFalse(isDriverRegistered(IDriverPool.DEFAULT), "Default driver is not deregistered!");
        LOGGER.info("drivers count: " + getDriversCount());
        Assert.assertEquals(0, getDriversCount(), "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "deregisterDefaultDriver" })
    public void registerCustom1Driver() {
        registerDriver(mockDriverCustom1, CUSTOM1);
        Assert.assertTrue(isDriverRegistered(CUSTOM1), "Custom1 driver is not registered!");
        Assert.assertEquals(1, getDriversCount(), "Number of registered driver is not valid!");

    }

    @Test(dependsOnMethods = "registerCustom1Driver")
    public void getCustom1Driver() {
        WebDriver driver = getDriver(CUSTOM1);
        Assert.assertEquals(mockDriverCustom1, driver, "Returned driver is not the same as registered!");
    }

    @Test(dependsOnMethods = "getCustom1Driver", expectedExceptions = {
            AssertionError.class }, expectedExceptionsMessageRegExp = "Unable to register driver as you reached max number of drivers per thread: 2")
    public void reachMaxDriverCountTest() {
        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);

        registerDriver(mockDriverCustom2, CUSTOM2);
        Assert.assertFalse(isDriverRegistered(CUSTOM2),
                CUSTOM2 + " driver is registered in spite of the max_drivercount=2");
        Assert.assertEquals(2, getDriversCount(), "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "reachMaxDriverCountTest" })
    public void deregisterCustom1Driver() {
        deregisterDriver(mockDriverCustom1);
        Assert.assertFalse(isDriverRegistered(CUSTOM1), CUSTOM1 + " driver is not deregistered!");
        Assert.assertEquals(1, getDriversCount(), "Number of registered driver is not valid!");

        deregisterDriver(mockDriverDefault);
        Assert.assertEquals(0, getDriversCount(), "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "deregisterCustom1Driver" })
    public void deregisterAllDrivers() {
        registerDriver(mockDriverDefault, IDriverPool.DEFAULT);
        Assert.assertEquals(1, getDriversCount(), "Number of registered driver is not valid!");
        registerDriver(mockDriverCustom1, CUSTOM1);
        Assert.assertEquals(2, getDriversCount(), "Number of registered driver is not valid!");
    }

/*    @AfterSuite()
    public void afterSuite() {
        Assert.assertEquals(0, getDriversCount(), "Number of registered driver is not valid!");
    }
*/     
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
     * 
     * @param name
     *            String driver name
     * 
     */
    private void registerDriver(WebDriver driver, String name) {
        Long threadId = Thread.currentThread().getId();
        ConcurrentHashMap<String, CarinaDriver> currentDrivers = getDrivers();

        int maxDriverCount = Configuration.getInt(Parameter.MAX_DRIVER_COUNT);

        if (currentDrivers.size() == maxDriverCount) {
            // TODO: after moving driver creation to DriverPoolEx need to add
            // such verification before driver start
            Assert.fail("Unable to register driver as you reached max number of drivers per thread: " + maxDriverCount);
        }
        if (currentDrivers.containsKey(name)) {
            Assert.fail("Driver '" + name + "' is already registered for thread: " + threadId);
        }

        // new 6.0 approach to manipulate drivers via regular Set
        CarinaDriver carinaDriver = new CarinaDriver(name, driver, TestPhase.getActivePhase(), threadId);
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
