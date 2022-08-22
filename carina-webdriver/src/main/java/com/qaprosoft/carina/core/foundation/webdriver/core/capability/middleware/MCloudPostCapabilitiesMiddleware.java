package com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.internal.CapabilityHelpers;

public class MCloudPostCapabilitiesMiddleware extends CapabilitiesMiddleware {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isDetected(Capabilities capabilities) {
        return CapabilitiesUtils.isMCloudDetected();
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        MutableCapabilities mcloudCapabilities = new MutableCapabilities();
        for (String capabilityName : capabilities.asMap().keySet()) {
            String cleanCapabilityName = StringUtils.removeStart(capabilityName, CapabilityHelpers.APPIUM_PREFIX);
            // mcoud is not understand capabilities with appium prefix
            mcloudCapabilities.setCapability(cleanCapabilityName, capabilities.getCapability(capabilityName));
        }
        return mcloudCapabilities;
    }
}
