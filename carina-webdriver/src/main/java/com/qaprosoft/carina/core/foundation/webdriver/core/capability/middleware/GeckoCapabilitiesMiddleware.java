package com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.GeckoCapabilities;

import io.appium.java_client.gecko.options.GeckoOptions;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

public class GeckoCapabilitiesMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {

        // it is not nesessary to upgrade capabilities if it is instance of GeckoOptions
        if (capabilities instanceof GeckoOptions) {
            return false;
        }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase(AutomationName.GECKO)) {
            return true;
        }

        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        return new GeckoCapabilities().getCapabilities(capabilities);
    }
}
