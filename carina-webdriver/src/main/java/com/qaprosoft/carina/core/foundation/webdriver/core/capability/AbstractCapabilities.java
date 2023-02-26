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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.zebrunner.carina.proxy.ProxyUtils;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;

import io.appium.java_client.remote.options.SupportsLanguageOption;
import io.appium.java_client.remote.options.SupportsLocaleOption;

public abstract class AbstractCapabilities<T extends MutableCapabilities> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ArrayList<String> NUMERIC_CAPABILITIES = new ArrayList<>(
            Arrays.asList("waitForIdleTimeout", "zebrunner:options.waitForIdleTimeout"));
    private static final List<String> STRING_CAPABILITIES = List.of("idleTimeout", "zebrunner:options.idleTimeout");

    /**
     * Generate capabilities. Capabilities will be taken from the configuration.
     * Additional capabilities may also be added (depends on the implementation)
     * 
     * @param testName todo add description
     * @return see {@link T}
     */
    public abstract T getCapability(String testName);

    protected void initBaseCapabilities(T capabilities, String testName) {
        if (!IDriverPool.DEFAULT.equalsIgnoreCase(testName)) {
            // #1573: remove "default" driver name capability registration
            R.CONFIG.put("capabilities.zebrunner:options.name", testName, true);
        }

        ProxyUtils.getSeleniumProxy()
                .ifPresent(proxy -> capabilities.setCapability(CapabilityType.PROXY, proxy));

        // for pc we may set browserName through Desired capabilities in our Test with a help of a method initBaseCapabilities,
        // so we don't want to override with value from config
        String browser = capabilities.getBrowserName() != null && capabilities.getBrowserName().length() > 0 ? capabilities.getBrowserName()
                : Configuration.getBrowser();

        if (Configuration.getBoolean(Parameter.HEADLESS)) {
            if (Browser.FIREFOX.browserName().equalsIgnoreCase(browser)
                    || Browser.CHROME.browserName().equalsIgnoreCase(browser)
                            && Configuration.getDriverType().equalsIgnoreCase(SpecialKeywords.DESKTOP)) {
                LOGGER.info("Browser will be started in headless mode. VNC and Video will be disabled.");
                R.CONFIG.put("capabilities.zebrunner:options.enableVNC", "false", true);
                R.CONFIG.put("capabilities.zebrunner:options.enableVideo", "false", true);
            } else {
                LOGGER.error("Headless mode isn't supported by {} browser / platform.", browser);
            }
        }
        // add capabilities based on dynamic _config.properties variables
        initCapabilities(capabilities);
    }

    /**
     * Add capabilities from configuration {@link R#CONFIG}.
     * 
     * @param options see {@link T}
     */
    protected void initCapabilities(T options) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map<String, String> properties = new HashMap(R.CONFIG.getProperties());
        Map<String, Object> capabilities = properties.entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith("capabilities."))
                .filter(entry -> entry.getValue() != null)
                .map(entry -> {
                    MutablePair<String, String> pair = new MutablePair<>();
                    pair.setLeft(entry.getKey().replaceFirst("capabilities.", ""));
                    pair.setRight(entry.getValue());
                    return pair;
                })
                .map(p -> {
                    MutablePair<String, Object> pair = new MutablePair<>();
                    pair.setLeft(p.getLeft());
                    String stringValue = p.getRight();

                    if (STRING_CAPABILITIES.contains(p.getLeft())) {
                        // custom Zebrunner logic
                        pair.setRight(stringValue);
                    } else if (NUMERIC_CAPABILITIES.contains(p.getLeft())) {
                        // custom Zebrunner logic
                        pair.setRight(Integer.parseInt(stringValue));
                    } else if (isNumber(stringValue)) {
                        pair.setRight(Integer.parseInt(stringValue));
                    } else if ("true".equalsIgnoreCase(stringValue)) {
                        pair.setRight(true);
                    } else if ("false".equalsIgnoreCase(stringValue)) {
                        pair.setRight(false);
                    } else {
                        pair.setRight(stringValue);
                    }
                    return pair;
                }).collect(Collectors.toMap(MutablePair::getLeft, MutablePair::getRight));

        for (Map.Entry<String, Object> entry : capabilities.entrySet()) {
            List<String> names = Arrays.asList(entry.getKey().split("\\."));

            // TODO add support of any nesting. maybe use some algorithm? or there will never be such nesting
            if (names.isEmpty()) {
                // it should never happens
                throw new RuntimeException("Something went wrong when try to create capabilities from configuration.");
            } else if (names.size() == 1) {
                options.setCapability(names.get(0), entry.getValue());
            } else if (names.size() == 2) {
                HashMap<String, Object> nestCapability = new HashMap<>();
                if (options.getCapability(names.get(0)) != null) {
                    // If we already have inner capability, we think that it is HashMap<String, Object> (custom capabilities)
                    nestCapability = (HashMap<String, Object>) options.getCapability(names.get(0));
                }

                nestCapability.put(names.get(1), entry.getValue());
                options.setCapability(names.get(0), nestCapability);
            } else if (names.size() == 3) {
                HashMap<String, Object> nestCapability = new HashMap<>();
                HashMap<String, Object> secondNestCapability = new HashMap<>();

                if (options.getCapability(names.get(0)) != null) {
                    // If we already have inner capability, we think that it is HashMap<String, Object> (custom capabilities)
                    // todo investigate if we have situations when value that already present is not HashMap
                    nestCapability = (HashMap<String, Object>) options.getCapability(names.get(0));
                    if (nestCapability.containsKey(names.get(1))) {
                        secondNestCapability = (HashMap<String, Object>) nestCapability.get(names.get(1));
                    }
                }
                secondNestCapability.put(names.get(2), entry.getValue());
                nestCapability.put(names.get(1), secondNestCapability);
                options.setCapability(names.get(0), nestCapability);
            } else {
                // Let's hope it won't be needed.
                throw new UnsupportedOperationException("At the moment nesting of more than 3 capabilities is not supported. "
                        + "If you come across a situation in which this is necessary, please notify the Carina Support team.");
            }
        }
        //TODO: [VD] reorganize in the same way Firefox profiles args/options if any and review other browsers
        // support customization for Chrome args and options
    }

    protected boolean isNumber(String value) {
        if (value == null || value.isEmpty()){
            return false;
        }

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException ex){
            return false;
        }

        return true;
    }

    /**
     * Add locale and language capabilities to caps param
     */
    protected T setLocaleAndLanguage(T caps) {
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
            caps.setCapability(SupportsLocaleOption.LOCALE_OPTION, localeValue);

            String langValue = Configuration.get(Parameter.LANGUAGE);
            if (!langValue.isEmpty()) {
                LOGGER.debug("Default language value is : {}", langValue);
                // provide extra capability language only if it exists among config parameters...
                caps.setCapability(SupportsLanguageOption.LANGUAGE_OPTION, langValue);
            }

        } else if (values.length == 2) {
            if (Configuration.getPlatform(caps).equalsIgnoreCase(SpecialKeywords.ANDROID)) {
                LOGGER.debug("Put language and locale to android capabilities. language: {}; locale: {}", values[0], values[1]);
                caps.setCapability(SupportsLanguageOption.LANGUAGE_OPTION, values[0]);
                caps.setCapability(SupportsLocaleOption.LOCALE_OPTION, values[1]);
            } else if (Configuration.getPlatform().equalsIgnoreCase(SpecialKeywords.IOS)) {
                LOGGER.debug("Put language and locale to iOS capabilities. language: {}; locale: {}", values[0], localeValue);
                caps.setCapability(SupportsLanguageOption.LANGUAGE_OPTION, values[0]);
                caps.setCapability(SupportsLocaleOption.LOCALE_OPTION, localeValue);
            }
        } else {
            LOGGER.error("Undefined locale provided (ignoring for mobile capabilitites): {}", localeValue);
        }
        return caps;
    }
}
