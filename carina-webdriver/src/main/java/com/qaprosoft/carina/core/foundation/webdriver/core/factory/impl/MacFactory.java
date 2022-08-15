package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.Mac2Capabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;

import io.appium.java_client.mac.Mac2Driver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

public class MacFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, String seleniumHost) {
        return this.create(testName, seleniumHost, null);
    }

    @Override
    public WebDriver create(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = new Mac2Capabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("capabilities: {}", options);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }

        return new Mac2Driver(hostURL, options);
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {

        if (Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.MAC)) {
            return true;
        }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase(AutomationName.MAC2)) {
            return true;
        }
        return false;
    }
}
