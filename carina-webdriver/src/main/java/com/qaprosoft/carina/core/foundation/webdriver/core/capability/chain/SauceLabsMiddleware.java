package com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

import io.appium.java_client.internal.CapabilityHelpers;

public class SauceLabsMiddleware extends CapabilitiesMiddleware {
    // todo add special capabilites for chrome, firefox - browserstack custom
    // todo add more and refactor
    List<String> saucelabsSpecificCapabilities = Arrays.asList("build", "name");

    @Override
    protected boolean isDetected(Capabilities capabilities) {
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if ((!customCapabilities.isEmpty() &&
                customCapabilities.toLowerCase().contains("saucelabs")) ||
                Configuration.getSeleniumUrl().contains("saucelabs.com")) {
            return true;
        }
        return false;
    }

    @Override
    protected Capabilities upgradeCapabilities(Capabilities capabilities) {
        HashMap<String, Object> saucelabsOptions = new HashMap<>();
        MutableCapabilities allCapabilities = new MutableCapabilities();
        for (String capabilityName : capabilities.asMap().keySet()) {
            String cleanCapabilityName = StringUtils.removeStart(capabilityName, CapabilityHelpers.APPIUM_PREFIX);
            if (saucelabsSpecificCapabilities.contains(cleanCapabilityName)) {
                saucelabsOptions.put(cleanCapabilityName, capabilities.getCapability(capabilityName));
            } else {
                // browserstack is not understand capabilities with appium prefix
                allCapabilities.setCapability(capabilityName, capabilities.getCapability(capabilityName));
            }
        }

        allCapabilities.setCapability("sauce:options", saucelabsOptions);
        return allCapabilities;
    }
}
