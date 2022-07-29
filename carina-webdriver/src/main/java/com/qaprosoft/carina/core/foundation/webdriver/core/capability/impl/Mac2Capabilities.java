package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstactCapabilities;

import io.appium.java_client.mac.options.Mac2Options;

public class Mac2Capabilities extends AbstactCapabilities<Mac2Options> {
    @Override
    public Mac2Options getCapabilities() {
        Mac2Options options = new Mac2Options();
        setCapabilitiesSafe(options, getConfigurationCapabilities());
        return options;
    }

    @Override
    public Mac2Options createCapabilitiesFromCustom(Capabilities customCapabilities) {
        Mac2Options options = new Mac2Options();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.amend(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    @Override
    public Mac2Options getCapabilitiesWithCustom(Capabilities customCapabilities) {
        Mac2Options options = getCapabilities();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.amend(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    private void setCapabilitiesSafe(Mac2Options options, Capabilities capabilities) {
        for (String capabilityName : capabilities.getCapabilityNames()) {
            options.amend(capabilityName, capabilities.getCapability(capabilityName));
        }
    }
}
