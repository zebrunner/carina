package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.WindowsCapabilities;

import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;
import io.appium.java_client.windows.WindowsDriver;

public class WindowsMiddleware extends Middleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        // presumably you can't rely on the windows platform, because you can test browsers on it
        // if (Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.WINDOWS)) {
        // return true;
        // }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase(AutomationName.WINDOWS)) {
            return true;
        }
        return false;
    }

    @Override
    protected WebDriver getDriverByRule(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = capabilitiesMiddleware.analyze(new WindowsCapabilities().getCapabilities(testName, capabilities));
        LOGGER.debug("Windows capabilities: {}", options);
        return new WindowsDriver(getURL(seleniumHost), options);
    }
}
