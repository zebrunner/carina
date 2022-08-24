/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.android.options.EspressoOptions;

public class EspressoCapabilities extends AbstractCapabilities<EspressoOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public EspressoOptions getCapability(String testName) {
        EspressoOptions capabilities = new EspressoOptions();
        // this step should be executed before initCapabilities() to be able to override this capabilities by default appium approach.
        setLocaleAndLanguage(capabilities);
        // add capabilities based on dynamic _config.properties variables
        initCapabilities(capabilities);
        return capabilities;
    }

    private void setLocaleAndLanguage(EspressoOptions caps) {
        /*
         * http://appium.io/docs/en/writing-running-appium/caps/ locale and language
         * Locale to set for iOS (XCUITest driver only) and Android.
         * fr_CA format for iOS. CA format (country name abbreviation) for Android
         */

        // parse locale param as it has language and country by default like en_US
        String localeValue = Configuration.get(Parameter.LOCALE);
        LOGGER.debug("Default locale value is : {}", localeValue);
        String[] values = localeValue.split("_");
        if (values.length == 1) {
            // only locale is present!
            caps.setLocale(localeValue);

            // todo add language in the Configuration.Parameters
            String langValue = R.CONFIG.get("language");
            if (!langValue.isEmpty()) {
                LOGGER.debug("Default language value is : {}", langValue);
                // provide extra capability language only if it exists among config parameters...
                caps.setLanguage(langValue);
            }

        } else if (values.length == 2) {
            LOGGER.debug("Put language and locale to android capabilities. language: " + values[0] + "; locale: " + values[1]);
            caps.setLanguage(values[0]);
            caps.setLocale(values[1]);

        } else {
            LOGGER.error("Undefined locale provided (ignoring for mobile capabilitites): " + localeValue);
        }
    }
}
