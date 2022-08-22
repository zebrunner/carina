package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringSeleniumCommandExecutor;

import io.appium.java_client.android.options.UiAutomator2Options;

public class ChromeFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        return capabilities instanceof UiAutomator2Options;
    }

    @Override
    public WebDriver getDriver(String seleniumHost, Capabilities capabilities) {
        LOGGER.debug("Chrome capabilities: {}", capabilities);
        EventFiringSeleniumCommandExecutor ce = new EventFiringSeleniumCommandExecutor(getURL(seleniumHost));
        WebDriver driver = new RemoteWebDriver(ce, capabilities);
        resizeBrowserWindow(driver, capabilities);
        return driver;
    }
}
