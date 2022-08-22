package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsBrowserNameOption;
import io.appium.java_client.safari.options.SafariOptions;

public class SafariCapabilities extends AbstractCapabilities<SafariOptions> {

    @Override
    public SafariOptions getCapabilities(Capabilities capabilities) {
        SafariOptions options = new SafariOptions();
        setCapabilities(options, capabilities);
        // todo check is it correct
        options = options.amend(SupportsBrowserNameOption.BROWSER_NAME_OPTION, Browser.SAFARI.browserName());
        options.setAutomationName(AutomationName.IOS_XCUI_TEST);
        setCapabilities(options, getConfigurationCapabilities());
        options.acceptInsecureCerts();
        return options;
    }
}
