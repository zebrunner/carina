package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
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
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.XCUITestCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;

import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

public class CustomIOSMobileFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, String seleniumHost) {
        return this.create(testName, seleniumHost, null);
    }

    @Override
    public WebDriver create(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = new XCUITestCapabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("capabilities: {}", options);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }

        RemoteWebDriver driver = new RemoteWebDriver(hostURL, options);
        registerDevice(driver);
        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.MOBILE)) {
            return false;
        }

        // for localhost, browserstack, saucelabs we do not create android driver
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);

        if (Platform.IOS.is(capabilities.getPlatformName()) ||
                (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                        capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                                .toString()
                                .equalsIgnoreCase(AutomationName.IOS_XCUI_TEST))) {

            if (!customCapabilities.isEmpty() &&
                    (customCapabilities.toLowerCase().contains("localhost") ||
                            customCapabilities.toLowerCase().contains("browserstack") ||
                            customCapabilities.toLowerCase().contains("saucelabs"))) {
                return true;
            }
            if (Configuration.getSeleniumUrl().contains("hub.browserstack.com")) {
                // #1786 mobile drivers on browserstack should be started via CUSTOM - RemoteWebDriver driver
                return true;
            }
        }

        return false;
    }
}
