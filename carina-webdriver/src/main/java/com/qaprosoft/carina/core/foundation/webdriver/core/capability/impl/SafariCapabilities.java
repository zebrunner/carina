package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.IAbstactCapabilities;

import io.appium.java_client.safari.options.SafariOptions;

public class SafariCapabilities extends IAbstactCapabilities<SafariOptions> {

    @Override
    public SafariOptions getCapabilities() {
        SafariOptions options = new SafariOptions();
        return options;
    }

    @Override
    public SafariOptions getCapabilitiesWithCustom(Capabilities capabilities) {
        SafariOptions options = new SafariOptions();
        if (capabilities != null) {
            for (String capabilityName : capabilities.getCapabilityNames()) {
                options.amend(capabilityName, capabilities.getCapability(capabilityName));
            }
        }
        return options;
    }
}
