/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.browsermobproxy.ProxyPool;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.proxy.SystemProxy;

public abstract class AbstractCapabilities {
    private static final Logger LOGGER = Logger.getLogger(AbstractCapabilities.class);

    public abstract DesiredCapabilities getCapability(String testName);

    protected DesiredCapabilities initBaseCapabilities(DesiredCapabilities capabilities, String browser, String testName) {

        String platform = Configuration.get(Configuration.Parameter.PLATFORM);
        if (!platform.equals("*")) {
            capabilities.setPlatform(Platform.extractFromSysProperty(platform));
        }

        capabilities.setBrowserName(browser);

        // Selenium 3.4 doesn't support '*'. Only explicit or empty browser version should be provided
        String browserVersion = Configuration.get(Parameter.BROWSER_VERSION);
        if ("*".equalsIgnoreCase(browserVersion)) {
            browserVersion = "";
        }
        capabilities.setVersion(browserVersion);
        capabilities.setCapability("name", testName);

        Proxy proxy = setupProxy();
        if (proxy != null) {
            capabilities.setCapability(CapabilityType.PROXY, proxy);
        }

        // add capabilities based on dynamic _config.properties variables
        capabilities = initCapabilities(capabilities);
        
        return capabilities;
    }

    protected DesiredCapabilities initCapabilities(DesiredCapabilities capabilities) {
        // read all properties which starts from "capabilities.*" prefix and add them into desired capabilities.
        final String prefix = SpecialKeywords.CAPABILITIES + ".";
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map<String, String> capabilitiesMap = new HashMap(R.CONFIG.getProperties());
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(prefix)) {
                String value = R.CONFIG.get(entry.getKey());
                if (!value.isEmpty()) {
                    String cap = entry.getKey().replaceAll(prefix, "");
                    if ("false".equalsIgnoreCase(value)) {
                        LOGGER.debug("Set capabilities value as boolean: false");
                        capabilities.setCapability(cap, false);
                    } else if ("true".equalsIgnoreCase(value)) {
                        LOGGER.debug("Set capabilities value as boolean: true");
                        capabilities.setCapability(cap, true);
                    } else {
                        LOGGER.debug("Set capabilities value as string: " + value);
                        capabilities.setCapability(cap, value);
                    }
                }
            }
        }
        capabilities.setCapability("carinaTestRunId", SpecialKeywords.TEST_RUN_ID);
        
        //TODO: [VD] reorganize in the same way Firefox profiles args/options if any and review other browsers
        // support customization for Chrome args and options
        if (BrowserType.CHROME.equalsIgnoreCase(Configuration.getBrowser())) {
            capabilities = addChromeOptions(capabilities);
        }
        return capabilities;
    }

    protected Proxy setupProxy() {
        ProxyPool.setupBrowserMobProxy();
        SystemProxy.setupProxy();

        String proxyHost = Configuration.get(Parameter.PROXY_HOST);
        String proxyPort = Configuration.get(Parameter.PROXY_PORT);
        if (Configuration.get(Parameter.BROWSERMOB_PROXY).equals("true")) {
            proxyPort = Integer.toString(ProxyPool.getProxyPortFromThread());
        }
        List<String> protocols = Arrays.asList(Configuration.get(Parameter.PROXY_PROTOCOLS).split("[\\s,]+"));

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

            return proxy;
        }

        return null;
    }
    
    
    private DesiredCapabilities addChromeOptions(DesiredCapabilities caps) {
        // add default carina options and arguments
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        
        //update browser language
        String browserLang = Configuration.get(Parameter.BROWSER_LANGUAGE); 
        if (!browserLang.isEmpty()) {
            LOGGER.info("Set Chrome lanaguage to: " + browserLang);
            options.addArguments("--lang=" + browserLang);
        }

        if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD)) {
            HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
            chromePrefs.put("download.prompt_for_download", false);
            chromePrefs.put("download.default_directory", ReportContext.getArtifactsFolder().getAbsolutePath());
            chromePrefs.put("plugins.always_open_pdf_externally", true);
            options.setExperimentalOption("prefs", chromePrefs);
        }

        // [VD] no need to set proxy via options anymore!
        // moreover if below code is uncommented then we have double proxy start and mess in host:port values
        
        // add all custom chrome args
        for (String arg: Configuration.get(Parameter.CHROME_ARGS).split(",")) {
            options.addArguments(arg.trim());
        }
    
        // add all custom chrome experimental options, w3c=false
        for (String opts: Configuration.get(Parameter.CHROME_EXPERIMENTAL_OPTS).split(",")) {
            //TODO: think about equal sign inside name or value later
            opts = opts.trim();
            String name = opts.split("=")[0].trim();
            String value = opts.split("=")[1].trim();
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                options.setExperimentalOption(name, Boolean.valueOf(value));
            } else {
                options.setExperimentalOption(name, value);
            }
        }
        caps.setCapability(ChromeOptions.CAPABILITY, options);
        return caps;
    }


}
