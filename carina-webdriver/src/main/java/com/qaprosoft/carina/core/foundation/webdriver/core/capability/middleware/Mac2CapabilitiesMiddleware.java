package com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.Mac2Capabilities;

import io.appium.java_client.mac.options.Mac2Options;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

public class Mac2CapabilitiesMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {

        if (capabilities instanceof Mac2Options) {
            return false;
        }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase(AutomationName.MAC2)) {
            return true;
        }
        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        return new Mac2Capabilities().getCapabilities(capabilities);
    }
}
