package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.SafariCapabilities;

import io.appium.java_client.safari.SafariDriver;
import io.appium.java_client.safari.options.SafariOptions;

public class SafariMiddleware extends Middleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {

        // // for browserstack we do not create safari driver
        // String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        // if (!customCapabilities.isEmpty() && customCapabilities.toLowerCase().contains("browserstack")) {
        // return false;
        // }

        // if (Configuration.getSeleniumUrl().contains("hub.browserstack.com")) {
        // return false;
        // }

        if (capabilities.getBrowserName().equalsIgnoreCase(Browser.SAFARI.browserName())) {
            return true;
        }
        return false;
    }

    @Override
    protected WebDriver getDriverByRule(String testName, String seleniumHost, Capabilities capabilities) {
        SafariOptions options = new SafariCapabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("Safari capabilities: {}", options);
        WebDriver driver = new SafariDriver(getURL(seleniumHost), options);
        resizeBrowserWindow(driver, options);
        return driver;
    }
}
