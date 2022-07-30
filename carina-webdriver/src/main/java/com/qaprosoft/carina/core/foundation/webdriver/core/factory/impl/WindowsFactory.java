package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.WindowsCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;

import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;
import io.appium.java_client.windows.WindowsDriver;

public class WindowsFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, String seleniumHost) {
        return this.create(testName, seleniumHost, null);
    }

    @Override
    public WebDriver create(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = new WindowsCapabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("capabilities: {}", options);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }

        return new WindowsDriver(hostURL, options);
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {
        // questionable, todo check
        if (Platform.WINDOWS.is(capabilities.getPlatformName())) {
            return true;
        }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase(AutomationName.WINDOWS)) {
            return true;
        }
        return false;
    }
}
