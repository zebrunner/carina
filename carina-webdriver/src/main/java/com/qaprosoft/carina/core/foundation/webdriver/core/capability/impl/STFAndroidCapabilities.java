package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstactCapabilities;

import io.appium.java_client.remote.options.SupportsLanguageOption;
import io.appium.java_client.remote.options.SupportsLocaleOption;

public class STFAndroidCapabilities extends AbstactCapabilities<MutableCapabilities> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public MutableCapabilities getCapabilities() {
        MutableCapabilities options = new MutableCapabilities();
        // this step should be executed before getConfigurationCapabilities() to be able to override this capabilities by default appium approach.
        setLocaleAndLanguage(options);
        setCapabilitiesSafe(options, getConfigurationCapabilities());
        return options;
    }

    @Override
    public MutableCapabilities createCapabilitiesFromCustom(Capabilities customCapabilities) {
        MutableCapabilities options = new MutableCapabilities();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.setCapability(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    @Override
    public MutableCapabilities getCapabilitiesWithCustom(Capabilities customCapabilities) {
        MutableCapabilities options = getCapabilities();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.setCapability(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    /*
     * http://appium.io/docs/en/writing-running-appium/caps/ locale and language
     * Locale to set for iOS (XCUITest driver only) and Android.
     * fr_CA format for iOS. CA format (country name abbreviation) for Android
     */
    private void setLocaleAndLanguage(MutableCapabilities options) {
        // parse locale param as it has language and country by default like en_US
        String localeValue = Configuration.get(Configuration.Parameter.LOCALE);
        LOGGER.debug("Default locale value is : " + localeValue);
        String[] values = localeValue.split("_");
        if (values.length == 1) {
            // only locale is present!
            options.setCapability(SupportsLocaleOption.LOCALE_OPTION, localeValue);

            String langValue = R.CONFIG.get("language");
            if (!langValue.isEmpty()) {
                LOGGER.debug("Default language value is : " + langValue);
                // provide extra capability language only if it exists among config parameters...
                options.setCapability(SupportsLanguageOption.LANGUAGE_OPTION, langValue);
            }

        } else if (values.length == 2) {
            LOGGER.debug("Put language and locale to android capabilities. language: {}; locale: {}", values[0], values[1]);
            options.setCapability(SupportsLanguageOption.LANGUAGE_OPTION, values[0]);
            options.setCapability(SupportsLocaleOption.LOCALE_OPTION, values[1]);
        } else {
            LOGGER.error("Undefined locale provided (ignoring for mobile capabilitites): {}", localeValue);
        }
    }

    private void setCapabilitiesSafe(MutableCapabilities options, Capabilities capabilities) {
        for (String capabilityName : capabilities.getCapabilityNames()) {
            options.setCapability(capabilityName, capabilities.getCapability(capabilityName));
        }
    }
}
