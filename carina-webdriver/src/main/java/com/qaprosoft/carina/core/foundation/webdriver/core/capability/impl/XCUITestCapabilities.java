package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.ios.options.XCUITestOptions;

public class XCUITestCapabilities extends AbstractCapabilities<XCUITestOptions> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public XCUITestOptions getCapabilities(Capabilities capabilities) {
        XCUITestOptions options = new XCUITestOptions();
        // this step should be executed before getConfigurationCapabilities() to be able to override this capabilities by default appium approach.
        setLocaleAndLanguage(options);
        setCapabilities(options, getConfigurationCapabilities());
        return options;
    }

    /*
     * http://appium.io/docs/en/writing-running-appium/caps/ locale and language
     * Locale to set for iOS (XCUITest driver only) and Android.
     * fr_CA format for iOS. CA format (country name abbreviation) for Android
     */
    private void setLocaleAndLanguage(XCUITestOptions options) {
        // parse locale param as it has language and country by default like en_US
        String localeValue = Configuration.get(Configuration.Parameter.LOCALE);
        LOGGER.debug("Default locale value is : " + localeValue);
        String[] values = localeValue.split("_");
        if (values.length == 1) {
            // only locale is present!
            options.setLocale(localeValue);

            String langValue = R.CONFIG.get("language");
            if (!langValue.isEmpty()) {
                LOGGER.debug("Default language value is : " + langValue);
                // provide extra capability language only if it exists among config parameters...
                options.setLanguage(langValue);
            }

        } else if (values.length == 2) {
            LOGGER.debug("Put language and locale to iOS capabilities. language: {}; locale: {}", values[0], localeValue);
            options.setLanguage(values[0]);
            options.setLocale(localeValue);

        } else {
            LOGGER.error("Undefined locale provided (ignoring for mobile capabilitites): {}", localeValue);
        }
    }
}
