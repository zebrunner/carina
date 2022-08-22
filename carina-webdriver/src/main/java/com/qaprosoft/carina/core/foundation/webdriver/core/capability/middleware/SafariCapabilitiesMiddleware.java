package com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.SafariCapabilities;

import io.appium.java_client.safari.options.SafariOptions;

public class SafariCapabilitiesMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {

        if (capabilities instanceof SafariOptions) {
            return false;
        }

        if (Browser.SAFARI.browserName().equalsIgnoreCase(capabilities.getBrowserName())) {
            return true;
        }
        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        return new SafariCapabilities().getCapabilities(capabilities);
    }
}
