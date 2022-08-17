package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.UIAutomator2Capabilities;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringAppiumCommandExecutor;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.MobilePlatform;

public class AndroidMiddleware extends Middleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.MOBILE)) {
            return false;
        }

        // // for localhost, browserstack, saucelabs we do not create android driver
        // String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        // if (!customCapabilities.isEmpty()
        // && (customCapabilities.toLowerCase().contains("localhost") ||
        // // customCapabilities.toLowerCase().contains("browserstack") ||
        // customCapabilities.toLowerCase().contains("saucelabs"))) {
        // return false;
        // }

        // if (Configuration.getSeleniumUrl().contains("hub.browserstack.com")) {
        // // #1786 mobile drivers on browserstack should be started via CUSTOM - RemoteWebDriver driver
        // return false;
        // }

        if (capabilities.getCapability("os") != null &&
                MobilePlatform.ANDROID.equalsIgnoreCase(capabilities.getCapability("os").toString())) {
            return true;
        }

        if (Platform.ANDROID.is(capabilities.getPlatformName())) {
            return true;
        }

        return false;
    }

    @Override
    protected WebDriver getDriverByRule(String testName, String seleniumHost, Capabilities capabilities) {
        UiAutomator2Options options = new UIAutomator2Capabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("Android capabilities: {}", options);

        RemoteWebDriver driver = null;
        URL hostURL = getURL(seleniumHost);

        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if ((!customCapabilities.isEmpty() &&
                customCapabilities.toLowerCase().contains("browserstack")) ||
                Configuration.getSeleniumUrl().contains("hub.browserstack.com") ||
                Configuration.getSeleniumUrl().contains("hub-cloud.browserstack.com")) {
            LOGGER.info("Browserstack was detected! RemoteWebDriver will be used instead of AndroidDriver");

            options.setPlatformName("ANY"); // Browserstack is not understand platform name IOS
            driver = new RemoteWebDriver(hostURL, options);

        } else {
            EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(hostURL);
            driver = new AndroidDriver(ce, options);
        }
        registerDevice(driver);
        return driver;
    }
}
