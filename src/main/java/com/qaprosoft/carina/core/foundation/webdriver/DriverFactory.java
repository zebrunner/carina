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

import java.net.URL;

import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileCapabilies;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.DesktopFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.MobileFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

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


    @SuppressWarnings("rawtypes")
	public static WebDriver create(String testName, DesiredCapabilities capabilities, String selenium_host) {
        RemoteWebDriver driver = null;
        try {
            if (capabilities.getCapability("automationName") == null)
                driver = new RemoteWebDriver(new URL(selenium_host), capabilities);
            else {
                String platform;
                if (capabilities.getCapability("platform") != null) {
                    platform = capabilities.getCapability("platform").toString();
                } else if (capabilities.getCapability("platformName") != null) {
                    platform = capabilities.getCapability("platformName").toString();
                } else {
                    throw new RuntimeException("Unable to identify platform type using platform and platformName capabilities for test: " + testName);
                }

                if (platform.toLowerCase().equals("android")) {
                    driver = new AndroidDriver(new URL(selenium_host), capabilities);
                } else if (platform.toLowerCase().equals("ios")) {
                    driver = new IOSDriver(new URL(selenium_host), capabilities);
                } else {
                    throw new RuntimeException("Undefined platform type for mobile driver test: " + testName);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to initialize extra driver!\r\n" + e.getMessage());
        }

        return driver;
    }

    /**
     * Creates driver instance for specified test.
     *
     * @param testName in which driver will be used
     * @return RemoteWebDriver instance
     */
    public static WebDriver create(String testName) {
        return create(testName, nullDevice);
    }

	public static WebDriver create(String testName, Device device) {
		AbstractFactory factory;
		String driverType = Configuration.get(Parameter.DRIVER_TYPE);
		if (driverType.equalsIgnoreCase(SpecialKeywords.DESKTOP)) {
			factory = new DesktopFactory();
		} else if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE)
				|| driverType.equalsIgnoreCase(SpecialKeywords.MOBILE_POOL)
				|| driverType.equalsIgnoreCase(SpecialKeywords.MOBILE_GRID)) {
			factory = new MobileFactory();
		} else {
			throw new RuntimeException("Unsupported driver_type: " + driverType + "!");
		}
		return factory.create(testName, device);
	}


    public static String getBrowserName(WebDriver driver) {
        Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
        return cap.getBrowserName().toString();
    }

    public static String getBrowserVersion(WebDriver driver) {
        Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
        return cap.getVersion().toString();
    }
    
    @Deprecated
    public static DesiredCapabilities getMobileWebCapabilities(boolean gridMode, String testName, String platform, String platformVersion, String deviceName,
            String automationName, String commandTimeout, String browserName) {
    	return MobileCapabilies.getMobileCapabilities(gridMode, platform, platformVersion, deviceName, automationName, commandTimeout, browserName, "", "", "");
    }


}