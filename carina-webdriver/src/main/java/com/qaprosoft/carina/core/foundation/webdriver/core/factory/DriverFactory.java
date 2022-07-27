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
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.IAbstactCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.AndroidFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.ChromeFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.CustomAndroidMobileFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.CustomIOSMobileFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.FirefoxFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.IOSFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.SafariFactory;
import com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener;
import com.zebrunner.agent.core.webdriver.RemoteWebDriverFactory;

/**
 * DriverFactory produces driver instance with desired capabilities according to
 * configuration.
 *
 * @author Alexey Khursevich (hursevich@gmail.com)
 */
public class DriverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static WebDriver create(String testName, Capabilities additionalCapabilities, String seleniumHost) {
		LOGGER.debug("DriverFactory start...");
        URL seleniumUrl = RemoteWebDriverFactory.getSeleniumHubUrl();
        if (seleniumUrl != null) {
            // override existing selenium_url in config
            R.CONFIG.put(Parameter.SELENIUM_URL.getKey(), seleniumUrl.toString());
        }

        IAbstractFactory driverFactory = chooseDriverFactory(IAbstactCapabilities.getConfigurationCapabilities());

        WebDriver driver = driverFactory.create(testName, additionalCapabilities, seleniumHost);
        LOGGER.debug("DriverFactory finish...");
        return driver;
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

    private static IAbstractFactory chooseDriverFactory(Capabilities capabilities) {

        if (AndroidFactory.isSuitable(capabilities)) {
            return new AndroidFactory();
        }

        if (CustomAndroidMobileFactory.isSuitable(capabilities)) {
            return new CustomAndroidMobileFactory();
        }

        if (IOSFactory.isSuitable(capabilities)) {
            return new IOSFactory();
        }

        if (CustomIOSMobileFactory.isSuitable(capabilities)) {
            return new CustomIOSMobileFactory();
        }

        if (SafariFactory.isSuitable(capabilities)) {
            return new SafariFactory();
        }

        if (ChromeFactory.isSuitable(capabilities)) {
            return new ChromeFactory();
        }

        if (FirefoxFactory.isSuitable(capabilities)) {
            return new FirefoxFactory();
        }

        // fixme add default driver factory instance
        return null;

    }

}