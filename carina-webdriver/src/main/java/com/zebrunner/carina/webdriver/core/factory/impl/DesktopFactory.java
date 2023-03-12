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
package com.zebrunner.carina.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;
import com.zebrunner.carina.webdriver.core.capability.impl.desktop.ChromeCapabilities;
import com.zebrunner.carina.webdriver.core.capability.impl.desktop.EdgeCapabilities;
import com.zebrunner.carina.webdriver.core.capability.impl.desktop.FirefoxCapabilities;
import com.zebrunner.carina.webdriver.core.capability.impl.desktop.OperaCapabilities;
import com.zebrunner.carina.webdriver.core.capability.impl.desktop.SafariCapabilities;
import com.zebrunner.carina.webdriver.core.factory.AbstractFactory;
import com.zebrunner.carina.webdriver.listener.EventFiringSeleniumCommandExecutor;

public class DesktopFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static MutableCapabilities staticCapabilities = null;

    @Override
    public WebDriver create(String name, MutableCapabilities capabilities, String seleniumHost) {
        WebDriver driver = null;
        if (seleniumHost == null) {
            seleniumHost = Configuration.getSeleniumUrl();
        }

        if (isCapabilitiesEmpty(capabilities)) {
            capabilities = getCapabilities(name);
        }

        if (staticCapabilities != null) {
            LOGGER.info("Static MutableCapabilities will be merged to basic driver capabilities");
            capabilities.merge(staticCapabilities);
        }

        if (Browser.SAFARI.browserName().equalsIgnoreCase(capabilities.getBrowserName()) &&
                "false".equalsIgnoreCase(Configuration.get(Parameter.W3C))) {
            capabilities = removeAppiumPrefix(capabilities);
        }

        LOGGER.debug("capabilities: {}", capabilities);

        try {
            EventFiringSeleniumCommandExecutor ce = new EventFiringSeleniumCommandExecutor(new URL(seleniumHost));
            driver = new RemoteWebDriver(ce, capabilities);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }
        resizeBrowserWindow(driver, capabilities);

        return driver;
    }

    @SuppressWarnings("deprecation")
    public MutableCapabilities getCapabilities(String name) {
        String browser = Configuration.getBrowser();
        
        if (Browser.FIREFOX.browserName().equalsIgnoreCase(browser)) {
            return new FirefoxCapabilities().getCapability(name);
        } else if (Browser.SAFARI.browserName().equalsIgnoreCase(browser)) {
            return new SafariCapabilities().getCapability(name);
        } else if (Browser.CHROME.browserName().equalsIgnoreCase(browser)) {
            return new ChromeCapabilities().getCapability(name);
        } else if (Browser.OPERA.browserName().equalsIgnoreCase(browser)) {
            return new OperaCapabilities().getCapability(name);
        } else if (Browser.EDGE.browserName().equalsIgnoreCase(browser) || "edge".equalsIgnoreCase(browser)) {
            return new EdgeCapabilities().getCapability(name);
        } else {
            throw new RuntimeException("Unsupported browser: " + browser);
        }
    }

    public static void addStaticCapability(String name, Object value) {
        if (staticCapabilities == null) {
            staticCapabilities = new MutableCapabilities();
        }
        staticCapabilities.setCapability(name, value);
    }

    /**
     * Sets browser window according to capabilites.resolution value, otherwise
     * maximizes window.
     * 
     * @param driver - instance of desktop @WebDriver
     * @param capabilities - driver capabilities
     */
    private void resizeBrowserWindow(WebDriver driver, MutableCapabilities capabilities) {
        try {
            Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                    .pollingEvery(Duration.ofMillis(Configuration.getInt(Parameter.RETRY_INTERVAL)))
                    .withTimeout(Duration.ofSeconds(Configuration.getInt(Parameter.EXPLICIT_TIMEOUT)))
                    .ignoring(WebDriverException.class)
                    .ignoring(NoSuchSessionException.class)
                    .ignoring(TimeoutException.class);
            if (capabilities.getCapability("resolution") != null) {
                String resolution = (String) capabilities.getCapability("resolution");
                int expectedWidth = Integer.parseInt(resolution.split("x")[0]);
                int expectedHeight = Integer.parseInt(resolution.split("x")[1]);
                wait.until(new Function<WebDriver, Boolean>(){
                    public Boolean apply(WebDriver driver ) {
                        driver.manage().window().setPosition(new Point(0, 0));
                        driver.manage().window().setSize(new Dimension(expectedWidth, expectedHeight));
                        Dimension actualSize = driver.manage().window().getSize();
                        if (actualSize.getWidth() == expectedWidth && actualSize.getHeight() == expectedHeight) {
                            LOGGER.debug(String.format("Browser window size set to %dx%d", actualSize.getWidth(), actualSize.getHeight()));
                        } else {
                            LOGGER.warn(String.format("Expected browser window %dx%d, but actual %dx%d",
                                    expectedWidth, expectedHeight, actualSize.getWidth(), actualSize.getHeight()));
                        }
                        return true;
                    }
                });
            } else {
                wait.until(new Function<WebDriver, Boolean>(){
                    public Boolean apply(WebDriver driver ) {
                        driver.manage().window().maximize();
                        LOGGER.debug("Browser window size was maximized!");
                        return true;
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("Unable to resize browser window", e);
        }
    }
}
