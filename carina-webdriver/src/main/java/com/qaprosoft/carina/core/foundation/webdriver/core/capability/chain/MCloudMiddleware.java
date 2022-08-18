package com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

import io.appium.java_client.internal.CapabilityHelpers;

public class MCloudMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if ((!customCapabilities.isEmpty() &&
                customCapabilities.toLowerCase().contains("mcloud"))) {
            return true;
        }
        return false;
    }

    @Override
    protected Capabilities upgradeCapabilities(Capabilities capabilities) {
        MutableCapabilities mcloudCapabilities = new MutableCapabilities();
        for (String capabilityName : capabilities.asMap().keySet()) {
            String cleanCapabilityName = StringUtils.removeStart(capabilityName, CapabilityHelpers.APPIUM_PREFIX);
            // mcoud is not understand capabilities with appium prefix
            mcloudCapabilities.setCapability(cleanCapabilityName, capabilities.getCapability(capabilityName));
        }
        return mcloudCapabilities;
    }
}