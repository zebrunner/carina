package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringSeleniumCommandExecutor;

import io.appium.java_client.remote.options.SupportsAutomationNameOption;

/**
 * Desktop chrome browser
 */
public class ChromeFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, String seleniumHost) {
        return this.create(testName, seleniumHost, null);
    }

    @Override
    public WebDriver create(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = new ChromeCapabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("Chrome capabilities: {}", options);
        EventFiringSeleniumCommandExecutor ce = new EventFiringSeleniumCommandExecutor(getURL(seleniumHost));
        WebDriver driver = new RemoteWebDriver(ce, options);
        resizeBrowserWindow(driver, options);

        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {
        if (capabilities.getPlatformName() != null && (capabilities.getPlatformName().is(Platform.IOS) ||
                capabilities.getPlatformName().is(Platform.ANDROID))) {
            return false;
        }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null) {
            return false;
        }

        if (capabilities.getBrowserName().equalsIgnoreCase(Browser.CHROME.browserName())) {
            return true;
        }

        return false;

    }
}
