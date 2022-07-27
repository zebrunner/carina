package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstactCapabilities;

import io.appium.java_client.remote.options.SupportsBrowserNameOption;
import io.appium.java_client.safari.options.SafariOptions;

public class SafariCapabilities extends AbstactCapabilities<SafariOptions> {

    @Override
    public SafariOptions getCapabilities() {
        SafariOptions options = new SafariOptions();
        options = options.amend(SupportsBrowserNameOption.BROWSER_NAME_OPTION, Browser.SAFARI.browserName());
        setCapabilitiesSafe(options, getConfigurationCapabilities());
        options.acceptInsecureCerts();
        return options;
    }

    @Override
    public SafariOptions createCapabilitiesFromCustom(Capabilities customCapabilities) {
        SafariOptions options = new SafariOptions();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.amend(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    @Override
    public SafariOptions getCapabilitiesWithCustom(Capabilities customCapabilities) {
        SafariOptions options = getCapabilities();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.amend(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    private void setCapabilitiesSafe(SafariOptions options, Capabilities capabilities) {
        for (String capabilityName : capabilities.getCapabilityNames()) {
            options.amend(capabilityName, capabilities.getCapability(capabilityName));
        }
    }
}
