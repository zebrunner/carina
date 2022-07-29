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
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;

import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

/**
 * CustomIOSMobileFactory creates instance {@link RemoteWebDriver} for IOS testing on browserstack and saucelabs
 */
public class CustomIOSMobileFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, String hostURL) {
        return this.create(testName, hostURL, null);
    }

    @Override
    public WebDriver create(String testName, String seleniumHost, Capabilities capabilities) {
        CapabilitiesBuilder capabilitiesBuilder = CapabilitiesBuilder.builder();
        if (capabilities != null) {
            capabilitiesBuilder.fromCustomCapabilities(capabilities);
        }
        capabilitiesBuilder.chooseOptionsType(OptionsType.IOS);
        Capabilities xcuiOptions = capabilitiesBuilder.build();

        LOGGER.debug("capabilities: {}", xcuiOptions);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }

        RemoteWebDriver driver = new RemoteWebDriver(hostURL, xcuiOptions);
        registerDevice(driver);
        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {
        // for localhost, browserstack, saucelabs we do not create android driver
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);

        // todo add rule for browser, if this type of driver is not used for browser testing

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
        }

        return false;
    }
}
