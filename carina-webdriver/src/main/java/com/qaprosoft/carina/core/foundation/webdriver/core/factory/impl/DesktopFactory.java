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
package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.FirefoxCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.IECapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.SafariCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public class DesktopFactory extends AbstractFactory {

    private static DesiredCapabilities staticCapabilities;
    private static final String RESTART_ALL_BAT_PATH = "C:\\Tools\\selenium-server\\restart-all.bat";
    private static final String RESTART_ALL_SH_PATH = "$HOME/tools/selenium/restart-all.sh";
    private static final String PREFIX_WIN = "cmd /c ";
    private static final String PREFIX_NIX = "/bin/bash -c ";

    @Override
    public WebDriver create(String name, Device device, DesiredCapabilities capabilities, String seleniumHost) {
        RemoteWebDriver driver = null;
        if (seleniumHost == null) {
            seleniumHost = Configuration.get(Configuration.Parameter.SELENIUM_HOST);
        }

        if (isCapabilitiesEmpty(capabilities)) {
            capabilities = getCapabilities(name);
        }

        if (staticCapabilities != null) {
            LOGGER.info("Static DesiredCapabilities will be merged to basic driver capabilities");
            capabilities.merge(staticCapabilities);
        }

        try {
            driver = new RemoteWebDriver(new URL(seleniumHost), capabilities);
        } catch (UnreachableBrowserException e) {
            // try to restart selenium hub
            restartAll(PREFIX_WIN, RESTART_ALL_BAT_PATH);
            restartAll(PREFIX_NIX, RESTART_ALL_SH_PATH);
            throw e;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to create desktop driver");
        }

        return driver;
    }

    public DesiredCapabilities getCapabilities(String name) {
        String browser = Configuration.get(Parameter.BROWSER);

        if (BrowserType.FIREFOX.equalsIgnoreCase(browser)) {
            return new FirefoxCapabilities().getCapability(name);
        } else if (BrowserType.IEXPLORE.equalsIgnoreCase(browser) || BrowserType.IE.equalsIgnoreCase(browser) || browser.equalsIgnoreCase("ie")) {
            return new IECapabilities().getCapability(name);
        } else if (BrowserType.SAFARI.equalsIgnoreCase(browser)) {
            return new SafariCapabilities().getCapability(name);
        } else if (BrowserType.CHROME.equalsIgnoreCase(browser)) {
            return new ChromeCapabilities().getCapability(name);
        } else {
            throw new RuntimeException("Unsupported browser: " + browser);
        }
    }

    public static void addStaticCapability(String name, Object value) {
        if (staticCapabilities == null) {
            staticCapabilities = new DesiredCapabilities();
        }
        staticCapabilities.setCapability(name, value);
    }

    private void restartAll(String cmdPrefix, String filePath) {
        if (new File(filePath).exists()) {
            LOGGER.info("Following command will be executed: " + cmdPrefix + filePath);
            try {
                Runtime.getRuntime().exec(cmdPrefix + filePath);
            } catch (IOException e) {
                throw new RuntimeException("Cannot restart selenium server");
            }
        }
    }
}
