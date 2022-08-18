package com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

import io.appium.java_client.internal.CapabilityHelpers;

public class LambdatestMiddleware extends CapabilitiesMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // todo add more
    private static final List<String> lambdatestSpecificCapabilities = Arrays.asList("w3c", "username", "accessKey", "visual",
            "video", "build", "project", "name", "tunnel", "selenium_version");

    @Override
    protected boolean isDetected(Capabilities capabilities) {
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if (!customCapabilities.isEmpty() && customCapabilities.toLowerCase().contains("lambdatest")) {
            return true;
        }
        return false;
    }

    @Override
    protected Capabilities upgradeCapabilities(Capabilities capabilities) {
        LOGGER.debug("Capabilities will be refactored by lambdatest rules");

        HashMap<String, Object> lambdatestOptions = new HashMap<>();
        MutableCapabilities allCapabilities = new MutableCapabilities();

        for (String capabilityName : capabilities.asMap().keySet()) {
            String cleanCapabilityName = StringUtils.removeStart(capabilityName, CapabilityHelpers.APPIUM_PREFIX);
            if (lambdatestSpecificCapabilities.contains(cleanCapabilityName)) {
                lambdatestOptions.put(cleanCapabilityName, capabilities.getCapability(capabilityName));
            } else {
                allCapabilities.setCapability(cleanCapabilityName, capabilities.getCapability(capabilityName));
            }
        }

        if (!lambdatestOptions.isEmpty()) {
            allCapabilities.setCapability("LT:Options", lambdatestOptions);
        }

        return allCapabilities;
    }
}
