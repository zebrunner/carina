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

import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.DesktopFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.MobileFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

/**
 * DriverFactory produces driver instance with desired capabilities according to
 * configuration.
 *
 * @author Alexey Khursevich (hursevich@gmail.com)
 */

public class DriverFactory {
    public static final String HTML_UNIT = "htmlunit";

    protected static final Logger LOGGER = Logger.getLogger(DriverFactory.class);

    private static final Device nullDevice = new Device();

    /**
     * Creates driver instance for specified test.
     *
     * @param testName in which driver will be used
     * @return RemoteWebDriver instance
     */
    public static WebDriver create(String testName) {
        return create(testName, nullDevice, null, null);
    }

    public static WebDriver create(String testName, DesiredCapabilities capabilities, String selenium_host) {
    	return create(testName, nullDevice, capabilities, selenium_host);
    }
    
	public static WebDriver create(String testName, Device device, DesiredCapabilities capabilities, String selenium_host) {
		LOGGER.debug("DriverFactory start...");
		AbstractFactory factory;
		String driverType = Configuration.getDriverType();
		if (driverType.equalsIgnoreCase(SpecialKeywords.DESKTOP)) {
			factory = new DesktopFactory();
		} else if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE)) {
			factory = new MobileFactory();
		} else {
			throw new RuntimeException("Unsupported driver_type: " + driverType + "!");
		}
		WebDriver drv = factory.create(testName, device, capabilities, selenium_host);
		LOGGER.debug("DriverFactory finish...");
		return drv;
	}


    public static String getBrowserName(WebDriver driver) {
        Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
        return cap.getBrowserName().toString();
    }

    public static String getBrowserVersion(WebDriver driver) {
        Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
        return cap.getVersion().toString();
    }

}