package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesBuilder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.OptionsType;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.IAbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringAppiumCommandExecutor;

import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;
import io.appium.java_client.safari.SafariDriver;

/**
 * Desktop safari browser
 */
public class SafariFactory extends IAbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, Capabilities capabilities, String seleniumHost) {
        Capabilities safariOptions = CapabilitiesBuilder.builder()
                .withCapabilities(capabilities)
                .chooseOptionsType(OptionsType.SAFARI_APPIUM)
                .build();

        LOGGER.debug("capabilities: {}", safariOptions);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }

        EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(hostURL);
        WebDriver driver = new SafariDriver(ce, safariOptions);
        resizeBrowserWindow(driver, safariOptions);
        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {
        if (capabilities.getBrowserName().equalsIgnoreCase(Browser.SAFARI.browserName())) {
            return true;
        }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION).toString()
                        .equalsIgnoreCase(AutomationName.SAFARI)) {
            return true;
        }

        return false;
    }
}
