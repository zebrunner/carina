package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.GeckoCapabilities;

import io.appium.java_client.gecko.GeckoDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

@Beta
public class GeckoMiddleware extends DriverMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase(AutomationName.GECKO)) {
            return true;
        }

        return false;
    }

    @Override
    public WebDriver getDriver(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = new GeckoCapabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("Gecko capabilities: {}", options);

        // todo investigate creating driver with EventFiringAppiumCommandExecutor
        return new GeckoDriver(getURL(seleniumHost), options);
    }
}
