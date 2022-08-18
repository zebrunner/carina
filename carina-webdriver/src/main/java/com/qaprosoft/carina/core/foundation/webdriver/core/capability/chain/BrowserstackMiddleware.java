package com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

import io.appium.java_client.internal.CapabilityHelpers;
import io.appium.java_client.remote.MobilePlatform;

public class BrowserstackMiddleware extends CapabilitiesMiddleware {
    // todo add special capabilites for chrome, firefox - browserstack custom
    // todo add more and refactor
    List<String> browserstackSpecificCapabilities = Arrays.asList("userName", "accessKey", "appiumVersion", "projectName", "buildName",
            "sessionName", "appVersion", "acceptInsecureCerts", "local", "midSessionInstallApps", "localIdentifier",
            "debug", "deviceLogs", "networkLogs", "networkLogsExcludeHosts", "networkLogsIncludeHosts",
            "appiumLogs", "video", "appStoreConfiguration", "gpsLocation", "geoLocation", "networkProfile",
            "customNetwork", "resignApp", "timezone", "disableAnimations", "enablePasscode", "deviceOrientation",
            "idleTimeout", "maskCommands", "uploadMedia", "os", "osVersion", "seleniumVersion");

    @Override
    protected boolean isDetected(Capabilities capabilities) {
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if ((!customCapabilities.isEmpty() &&
                customCapabilities.toLowerCase().contains("browserstack")) ||
                Configuration.getSeleniumUrl().contains("hub.browserstack.com") ||
                Configuration.getSeleniumUrl().contains("hub-cloud.browserstack.com")) {
            return true;
        }
        return false;
    }

    @Override
    protected Capabilities upgradeCapabilities(Capabilities capabilities) {
        HashMap<String, Object> browserstackOptions = new HashMap<>();
        DesiredCapabilities allCapabilities = new DesiredCapabilities();
        for (String capabilityName : capabilities.asMap().keySet()) {
            String cleanCapabilityName = StringUtils.removeStart(capabilityName, CapabilityHelpers.APPIUM_PREFIX);
            if (browserstackSpecificCapabilities.contains(cleanCapabilityName)) {
                browserstackOptions.put(cleanCapabilityName, capabilities.getCapability(capabilityName));
            } else {
                // browserstack is not understand capabilities with appium prefix
                allCapabilities.setCapability(cleanCapabilityName, capabilities.getCapability(capabilityName));
            }
        }

        // browserstack is not understand ios platfromName
        if (allCapabilities.getCapability("platformName") != null
                && MobilePlatform.IOS.equalsIgnoreCase(allCapabilities.getCapability("platformName").toString())) {
            allCapabilities.setCapability("platformName", "ANY");
        }

        allCapabilities.setCapability("bstack:options", browserstackOptions);
        return allCapabilities;
    }
}
