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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import java.util.Arrays;
import java.util.HashMap;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class ChromeCapabilities extends AbstractCapabilities {
    public DesiredCapabilities getCapability(String testName) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities = initBaseCapabilities(capabilities, BrowserType.CHROME, testName);
        capabilities.setCapability("chrome.switches", Arrays.asList("--start-maximized", "--ignore-ssl-errors"));
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        
        //update browser language
        String browserLocale = Configuration.get(Parameter.BROWSER_LOCALE); 
        if (!browserLocale.isEmpty()) {
        	LOGGER.info("Set Chrome lanaguage to: " + browserLocale);
        	options.addArguments("--lang=" + browserLocale);
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

/*        Proxy proxy = setupProxy();
        if (proxy != null) {
        	// explicitely add proxy as chrome option
        	// https://github.com/SeleniumHQ/selenium/issues/5299
        	options.setProxy(proxy);
        }*/

        
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        return capabilities;
    }
}
