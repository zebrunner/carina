package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.mac.Mac2Driver;
import io.appium.java_client.mac.options.Mac2Options;

public class MacFactory extends AbstractFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        return capabilities instanceof Mac2Options;
    }

    @Override
    public WebDriver getDriver(String seleniumHost, Capabilities capabilities) {
        LOGGER.debug("Mac2 capabilities: {}", capabilities);
        return new Mac2Driver(getURL(seleniumHost), capabilities);
    }
}
