package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesBuilder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.OptionsType;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;

import io.appium.java_client.gecko.GeckoDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

// todo check is it work
public class GeckoFactory extends AbstractFactory {
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
        capabilitiesBuilder.chooseOptionsType(OptionsType.GECKO);
        Capabilities geckoOptions = capabilitiesBuilder.build();

        LOGGER.debug("capabilities: {}", geckoOptions);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }

        // todo investigate creating driver with EventFiringAppiumCommandExecutor
        // EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(hostURL);
        // return new GeckoDriver(ce, geckoOptions);

        return new GeckoDriver(hostURL, geckoOptions);
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase(AutomationName.GECKO)) {
            return true;
        }

        return false;
    }
}
