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
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.browsermobproxy.ProxyPool;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.proxy.SystemProxy;

import io.appium.java_client.remote.options.SupportsLanguageOption;
import io.appium.java_client.remote.options.SupportsLocaleOption;

public abstract class AbstractCapabilities<T extends MutableCapabilities> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Generate Capabilities according to configuration file
     */
    public abstract T getCapability(String testName);

    protected T initBaseCapabilities(T capabilities, String testName) {

        if (!IDriverPool.DEFAULT.equalsIgnoreCase(testName)) {
            // #1573: remove "default" driver name capability registration
            capabilities.setCapability("name", testName);
        }

        Proxy proxy = setupProxy();
        if (proxy != null) {
            capabilities.setCapability(CapabilityType.PROXY, proxy);
        }

        // add capabilities based on dynamic _config.properties variables
        return initCapabilities(capabilities);
    }

    protected T initCapabilities(T capabilities) {
        ArrayList<String> numericCaps = new ArrayList<>(Arrays.asList("idleTimeout", "waitForIdleTimeout"));
        
        // read all properties which starts from "capabilities.*" prefix and add them into desired capabilities.
        final String prefix = SpecialKeywords.CAPABILITIES + ".";

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map<String, String> capabilitiesMap = new HashMap(R.CONFIG.getProperties());

        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            if (!entry.getKey().toLowerCase().startsWith(prefix)) {
                continue;
            }

            String value = R.CONFIG.get(entry.getKey());
            if (value.isEmpty()) {
                continue;
            }

            String capability = entry.getKey().replaceAll(prefix, "");

            if (numericCaps.contains(capability) && isNumber(value)) {
                LOGGER.debug("Adding {} to capabilities as integer", capability);
                capabilities.setCapability(capability, Integer.parseInt(value));
            } else if ("false".equalsIgnoreCase(value)) {
                capabilities.setCapability(capability, false);
            } else if ("true".equalsIgnoreCase(value)) {
                capabilities.setCapability(capability, true);
            } else {
                capabilities.setCapability(capability, value);
                }
            }


        //TODO: [VD] reorganize in the same way Firefox profiles args/options if any and review other browsers
        // support customization for Chrome args and options

        // for pc we may set browserName through Desired capabilities in our Test with a help of a method initBaseCapabilities,
        // so we don't want to override with value from config
        String browser;
        if (capabilities.getBrowserName() != null && capabilities.getBrowserName().length() > 0) {
            browser = capabilities.getBrowserName();
        } else {
            browser = Configuration.getBrowser();
        }

        if (Configuration.getBoolean(Parameter.HEADLESS)) {
            if (Browser.FIREFOX.browserName().equalsIgnoreCase(browser)
                    || Browser.CHROME.browserName().equalsIgnoreCase(browser)
                    && Configuration.getDriverType().equalsIgnoreCase(SpecialKeywords.DESKTOP)) {
                LOGGER.info("Browser will be started in headless mode. VNC and Video will be disabled.");
                capabilities.setCapability("enableVNC", false);
                capabilities.setCapability("enableVideo", false);
            } else {
                LOGGER.error("Headless mode isn't supported by {} browser / platform.", browser);
            }
        }
        return initCustomCapabilities(capabilities);
    }

    private T initCustomCapabilities(T capabilities) {
        String provider = Configuration.get(Parameter.PROVIDER);

        if (Objects.equals(provider, StringUtils.EMPTY)) {
            return capabilities;
        }

        Map<String, String> propertiesMap = new HashMap(R.CONFIG.getProperties());
        Map<String, Object> customCapabilities = new HashMap<>();

        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {

            if (entry.getKey().toLowerCase().startsWith(provider + ".capabilities")) {
                String cap = entry.getKey().replaceFirst(provider + ".capabilities.", "");
                String value = R.CONFIG.get(entry.getKey());
                if (value.isEmpty()) {
                    continue;
                }
                customCapabilities.put(cap, value);
            }
        }

        if ("true".equalsIgnoreCase(Configuration.get(Parameter.W3C)) && !customCapabilities.isEmpty()) {
            capabilities.setCapability(provider + ":options", customCapabilities);
        } else {
            for (String capabilityName : customCapabilities.keySet()) {
                capabilities.setCapability(capabilityName, customCapabilities.get(capabilityName));
            }
        }
        return capabilities;
    }

    protected Proxy setupProxy() {
        ProxyPool.setupBrowserMobProxy();
        SystemProxy.setupProxy();

        String proxyHost = Configuration.get(Parameter.PROXY_HOST);
        String proxyPort = Configuration.get(Parameter.PROXY_PORT);
        String noProxy = Configuration.get(Parameter.NO_PROXY);
        
        if (Configuration.get(Parameter.BROWSERMOB_PROXY).equals("true")) {
            proxyPort = Integer.toString(ProxyPool.getProxyPortFromThread());
        }
        List<String> protocols = Arrays.asList(Configuration.get(Parameter.PROXY_PROTOCOLS).split("[\\s,]+"));

        //TODO: test removal comparing with null
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {

            org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
            String proxyAddress = String.format("%s:%s", proxyHost, proxyPort);

            if (protocols.contains("http")) {
                LOGGER.info("Http proxy will be set: {}:{}", proxyHost, proxyPort);
                proxy.setHttpProxy(proxyAddress);
            }

            if (protocols.contains("https")) {
                LOGGER.info("Https proxy will be set: {}:{}", proxyHost, proxyPort);
                proxy.setSslProxy(proxyAddress);
            }

            if (protocols.contains("ftp")) {
                LOGGER.info("FTP proxy will be set: {}:{}", proxyHost, proxyPort);
                proxy.setFtpProxy(proxyAddress);
            }

            if (protocols.contains("socks")) {
                LOGGER.info("Socks proxy will be set: {}:{}", proxyHost, proxyPort);
                proxy.setSocksProxy(proxyAddress);
            }
            
            if (!noProxy.isEmpty()) {
                proxy.setNoProxy(noProxy);
            }

            return proxy;
        }

        return null;
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
