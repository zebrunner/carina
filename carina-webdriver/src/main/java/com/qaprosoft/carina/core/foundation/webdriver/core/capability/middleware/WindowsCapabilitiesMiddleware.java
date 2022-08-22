package com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.WindowsCapabilities;

import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;
import io.appium.java_client.windows.options.WindowsOptions;

public class WindowsCapabilitiesMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {

        if (capabilities instanceof WindowsOptions) {
            return false;
        }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase(AutomationName.WINDOWS)) {
            return true;
        }
        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        return new WindowsCapabilities().getCapabilities(capabilities);
    }
}
