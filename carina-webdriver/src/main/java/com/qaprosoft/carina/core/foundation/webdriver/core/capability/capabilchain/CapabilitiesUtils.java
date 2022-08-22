package com.qaprosoft.carina.core.foundation.webdriver.core.capability.capabilchain;

import org.openqa.selenium.Capabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class CapabilitiesUtils {

    public static final String BROWSERSTACK_SPECIFIC_CAPABILITIES = "bstack:options";
    public static final String SAUCELABS_SPECIFIC_CAPABILITIES = "sauce:options";

    //
    // public static boolean isCustomDriver() {
    // String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
    // if (!customCapabilities.isEmpty()) {
    //
    // if (customCapabilities.toLowerCase().contains("browserstack") ||
    // Configuration.getSeleniumUrl().contains("hub.browserstack.com") ||
    // Configuration.getSeleniumUrl().contains("hub-cloud.browserstack.com")) {
    // LOGGER.info("Browserstack was detected in custom_capability path! RemoteWebDriver will be used");
    // return true;
    // }
    //
    // if (customCapabilities.toLowerCase().contains("saucelabs")) {
    // LOGGER.info("Saucelabs was detected in custom_capability path! RemoteWebDriver will be used");
    // return true;
    // }
    //
    // // todo investigate usage
    // if (customCapabilities.toLowerCase().contains("localhost")) {
    // LOGGER.info("localhost name was detected in custom_capability path! RemoteWebDriver will be used");
    // }
    // }
    //
    // return false;
    // }

    public static boolean isSauceLabsDetected() {
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if ((!customCapabilities.isEmpty() &&
                customCapabilities.toLowerCase().contains("saucelabs")) ||
                Configuration.getSeleniumUrl().contains("saucelabs.com")) {
            return true;
        }
        return false;
    }

    public static boolean isBrowserStackDetected() {
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);

        // EDIT: It is not a good idea to detect browserstack by this capability
        // // if user already set bstack:options, supposed that the user already knows what he doing
        // if (capabilities.getCapability("bstack:options") != null) {
        // return true;
        // }

        if ((!customCapabilities.isEmpty() &&
                customCapabilities.toLowerCase().contains("browserstack")) ||
                Configuration.getSeleniumUrl().contains("hub.browserstack.com") ||
                Configuration.getSeleniumUrl().contains("hub-cloud.browserstack.com")) {
            return true;
        }
        return false;
    }

    public static boolean isBrowserStackSpecificCapabilitiesDetected(Capabilities capabilities) {
        return capabilities.getCapability(BROWSERSTACK_SPECIFIC_CAPABILITIES) != null;
    }

    public static boolean isSauceLabsSpecificCapabilitiesDetected(Capabilities capabilities) {
        return capabilities.getCapability(SAUCELABS_SPECIFIC_CAPABILITIES) != null;
    }
}
