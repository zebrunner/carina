package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.remote.options.SupportsBrowserNameOption;
import io.appium.java_client.safari.options.SafariOptions;

public class SafariCapabilities extends AbstractCapabilities<SafariOptions> {

    @Override
    public SafariOptions getCapabilities(Capabilities capabilities) {
        SafariOptions options = new SafariOptions();
        options = options.amend(SupportsBrowserNameOption.BROWSER_NAME_OPTION, Browser.SAFARI.browserName());
        setCapabilities(options, capabilities);
        options.acceptInsecureCerts();
        return options;
    }
}
