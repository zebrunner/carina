/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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

import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class MobileCapabilies extends AbstractCapabilities {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public DesiredCapabilities getCapability(String testName) {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        //this step should be executed before initCapabilities() to be able to override this capabilities by default appium approach.
        capabilities = setLocaleAndLanguage(capabilities);
        // add capabilities based on dynamic _config.properties variables
        capabilities = initCapabilities(capabilities);
        return capabilities;
    }    
    
    private DesiredCapabilities setLocaleAndLanguage(DesiredCapabilities caps) {
        /*
         * http://appium.io/docs/en/writing-running-appium/caps/ locale and language
         * Locale to set for iOS (XCUITest driver only) and Android.
         * fr_CA format for iOS. CA format (country name abbreviation) for Android
         */
        
        // parse locale param as it has language and country by default like en_US
        String localeValue = Configuration.get(Parameter.LOCALE);
        LOGGER.debug("Default locale value is : " + localeValue);
        String[] values = localeValue.split("_");
        if (values.length == 1) {
            // only locale is present!
            caps.setCapability("locale", localeValue);
            
            String langValue = R.CONFIG.get("language");
            if (!langValue.isEmpty()) {
                LOGGER.debug("Default language value is : " + langValue);
                // provide extra capability language only if it exists among config parameters...
                caps.setCapability("language", langValue);
            }
            
        } else if (values.length == 2) {
            if (Configuration.getPlatform(caps).equalsIgnoreCase(SpecialKeywords.ANDROID)) {
                LOGGER.debug("Put language and locale to android capabilities. language: " + values[0] + "; locale: " + values[1]);
                caps.setCapability("language", values[0]);
                caps.setCapability("locale", values[1]);
            } else if (Configuration.getPlatform().equalsIgnoreCase(SpecialKeywords.IOS)) {
                LOGGER.debug("Put language and locale to iOS capabilities. language: " + values[0] + "; locale: " + localeValue);
                caps.setCapability("language", values[0]);
                caps.setCapability("locale", localeValue);
            }        
        } else {
            LOGGER.error("Undefined locale provided (ignoring for mobile capabilitites): " + localeValue);
        }
        return caps;
    }
}
