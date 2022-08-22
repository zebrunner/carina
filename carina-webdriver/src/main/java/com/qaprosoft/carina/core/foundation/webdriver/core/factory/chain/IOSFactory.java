package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware.CapabilitiesUtils;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringAppiumCommandExecutor;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

public class IOSFactory extends AbstractFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        return capabilities instanceof XCUITestOptions;
    }

    @Override
    public WebDriver getDriver(String seleniumHost, Capabilities capabilities) {
        LOGGER.debug("IOS capabilities: {}", capabilities);

        RemoteWebDriver driver = null;
        URL hostURL = getURL(seleniumHost);

        if (CapabilitiesUtils.isBrowserStackSpecificCapabilitiesDetected(capabilities) ||
                CapabilitiesUtils.isSauceLabsSpecificCapabilitiesDetected(capabilities)) {
            LOGGER.info("Custom driver will be used!");

            // options.setPlatformName("ANY"); // Browserstack is not understand platform name IOS
            driver = new RemoteWebDriver(hostURL, capabilities);

        } else {
            EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(hostURL);
            driver = new IOSDriver(ce, capabilities);

        }
        registerDevice(driver);
        return driver;
    }
}
