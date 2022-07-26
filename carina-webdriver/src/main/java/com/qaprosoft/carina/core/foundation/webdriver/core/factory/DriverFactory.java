/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.core.factory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebDriverBuilder;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesBuilder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.OptionsType;
import com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener;
import com.zebrunner.agent.core.webdriver.RemoteWebDriverFactory;

import io.appium.java_client.remote.AutomationName;

/**
 * DriverFactory produces driver instance with desired capabilities according to
 * configuration.
 *
 * @author Alexey Khursevich (hursevich@gmail.com)
 */
public class DriverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static WebDriver create(String testName, Capabilities capabilities, String seleniumHost) {
		LOGGER.debug("DriverFactory start...");
        RemoteWebDriverBuilder webDriverBuilder = RemoteWebDriver.builder();

        // todo it is a magic?
        URL seleniumUrl = RemoteWebDriverFactory.getSeleniumHubUrl();
        if (seleniumUrl != null) {
            // override existing selenium_url in config
            R.CONFIG.put(Parameter.SELENIUM_URL.getKey(), seleniumUrl.toString());
        }

        webDriverBuilder.address(seleniumHost);

        CapabilitiesBuilder capabilitiesBuilder = CapabilitiesBuilder.builder();
        chooseCapabilitiesType(capabilitiesBuilder, testName);
        capabilitiesBuilder.withCapabilities(capabilities);
        webDriverBuilder.oneOf(capabilitiesBuilder.build());

        // driver = factory.registerListeners(driver, getEventListeners());
		
		LOGGER.debug("DriverFactory finish...");

        return webDriverBuilder.build();
	}
	
	   /**
     * Reads 'driver_event_listeners' configuration property and initializes
     * appropriate array of driver event listeners.
     * 
     * @return array of driver listeners
     */
    private static WebDriverEventListener[] getEventListeners() {
        List<WebDriverEventListener> listeners = new ArrayList<>();
        try {
            //explicitly add default carina com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener
            DriverListener driverListener = new DriverListener();
            listeners.add(driverListener);

            String listenerClasses = Configuration.get(Parameter.DRIVER_EVENT_LISTENERS);
            if (!StringUtils.isEmpty(listenerClasses)) {
                for (String listenerClass : listenerClasses.split(",")) {
                    Class<?> clazz = Class.forName(listenerClass);
                    if (WebDriverEventListener.class.isAssignableFrom(clazz)) {
                        WebDriverEventListener listener = (WebDriverEventListener) clazz.newInstance();
                        listeners.add(listener);
                        LOGGER.debug("Webdriver event listener registered: " + clazz.getName());
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Unable to register webdriver event listeners!", e);
        }
        return listeners.toArray(new WebDriverEventListener[listeners.size()]);
    }

    /**
     * Choose capabilities type depends on configuration
     * 
     * @param capabilitiesBuilder
     */
    private static void chooseCapabilitiesType(CapabilitiesBuilder capabilitiesBuilder, String testName) {
        String browser = Configuration.getBrowser();
        String automationName = Configuration.getAutomationName();

        if (AutomationName.ANDROID_UIAUTOMATOR2.equalsIgnoreCase(automationName)) {
            capabilitiesBuilder.chooseOptionsType(OptionsType.ANDROID_UIAUTOMATOR2_APPIUM);
            return;
        }

        if (!IDriverPool.DEFAULT.equalsIgnoreCase(testName)) {
            // #1573: remove "default" driver name capability registration
            capabilitiesBuilder.withTestName(testName);
        }

        if (Browser.CHROME.browserName().equalsIgnoreCase(browser)) {
            capabilitiesBuilder.chooseOptionsType(OptionsType.CHROME_SELENIUM);
            return;
        }
        if (Browser.FIREFOX.browserName().equalsIgnoreCase(browser)) {
            capabilitiesBuilder.chooseOptionsType(OptionsType.FIREFOX_SELENIUM);
            return;
        }
    }

}