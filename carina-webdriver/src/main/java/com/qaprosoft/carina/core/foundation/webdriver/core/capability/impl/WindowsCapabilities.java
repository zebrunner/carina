package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.windows.options.WindowsOptions;

public class WindowsCapabilities extends AbstractCapabilities<WindowsOptions> {

    @Override
    public WindowsOptions getCapabilities(Capabilities capabilities) {
        WindowsOptions options = new WindowsOptions();
        setCapabilities(options, capabilities);
        return options;
    }
}
