package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstactCapabilities;

import io.appium.java_client.gecko.options.GeckoOptions;

public class GeckoCapabilities extends AbstactCapabilities<GeckoOptions> {

    @Override
    public GeckoOptions getCapabilities(String testName, Capabilities customCapabilities) {
        GeckoOptions options = new GeckoOptions();

        if (customCapabilities != null) {
            setCapabilities(options, customCapabilities);
            return options;
        }

        setCapabilities(options, getConfigurationCapabilities());
        return options;
    }

}
