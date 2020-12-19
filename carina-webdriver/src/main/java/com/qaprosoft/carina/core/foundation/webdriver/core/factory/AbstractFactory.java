/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Base implementation of WebDriver factory.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public abstract class AbstractFactory {
    
    /**
     * Creates new instance of {@link WebDriver} according to specified {@link DesiredCapabilities}.
     * 
     * @param testName - where driver is initiated
     * @param capabilities - driver desired capabilitues
     * @param seleniumHost - selenium server URL
     * @return instance of {@link WebDriver}
     */
    abstract public WebDriver create(String testName, DesiredCapabilities capabilities, String seleniumHost);

    /**
     * If any listeners specified, converts RemoteWebDriver to EventFiringWebDriver and registers all listeners.
     * 
     * @param driver - instance of @link WebDriver}
     * @param listeners - instances of {@link WebDriverEventListener}
     * @return driver with registered listeners
     */
    public WebDriver registerListeners(WebDriver driver, WebDriverEventListener... listeners) {
        if (!ArrayUtils.isEmpty(listeners)) {
            driver = new EventFiringWebDriver(driver);
            for (WebDriverEventListener listener : listeners) {
                ((EventFiringWebDriver) driver).register(listener);
            }
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

}