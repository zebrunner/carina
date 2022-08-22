package com.qaprosoft.carina.core.foundation.webdriver.core.capability.capabilchain;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.internal.CapabilityHelpers;

public class BrowserstackPostMiddleware extends CapabilitiesMiddleware {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isDetected(Capabilities capabilities) {
        // If we already have bstack:options, it is not necessary to pre-upgrade options
        return CapabilitiesUtils.isBrowserStackSpecificCapabilitiesDetected(capabilities);
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        // todo it seems that browserstack is not understand capabilities with appium prefix, investigate
        DesiredCapabilities allCapabilities = new DesiredCapabilities();
        for (String capabilityName : capabilities.asMap().keySet()) {
            String cleanCapabilityName = StringUtils.removeStart(capabilityName, CapabilityHelpers.APPIUM_PREFIX);
            allCapabilities.setCapability(cleanCapabilityName, capabilities.getCapability(capabilityName));
        }
        return allCapabilities;
    }
}
