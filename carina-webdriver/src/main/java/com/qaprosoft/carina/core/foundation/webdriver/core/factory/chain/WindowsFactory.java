package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.options.WindowsOptions;

public class WindowsFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        return capabilities instanceof WindowsOptions;
    }

    @Override
    public WebDriver getDriver(String seleniumHost, Capabilities capabilities) {
        LOGGER.debug("Windows capabilities: {}", capabilities);
        return new WindowsDriver(getURL(seleniumHost), capabilities);
    }
}
