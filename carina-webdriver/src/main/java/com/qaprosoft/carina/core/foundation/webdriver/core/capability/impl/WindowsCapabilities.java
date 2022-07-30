package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstactCapabilities;

import io.appium.java_client.windows.options.WindowsOptions;

public class WindowsCapabilities extends AbstactCapabilities<WindowsOptions> {

    @Override
    public WindowsOptions getCapabilities(String testName, Capabilities customCapabilities) {
        WindowsOptions options = new WindowsOptions();
        if (customCapabilities != null) {
            setCapabilities(options, customCapabilities);
            return options;
        }

        setCapabilities(options, getConfigurationCapabilities());
        return options;
    }
}
