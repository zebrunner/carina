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

public class SauceLabsMiddleware extends CapabilitiesMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // todo add special capabilites for chrome, firefox - browserstack custom
    // todo add more and refactor
    List<String> saucelabsSpecificCapabilities = Arrays.asList("build", "name", "tags", "username", "accessKey",
            "custom-data", "public", "tunnelName", "tunnelIdentifier", "tunnelOwner", "parentTunnel",
            "recordVideo", "videoUploadOnPass", "recordScreenshots", "recordLogs", "maxDuration",
            "commandTimeout", "idleTimeout", "priority", "timeZone",
            "seleniumVersion", "chromedriverVersion", "edgedriverVersion", "geckodriverVersion",
            "iedriverVersion", "avoidProxy", "extendedDebugging", "capturePerformance",
            "screenResolution", "appiumVersion", "deviceType", "deviceOrientation", "orientation",
            "setupDeviceLock", "otherApps", "tabletOnly", "phoneOnly", "privateDevicesOnly", "publicDevicesOnly",
            "carrierConnectivityOnly", "cacheId", "sessionCreationRetry", "sessionCreationTimeout",
            "newCommandTimeout", "noReset", "crosswalkApplication", "autoGrantPermissions",
            "enableAnimations", "resigningEnabled", "sauceLabsImageInjectionEnabled",
            "sauceLabsBypassScreenshotRestriction", "allowTouchIdEnroll",
            "groupFolderRedirectEnabled", "sauceLabsNetworkCaptureEnabled",
            "audioCapture", "systemAlertsDelayEnabled");

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
        LOGGER.debug("Capabilities will be refactored by saucelabs rules");

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
