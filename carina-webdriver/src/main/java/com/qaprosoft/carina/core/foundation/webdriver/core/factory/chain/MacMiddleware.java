package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.Mac2Capabilities;

import io.appium.java_client.mac.Mac2Driver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

public class MacMiddleware extends DriverMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        // presumably you can't rely on the mac platform, because you can test browsers on it
        // if (Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.MAC)) {
        // return true;
        // }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase(AutomationName.MAC2)) {
            return true;
        }
        return false;
    }

    @Override
    public WebDriver getDriver(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = capabilitiesMiddleware.analyze(new Mac2Capabilities().getCapabilities(testName, capabilities));
        LOGGER.debug("Mac2 capabilities: {}", options);
        return new Mac2Driver(getURL(seleniumHost), options);
    }
}
