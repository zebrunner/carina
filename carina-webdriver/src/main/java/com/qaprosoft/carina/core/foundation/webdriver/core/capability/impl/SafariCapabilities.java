package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstactCapabilities;

import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.options.SupportsBrowserNameOption;
import io.appium.java_client.safari.options.SafariOptions;

public class SafariCapabilities extends AbstactCapabilities<SafariOptions> {

    @Override
    public SafariOptions getCapabilities(String testName, Capabilities customCapabilities) {
        SafariOptions options = new SafariOptions();
        if (customCapabilities != null) {
            setCapabilities(options, customCapabilities);
            return options;
        }
        options = options.amend(SupportsBrowserNameOption.BROWSER_NAME_OPTION, Browser.SAFARI.browserName());
        options.setAutomationName(AutomationName.IOS_XCUI_TEST);
        setCapabilities(options, getConfigurationCapabilities());
        options.acceptInsecureCerts();
        return options;
    }
}
