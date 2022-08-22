package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.safari.SafariDriver;
import io.appium.java_client.safari.options.SafariOptions;

public class SafariFactory extends AbstractFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        return capabilities instanceof SafariOptions;
    }

    @Override
    public WebDriver getDriver(String seleniumHost, Capabilities capabilities) {
        LOGGER.debug("Safari capabilities: {}", capabilities);
        WebDriver driver = new SafariDriver(getURL(seleniumHost), capabilities);
        resizeBrowserWindow(driver, capabilities);
        return driver;
    }
}
