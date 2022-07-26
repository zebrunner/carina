package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.IAbstactCapabilities;

import io.appium.java_client.windows.options.WindowsOptions;

public class WindowsCapabilities extends IAbstactCapabilities<WindowsOptions> {

    @Override
    public WindowsOptions getCapabilities() {
        WindowsOptions options = new WindowsOptions();
        setCapabilitiesSafe(options, getConfigurationCapabilities());
        return options;
    }

    @Override
    public WindowsOptions getCapabilitiesWithCustom(Capabilities capabilities) {
        WindowsOptions options = new WindowsOptions();
        if (capabilities != null) {
            for (String capabilityName : capabilities.getCapabilityNames()) {
                options.amend(capabilityName, capabilities.getCapability(capabilityName));
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
