package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.windows.options.WindowsOptions;

public class WindowsCapabilities extends AbstractCapabilities<WindowsOptions> {

    @Override
    public WindowsOptions getCapabilities() {
        WindowsOptions options = new WindowsOptions();
        setCapabilitiesSafe(options, getConfigurationCapabilities());
        return options;
    }

    @Override
    public WindowsOptions createCapabilitiesFromCustom(Capabilities customCapabilities) {
        WindowsOptions options = new WindowsOptions();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.amend(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    @Override
    public WindowsOptions getCapabilitiesWithCustom(Capabilities customCapabilities) {
        WindowsOptions options = getCapabilities();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.amend(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    private void setCapabilitiesSafe(WindowsOptions options, Capabilities capabilities) {
        for (String capabilityName : capabilities.getCapabilityNames()) {
            options.amend(capabilityName, capabilities.getCapability(capabilityName));
        }
    }
}
