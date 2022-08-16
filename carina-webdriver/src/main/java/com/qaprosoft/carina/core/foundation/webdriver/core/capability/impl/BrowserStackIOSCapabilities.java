package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class BrowserStackIOSCapabilities extends AbstractCapabilities<MutableCapabilities> {

    @Override
    public MutableCapabilities getCapabilities(String testName, Capabilities customCapabilities) {
        MutableCapabilities capabilities = new XCUITestCapabilities().getCapabilities(testName, customCapabilities);
        capabilities.setCapability("platformName", "ANY");
        return capabilities;
    }
}
