package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.IAbstactCapabilities;

import io.appium.java_client.remote.options.SupportsBrowserNameOption;
import io.appium.java_client.safari.options.SafariOptions;

public class SafariCapabilities extends IAbstactCapabilities<SafariOptions> {

    @Override
    public SafariOptions getCapabilities() {
        SafariOptions options = new SafariOptions();
        options = options.amend(SupportsBrowserNameOption.BROWSER_NAME_OPTION, Browser.SAFARI.browserName());
        return options;
    }

    @Override
    public SafariOptions getCapabilitiesWithCustom(Capabilities capabilities) {
        SafariOptions options = getCapabilities();
        if (capabilities != null) {
            for (String capabilityName : capabilities.getCapabilityNames()) {
                options.amend(capabilityName, capabilities.getCapability(capabilityName));
            }
        }
        return options;
    }
}
