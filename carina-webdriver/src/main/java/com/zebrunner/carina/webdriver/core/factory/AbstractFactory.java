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
package com.zebrunner.carina.webdriver.core.factory;

import java.lang.invoke.MethodHandles;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.openqa.selenium.support.events.WebDriverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.utils.R;

import io.appium.java_client.internal.CapabilityHelpers;

/**
 * Base implementation of WebDriver factory.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public abstract class AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Creates new instance of {@link WebDriver} according to specified {@link MutableCapabilities}.
     * 
     * @param testName - where driver is initiated
     * @param capabilities - driver capabilities
     * @param seleniumHost - selenium server URL
     * @return instance of {@link WebDriver}
     */
    public abstract WebDriver create(String testName, MutableCapabilities capabilities, String seleniumHost);

    /**
     * If any listeners specified, converts RemoteWebDriver to EventFiringDecorator and registers all listeners.
     * 
     * @param driver - instance of {@link WebDriver}
     * @param listeners - instances of {@link WebDriverListener}
     * @return driver with registered listeners
     */
    public WebDriver registerListeners(WebDriver driver, WebDriverListener... listeners) {
        if (!ArrayUtils.isEmpty(listeners)) {
            driver = new EventFiringDecorator<>(listeners).decorate(driver);
        }
        return driver;
    }

    /**
     * Checks driver capabilities for being not empty.
     * 
     * @param capabilities - driver capabilities
     * @return if capabilities empty or null
     */
    protected boolean isCapabilitiesEmpty(Capabilities capabilities) {
        return capabilities == null || MapUtils.isEmpty(capabilities.asMap());
    }

    protected boolean isEnabled(String capability) {
        return R.CONFIG.getBoolean(capability);
    }

    protected MutableCapabilities removeAppiumPrefix(MutableCapabilities capabilities) {
        MutableCapabilities allCapabilities = new MutableCapabilities();
        for (String capabilityName : capabilities.asMap().keySet()) {
            String cleanCapabilityName = StringUtils.removeStart(capabilityName, CapabilityHelpers.APPIUM_PREFIX);
            allCapabilities.setCapability(cleanCapabilityName, capabilities.getCapability(capabilityName));
        }
        return allCapabilities;
    }
}
