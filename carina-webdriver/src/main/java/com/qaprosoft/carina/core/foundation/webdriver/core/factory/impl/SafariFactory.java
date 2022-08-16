package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.SafariCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;

import io.appium.java_client.safari.SafariDriver;

/**
 * Desktop safari browser
 */
public class SafariFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, String seleniumHost) {
        return this.create(testName, seleniumHost, null);
    }

    @Override
    public WebDriver create(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = new SafariCapabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("capabilities: {}", options);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }
        // todo investigate creating driver with EventFiringAppiumCommandExecutor
        // EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(hostURL);
        // WebDriver driver = new SafariDriver(ce, safariOptions);
        WebDriver driver = new SafariDriver(hostURL, options);

        resizeBrowserWindow(driver, options);
        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {

        // for browserstack we do not create safari driver
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if (!customCapabilities.isEmpty() && customCapabilities.toLowerCase().contains("browserstack")) {
            return false;
        }

        if (Configuration.getSeleniumUrl().contains("hub.browserstack.com")) {
            return false;
        }

        if (capabilities.getBrowserName().equalsIgnoreCase(Browser.SAFARI.browserName())) {
            return true;
        }
        return false;
    }
}
