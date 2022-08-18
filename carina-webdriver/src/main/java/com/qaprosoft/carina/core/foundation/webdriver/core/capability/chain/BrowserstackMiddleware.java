package com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

import io.appium.java_client.internal.CapabilityHelpers;
import io.appium.java_client.remote.MobilePlatform;

public class BrowserstackMiddleware extends CapabilitiesMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // todo add special capabilites for chrome, firefox - browserstack custom
    // todo add more and refactor
    private static final List<String> browserstackSpecificCapabilities = Arrays.asList("userName", "accessKey", "appiumVersion", "projectName",
            "buildName",
            "sessionName", "appVersion", "acceptInsecureCerts", "local", "midSessionInstallApps", "localIdentifier",
            "debug", "deviceLogs", "networkLogs", "networkLogsExcludeHosts", "networkLogsIncludeHosts",
            "appiumLogs", "video", "appStoreConfiguration", "gpsLocation", "geoLocation", "networkProfile",
            "customNetwork", "resignApp", "timezone", "disableAnimations", "enablePasscode", "deviceOrientation",
            "idleTimeout", "maskCommands", "uploadMedia", "os", "osVersion", "seleniumVersion");

    private static final List<String> chromeSpecificCapabilities = Arrays.asList("driver");
    // todo investigate ie
    private static final List<String> edgeSpecificCapabilities = Arrays.asList("enablePopups", "browserstack.sendKeys");
    private static final List<String> safariSpecificCapabilities = Arrays.asList("enablePopups", "allowAllCookies", "driver");
    private static final List<String> firefoxSpecificCapabilities = Arrays.asList("driver");

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
        LOGGER.debug("Capabilities will be refactored by browserstack rules");

        HashMap<String, Object> browserstackOptions = new HashMap<>();
        HashMap<String, Object> chromeSpecificOptions = new HashMap<>();
        HashMap<String, Object> edgeSpecificOptions = new HashMap<>();
        HashMap<String, Object> safariSpecificOptions = new HashMap<>();
        HashMap<String, Object> firefoxSpecificOptions = new HashMap<>();

        String browserName = capabilities.getBrowserName();
        DesiredCapabilities allCapabilities = new DesiredCapabilities();
        for (String capabilityName : capabilities.asMap().keySet()) {
            String cleanCapabilityName = StringUtils.removeStart(capabilityName, CapabilityHelpers.APPIUM_PREFIX);
            if (browserstackSpecificCapabilities.contains(cleanCapabilityName)) {
                browserstackOptions.put(cleanCapabilityName, capabilities.getCapability(capabilityName));
                continue;
            }

            if (Browser.CHROME.browserName().equalsIgnoreCase(browserName) &&
                    chromeSpecificCapabilities.contains(cleanCapabilityName)) {
                chromeSpecificOptions.put(cleanCapabilityName, capabilities.getCapability(capabilityName));
                continue;
            }

            if (Browser.EDGE.browserName().equalsIgnoreCase(browserName) &&
                    edgeSpecificCapabilities.contains(cleanCapabilityName)) {
                edgeSpecificOptions.put(cleanCapabilityName, capabilities.getCapability(capabilityName));
                continue;
            }
            if (Browser.SAFARI.browserName().equalsIgnoreCase(browserName) &&
                    safariSpecificCapabilities.contains(cleanCapabilityName)) {
                safariSpecificOptions.put(cleanCapabilityName, capabilities.getCapability(capabilityName));
                continue;
            }

            if (Browser.FIREFOX.browserName().equalsIgnoreCase(browserName) &&
                    firefoxSpecificCapabilities.contains(cleanCapabilityName)) {
                firefoxSpecificOptions.put(cleanCapabilityName, capabilities.getCapability(capabilityName));
                continue;
            }

            else {
                // browserstack is not understand capabilities with appium prefix
                allCapabilities.setCapability(cleanCapabilityName, capabilities.getCapability(capabilityName));
            }
        }

        // browserstack is not understand ios platfromName
        if (allCapabilities.getCapability("platformName") != null
                && MobilePlatform.IOS.equalsIgnoreCase(allCapabilities.getCapability("platformName").toString())) {
            allCapabilities.setCapability("platformName", "ANY");
        }

        if (!chromeSpecificOptions.isEmpty()) {
            browserstackOptions.put("chrome", chromeSpecificOptions);
        }

        if (!edgeSpecificOptions.isEmpty()) {
            browserstackOptions.put("edge", edgeSpecificOptions);
        }

        if (!safariSpecificOptions.isEmpty()) {
            browserstackOptions.put("safari", safariSpecificOptions);
        }

        if (!firefoxSpecificOptions.isEmpty()) {
            browserstackOptions.put("firefox", firefoxSpecificOptions);
        }

        if (!browserstackOptions.isEmpty()) {
            allCapabilities.setCapability("bstack:options", browserstackOptions);
        }

        return allCapabilities;
    }
}
