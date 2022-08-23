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
import org.openqa.selenium.remote.BrowserType;
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

public abstract class AbstractCapabilities<T extends MutableCapabilities> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public abstract T getCapability(String testName);

    protected void initBaseCapabilities(T capabilities, String testName) {
        if (!IDriverPool.DEFAULT.equalsIgnoreCase(testName)) {
            // #1573: remove "default" driver name capability registration
            capabilities.setCapability("name", testName);
        }

        Proxy proxy = setupProxy();
        if (proxy != null) {
            capabilities.setCapability(CapabilityType.PROXY, proxy);
        }

        // add capabilities based on dynamic _config.properties variables
        initCapabilities(capabilities);
    }

    protected void initCapabilities(T capabilities) {
        ArrayList<String> numericCaps = new ArrayList<String>(
                Arrays.asList("idleTimeout", "waitForIdleTimeout"));
        
        // read all properties which starts from "capabilities.*" prefix and add them into desired capabilities.
        final String prefix = SpecialKeywords.CAPABILITIES + ".";
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map<String, String> capabilitiesMap = new HashMap(R.CONFIG.getProperties());
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(prefix)) {
                String value = R.CONFIG.get(entry.getKey());                
                if (!value.isEmpty()) {
                    String cap = entry.getKey().replaceAll(prefix, "");
                    if (numericCaps.contains(cap) && isNumber(value)) {
                        LOGGER.debug("Adding " + cap + " to capabilities as integer");
                        capabilities.setCapability(cap, Integer.parseInt(value));
                    } else if ("false".equalsIgnoreCase(value)) {
                        capabilities.setCapability(cap, false);
                    } else if ("true".equalsIgnoreCase(value)) {
                        capabilities.setCapability(cap, true);
                    } else {
                        capabilities.setCapability(cap, value);
                    }
                }
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
            if (BrowserType.FIREFOX.equalsIgnoreCase(browser)
                    || BrowserType.CHROME.equalsIgnoreCase(browser)
                    && Configuration.getDriverType().equalsIgnoreCase(SpecialKeywords.DESKTOP)) {
                LOGGER.info("Browser will be started in headless mode. VNC and Video will be disabled.");
                capabilities.setCapability("enableVNC", false);
                capabilities.setCapability("enableVideo", false);
            } else {
                LOGGER.error(String.format("Headless mode isn't supported by %s browser / platform.", browser));
            }
        }
        initSpecialCapabilities(capabilities);
    }

    protected void initSpecialCapabilities(T capabilities) {
        if (!Objects.equals(R.CONFIG.get("isW3C"), "true")) {
            return;
        }
        String provider = R.CONFIG.get("provider");
        if (Objects.equals(provider, StringUtils.EMPTY)) {
            return;
        }

        Map<String, String> capabilitiesMap = new HashMap(R.CONFIG.getProperties());
        HashMap<String, String> specialCapabilities = new HashMap<>();

        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            if (!entry.getKey().toLowerCase().startsWith(provider)) {
                continue;
            }

            String value = R.CONFIG.get(entry.getKey());
            if (value.isEmpty()) {
                continue;
            }

            String cap = entry.getKey().replaceAll(provider, "");
            specialCapabilities.put(cap, value);
        }
        capabilities.setCapability(provider + ":options", specialCapabilities);
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
                LOGGER.info(String.format("Http proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setHttpProxy(proxyAddress);
            }

            if (protocols.contains("https")) {
                LOGGER.info(String.format("Https proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setSslProxy(proxyAddress);
            }

            if (protocols.contains("ftp")) {
                LOGGER.info(String.format("FTP proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setFtpProxy(proxyAddress);
            }

            if (protocols.contains("socks")) {
                LOGGER.info(String.format("Socks proxy will be set: %s:%s", proxyHost, proxyPort));
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
}
