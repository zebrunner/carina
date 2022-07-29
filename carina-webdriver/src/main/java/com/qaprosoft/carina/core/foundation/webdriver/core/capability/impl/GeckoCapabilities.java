package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.gecko.options.GeckoOptions;

public class GeckoCapabilities extends AbstractCapabilities<GeckoOptions> {

    @Override
    public GeckoOptions getCapabilities() {
        GeckoOptions options = new GeckoOptions();
        setCapabilitiesSafe(options, getConfigurationCapabilities());
        return options;
    }

    @Override
    public GeckoOptions createCapabilitiesFromCustom(Capabilities customCapabilities) {
        GeckoOptions options = new GeckoOptions();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.amend(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    @Override
    public GeckoOptions getCapabilitiesWithCustom(Capabilities customCapabilities) {
        GeckoOptions options = getCapabilities();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.amend(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    private void setCapabilitiesSafe(GeckoOptions options, Capabilities capabilities) {
        for (String capabilityName : capabilities.getCapabilityNames()) {
            options.amend(capabilityName, capabilities.getCapability(capabilityName));
        }
    }
}
