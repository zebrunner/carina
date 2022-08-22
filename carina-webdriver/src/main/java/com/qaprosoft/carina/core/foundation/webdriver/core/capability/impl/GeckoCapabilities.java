package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.gecko.options.GeckoOptions;

public class GeckoCapabilities extends AbstractCapabilities<GeckoOptions> {

    @Override
    public GeckoOptions getCapabilities(Capabilities capabilities) {
        GeckoOptions options = new GeckoOptions();
        setCapabilities(options, capabilities);
        return options;
    }

}
