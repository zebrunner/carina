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
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.BrowserstackPostCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.BrowserstackPreCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.CapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.ChromeCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.EdgeCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.FirefoxCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.GeckoCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.LambdaTestPreCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.MCloudPostCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.Mac2CapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.SafariCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.SauceLabsPreCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.UIAutomaror2CapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.WindowsCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.XCUITestCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.ZebrunnerPreCapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.AndroidFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.ChromeFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.EdgeFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.FirefoxFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.GeckoFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.IOSFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.MacFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.SafariFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain.WindowsFactory;
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

    public static WebDriver create(String testName, String seleniumHost, MutableCapabilities capabilities) {
		LOGGER.debug("DriverFactory start...");

        URL seleniumUrl = RemoteWebDriverFactory.getSeleniumHubUrl();
        if (seleniumUrl != null) {
            // override existing selenium_url in config
            R.CONFIG.put(Parameter.SELENIUM_URL.getKey(), seleniumUrl.toString());
            seleniumHost = seleniumUrl.toString();
        }

        CapabilitiesMiddleware preCapabilitiesMiddleware = CapabilitiesMiddleware.link(
                new BrowserstackPreCapabilitiesMiddleware(),
                new SauceLabsPreCapabilitiesMiddleware(),
                new ZebrunnerPreCapabilitiesMiddleware(testName),
                new LambdaTestPreCapabilitiesMiddleware());

        CapabilitiesMiddleware capabilitiesMiddleware = CapabilitiesMiddleware.link(
                new ChromeCapabilitiesMiddleware(),
                new FirefoxCapabilitiesMiddleware(),
                new EdgeCapabilitiesMiddleware(),
                new UIAutomaror2CapabilitiesMiddleware(),
                new XCUITestCapabilitiesMiddleware(),
                new GeckoCapabilitiesMiddleware(),
                new Mac2CapabilitiesMiddleware(),
                new SafariCapabilitiesMiddleware(),
                new WindowsCapabilitiesMiddleware());

        CapabilitiesMiddleware postCapabilitiesMiddleware = CapabilitiesMiddleware.link(
                new BrowserstackPostCapabilitiesMiddleware(),
                new MCloudPostCapabilitiesMiddleware());

        AbstractFactory driverFactory = AbstractFactory.link(
                new ChromeFactory(),
                new EdgeFactory(),
                new FirefoxFactory(),
                new AndroidFactory(),
                new IOSFactory(),
                new SafariFactory(),
                new WindowsFactory(),
                new MacFactory(),
                new GeckoFactory());

        capabilities = capabilities == null ? AbstractCapabilities.getConfigurationCapabilities() : capabilities;
        capabilities = preCapabilitiesMiddleware.analyze(capabilities);
        capabilities = capabilitiesMiddleware.analyze(capabilities);

        AbstractFactory suitableFactory = driverFactory
                .getSuitableDriverFactory(capabilities);

        LOGGER.info("Starting driver session...");

        capabilities = postCapabilitiesMiddleware.analyze(capabilities);

        WebDriver driver = suitableFactory.getDriver(seleniumHost, capabilities);
        driver = suitableFactory.registerListeners(driver, getEventListeners());
        LOGGER.info("Driver session started.");
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
}
