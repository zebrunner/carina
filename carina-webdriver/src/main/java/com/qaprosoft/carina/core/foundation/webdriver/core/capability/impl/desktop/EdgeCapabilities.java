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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.report.ReportContext;

public class EdgeCapabilities extends AbstractCapabilities<ChromiumOptions<?>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public ChromiumOptions<?> getCapabilities() {
        ChromiumOptions<?> options = new ChromiumOptions<>(CapabilityType.BROWSER_NAME, Browser.EDGE.browserName(), "ms:edgeOptions");
        addProxy(options);
        addConfigurationCapabilities(options);
        addEdgeOptions(options);
        options.addArguments("--start-maximized", "--ignore-ssl-errors");
        options.setAcceptInsecureCerts(true);
        return options;
    }

    @Override
    public ChromiumOptions<?> getCapability(String testName) {
        return getCapabilities();
    }

    /**
     * Add edge-specific arguments, prefs.
     *
     * @param options see {@link ChromiumOptions}
     */
    private void addEdgeOptions(ChromiumOptions<?> options) {
        Map<String, Object> prefs = new HashMap<>();
        boolean needsPrefs = false;
        // disable the "unsupported flag" prompt
        options.addArguments("--test-type");
        // update browser language
        String browserLang = Configuration.get(Configuration.Parameter.BROWSER_LANGUAGE);
        if (!browserLang.isEmpty()) {
            LOGGER.info("Set Edge language to: {}", browserLang);
            options.addArguments("--lang=" + browserLang);
            prefs.put("intl.accept_languages", browserLang);
            needsPrefs = true;
        }

        if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD)) {
            prefs.put("download.prompt_for_download", false);
            if (!"zebrunner".equalsIgnoreCase(R.CONFIG.get(SpecialKeywords.PROVIDER))) {
                prefs.put("download.default_directory",
                        ReportContext.getArtifactsFolder().getAbsolutePath());
            }
            needsPrefs = true;
        }

        if (needsPrefs) {
            options.setCapability("prefs", prefs);
        }
        options.setCapability("ms:edgeChrominum", true);

        String driverType = Configuration.getDriverType();
        if (Configuration.getBoolean(Configuration.Parameter.HEADLESS)
                && driverType.equals(SpecialKeywords.DESKTOP)) {
            options.setHeadless(Configuration.getBoolean(Configuration.Parameter.HEADLESS));
        }
    }
}
