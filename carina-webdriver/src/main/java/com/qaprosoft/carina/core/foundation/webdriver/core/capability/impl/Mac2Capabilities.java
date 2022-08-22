package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.mac.options.Mac2Options;

public class Mac2Capabilities extends AbstractCapabilities<Mac2Options> {
    @Override
    public Mac2Options getCapabilities(Capabilities capabilities) {
        Mac2Options options = new Mac2Options();
        setCapabilities(options, capabilities);
        return options;
    }
}
