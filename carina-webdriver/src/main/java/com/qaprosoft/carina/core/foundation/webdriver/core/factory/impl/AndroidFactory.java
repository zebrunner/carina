package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.UIAutomator2Capabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringAppiumCommandExecutor;

import io.appium.java_client.android.AndroidDriver;

/**
 * For native android testing and testing browser on android
 */
public class AndroidFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, String seleniumHost) {
        return this.create(testName, seleniumHost, null);
    }

    @Override
    public WebDriver create(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = new UIAutomator2Capabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("Android capabilities: {}", options);
        EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(getURL(seleniumHost));
        AndroidDriver driver = new AndroidDriver(ce, options);
        registerDevice(driver);
        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities and selenium url
     */
    public static boolean isSuitable(Capabilities capabilities) {

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.MOBILE)) {
            return false;
        }

        // for localhost, browserstack, saucelabs we do not create android driver
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if (!customCapabilities.isEmpty()
                && (customCapabilities.toLowerCase().contains("localhost") ||
                        customCapabilities.toLowerCase().contains("browserstack") ||
                        customCapabilities.toLowerCase().contains("saucelabs"))) {
            return false;
        }

        if (Configuration.getSeleniumUrl().contains("hub.browserstack.com")) {
            // #1786 mobile drivers on browserstack should be started via CUSTOM - RemoteWebDriver driver
            return false;
        }

        if (Platform.ANDROID.is(capabilities.getPlatformName())) {
            return true;
        }

        return false;
    }

    private EventFiringAppiumCommandExecutor getCommandExecutor(String seleniumHost) {
        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("selenium host is invalid", e);
        }

        return new EventFiringAppiumCommandExecutor(hostURL);
    }
}
