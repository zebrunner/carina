package com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class BrowserstackMiddleware extends CapabilitiesMiddleware {

    // todo add more and refactor
    List<String> browserstackSpecificCapabilities = Arrays.asList("userName", "accessKey", "os", "osVersion", "projectName", "buildName",
            "sessionName", "local", "localIdentifier", "debug", "consoleLogs", "networkLogs", "appiumLogs",
            "video", "seleniumLogs", "telemetryLogs", "geoLocation", "timezone", "resolution",
            "seleniumVersion", "browserstack.maskCommands", "idleTimeout", "maskBasicAuth",
            "autoWait", "hosts", "bfcache", "wsLocalSupport", "disableCorsRestrictions",
            "buildTag", "deviceName", "realMobile", "appiumVersion", "deviceOrientation",
            "customNetwork", "networkProfile");

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
            if (browserstackSpecificCapabilities.contains(capabilityName)) {
                browserstackOptions.put(capabilityName, capabilities.getCapability(capabilityName));
            } else {
                allCapabilities.setCapability(capabilityName, capabilities.getCapability(capabilityName));
            }
        }

        allCapabilities.setCapability("bstack:options", browserstackOptions);
        return allCapabilities;
    }
}
