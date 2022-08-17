package com.qaprosoft.carina.core.foundation.webdriver.core.factory;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class DriverUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static boolean isCustomDriver() {
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if ((!customCapabilities.isEmpty() &&
                customCapabilities.toLowerCase().contains("browserstack")) ||
                Configuration.getSeleniumUrl().contains("hub.browserstack.com") ||
                Configuration.getSeleniumUrl().contains("hub-cloud.browserstack.com")) {
            LOGGER.info("Browserstack was detected! RemoteWebDriver will be used instead of AndroidDriver");
            return true;
        }
        return false;
    }
}
