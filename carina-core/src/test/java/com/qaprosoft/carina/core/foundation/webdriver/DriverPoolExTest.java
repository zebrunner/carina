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

import org.mockito.Mock;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

public class DriverPoolExTest {

    private final static String CUSTOM1 = "custom-1-driver";
    private final static String CUSTOM2 = "custom-2-driver";

    private final static String CLASS_MODE = "class_mode";
    // private final static String METHOD_MODE = "method_mode";
    private final static String SUITE_MODE = "suite_mode";

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
        R.CONFIG.put("driver_mode", SUITE_MODE);
        R.CONFIG.put("max_driver_count", "2");

        R.CONFIG.put("thread_count", "1");
        R.CONFIG.put("data_provider_thread_count", "1");

        this.mockDriverDefault = mock(WebDriver.class);
        this.mockDriverCustom1 = mock(WebDriver.class);
        this.mockDriverCustom2 = mock(WebDriver.class);
    }

    @Test
    public void suiteModeDriverTest() {
        Assert.assertFalse(DriverPool.isDriverRegistered(DriverPool.DEFAULT),
                "Default driver is mistakenly registered!");

        this.mockDriverSuite = mock(WebDriver.class);
        DriverPool.single_driver = mockDriverSuite;

        Assert.assertEquals(mockDriverSuite, DriverPool.getSingleDriver(),
                "Single driver for suite mode is not returned by getSingleDriver() method!");
        Assert.assertEquals(mockDriverSuite, DriverPool.getDriver(),
                "Single driver for suite mode is not returned by getDriver() method!");

        DriverPool.registerDriver(mockDriverSuite, DriverPool.DEFAULT);
        Assert.assertEquals(mockDriverSuite, DriverPool.getDriver(DriverPool.DEFAULT),
                "Single driver for suite mode is not returned by getDriver(DEFAULT) method!");

        DriverPool.quitDriver();
        Assert.assertEquals(null, DriverPool.getSingleDriver(),
                "Single driver for suite mode is not reset by quitDriver()!");

        Assert.assertFalse(DriverPool.isDriverRegistered(DriverPool.DEFAULT),
                "Default driver is mistakenly registered!");

        DriverPool.registerDriver(mockDriverDefault, DriverPool.DEFAULT);

        Assert.assertEquals(1, DriverPool.size(), "Number of registered driver is not valid!");
        Assert.assertTrue(DriverPool.isDriverRegistered(DriverPool.DEFAULT), "Default driver is not registered!");

        Assert.assertEquals(mockDriverDefault, DriverPool.single_driver, "Number of registered driver is not valid!");
        Assert.assertEquals(mockDriverDefault, DriverPool.getDriver(), "Number of registered driver is not valid!");

        DriverPool.deregisterDriver(DriverPool.DEFAULT);
        Assert.assertFalse(DriverPool.isDriverRegistered(DriverPool.DEFAULT), "Default driver is not deregistered!");
        Assert.assertEquals(0, DriverPool.size(), "Number of registered driver is not valid!");

        R.CONFIG.put("driver_mode", CLASS_MODE);
    }

    @Test(dependsOnMethods = "suiteModeDriverTest")
    public void registerDefaultDriver() {
        Assert.assertEquals(CLASS_MODE, R.CONFIG.get("driver_mode"), "driver_mode is invalid!");

        DriverPool.registerDriver(mockDriverDefault, DriverPool.DEFAULT);
        Assert.assertEquals(1, DriverPool.size(), "Number of registered driver is not valid!");
        Assert.assertTrue(DriverPool.isDriverRegistered(DriverPool.DEFAULT), "Default driver is not registered!");

        Assert.assertEquals(mockDriverDefault, DriverPool.getDriver(),
                "Returned driver is not the same as registered!");
    }

    @Test(dependsOnMethods = { "registerDefaultDriver" })
    public void deregisterDefaultDriver() {
        DriverPool.deregisterDriver(DriverPool.DEFAULT);
        Assert.assertFalse(DriverPool.isDriverRegistered(DriverPool.DEFAULT), "Default driver is not deregistered!");
        Assert.assertEquals(0, DriverPool.size(), "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "deregisterDefaultDriver" })
    public void registerCustom1Driver() {
        DriverPool.registerDriver(mockDriverCustom1, CUSTOM1);
        Assert.assertTrue(DriverPool.isDriverRegistered(CUSTOM1), "Custom1 driver is not registered!");
        Assert.assertEquals(1, DriverPool.size(), "Number of registered driver is not valid!");

    }

    @Test(dependsOnMethods = "registerCustom1Driver")
    public void getCustom1Driver() {
        WebDriver driver = DriverPool.getDriver(CUSTOM1);
        Assert.assertEquals(mockDriverCustom1, driver, "Returned driver is not the same as registered!");
    }

    @Test(dependsOnMethods = "getCustom1Driver", expectedExceptions = {
            AssertionError.class }, expectedExceptionsMessageRegExp = "Unable to register driver as you reached max number of drivers per thread: 2")
    public void reachMaxDriverCountTest() {
        DriverPool.registerDriver(mockDriverDefault, DriverPool.DEFAULT);

        DriverPool.registerDriver(mockDriverCustom2, CUSTOM2);
        Assert.assertFalse(DriverPool.isDriverRegistered(CUSTOM2),
                CUSTOM2 + " driver is registered in spite of the max_drivercount=2");
        Assert.assertEquals(2, DriverPool.size(), "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "reachMaxDriverCountTest" })
    public void deregisterCustom1Driver() {
        DriverPool.deregisterDriver(CUSTOM1);
        Assert.assertFalse(DriverPool.isDriverRegistered(CUSTOM1), CUSTOM1 + " driver is not deregistered!");
        Assert.assertEquals(1, DriverPool.size(), "Number of registered driver is not valid!");

        DriverPool.deregisterDrivers();
        Assert.assertEquals(0, DriverPool.size(), "Number of registered driver is not valid!");
    }

    @Test(dependsOnMethods = { "deregisterCustom1Driver" })
    public void deregisterAllDrivers() {
        DriverPool.registerDriver(mockDriverDefault, DriverPool.DEFAULT);
        Assert.assertEquals(1, DriverPool.size(), "Number of registered driver is not valid!");
        DriverPool.registerDriver(mockDriverCustom1, CUSTOM1);
        Assert.assertEquals(2, DriverPool.size(), "Number of registered driver is not valid!");
        DriverPool.deregisterDrivers();
        Assert.assertEquals(0, DriverPool.size(), "Number of registered driver is not valid!");
    }

    /*
     * @Test(expectedExceptions = {
     * AssertionError.class }, expectedExceptionsMessageRegExp = "Unable to find 'NOT-EXISTED-DRIVER' driver for deregistration!")
     * public void deregisterInvalidDriver() {
     * DriverPool.deregisterDriver("NOT-EXISTED-DRIVER");
     * }
     */
    @Test(dependsOnMethods = "deregisterAllDrivers")
    public void replaceDefaultDriver() {
        DriverPool.registerDriver(mockDriverDefault, DriverPool.DEFAULT);
        Assert.assertEquals(1, DriverPool.size(), "Number of registered driver is not valid!");
        Assert.assertEquals(mockDriverDefault, DriverPool.getDriver(), "Default driver is not valid!");

        DriverPool.replaceDriver(mockDriverCustom1);
        Assert.assertEquals(1, DriverPool.size(), "Number of registered driver is not valid!");
        Assert.assertEquals(mockDriverCustom1, DriverPool.getDriver(), "Replaced driver is not valid!");

        DriverPool.deregisterDrivers();
    }

    @Test(dependsOnMethods = "replaceDefaultDriver")
    public void replaceCustomDriver() {
        DriverPool.registerDriver(mockDriverCustom1, CUSTOM1);
        Assert.assertEquals(1, DriverPool.size(), "Number of registered driver is not valid!");
        Assert.assertEquals(mockDriverCustom1, DriverPool.getDriver(CUSTOM1), "Default driver is not valid!");

        DriverPool.replaceDriver(mockDriverCustom2, CUSTOM1);
        Assert.assertEquals(1, DriverPool.size(), "Number of registered driver is not valid!");
        Assert.assertEquals(mockDriverCustom2, DriverPool.getDriver(CUSTOM1), "Replaced driver is not valid!");

        DriverPool.deregisterDrivers();
    }

}
