package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesBuilder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.OptionsType;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.IAbstractFactory;

import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

public class CustomAndroidMobileFactory extends IAbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, Capabilities capabilities, String seleniumHost) {
        Capabilities uiAutomator2Options = CapabilitiesBuilder.builder()
                .withCapabilities(capabilities)
                .chooseOptionsType(OptionsType.ANDROID_UIAUTOMATOR2_APPIUM)
                .build();

        LOGGER.debug("capabilities: {}", uiAutomator2Options);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }

        RemoteWebDriver driver = new RemoteWebDriver(hostURL, uiAutomator2Options);
        registerDevice(driver);
        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {
        // for localhost, browserstack, saucelabs we do not create android driver
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);

        if (Platform.ANDROID.is(capabilities.getPlatformName()) ||
                (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                        capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                                .toString()
                                .equalsIgnoreCase(AutomationName.ANDROID_UIAUTOMATOR2))) {

            if (!customCapabilities.isEmpty() &&
                    (customCapabilities.toLowerCase().contains("localhost") ||
                            customCapabilities.toLowerCase().contains("browserstack") ||
                            customCapabilities.toLowerCase().contains("saucelabs"))) {
                return true;
            }
        }

        return false;
    }
}
