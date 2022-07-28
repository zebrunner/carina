package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstactCapabilities;

import io.appium.java_client.gecko.options.GeckoOptions;

public class GeckoCapabilities extends AbstactCapabilities<GeckoOptions> {

    @Override
    public GeckoOptions getCapabilities() {
        GeckoOptions options = new GeckoOptions();
        setCapabilitiesSafe(options, getConfigurationCapabilities());
        return options;
    }

    @Override
    public GeckoOptions createCapabilitiesFromCustom(Capabilities customCapabilities) {
        return null;
    }

    @Override
    public GeckoOptions getCapabilitiesWithCustom(Capabilities customCapabilities) {
        return null;
    }

    private void setCapabilitiesSafe(GeckoOptions options, Capabilities capabilities) {
        for (String capabilityName : capabilities.getCapabilityNames()) {
            options.amend(capabilityName, capabilities.getCapability(capabilityName));
        }
    }
}
