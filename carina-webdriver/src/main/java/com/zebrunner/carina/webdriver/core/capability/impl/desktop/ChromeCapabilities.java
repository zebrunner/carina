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
package com.zebrunner.carina.webdriver.core.capability.impl.desktop;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.webdriver.core.capability.AbstractCapabilities;

public class ChromeCapabilities extends AbstractCapabilities<ChromeOptions> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Generate ChromeOptions depends on capabilities defines in configuration file
     * Also adds chrome-specific arguments, prefs and so on
     */
    @Override
    public ChromeOptions getCapability(String testName) {
        ChromeOptions options = new ChromeOptions();
        initBaseCapabilities(options, testName);
        addChromeOptions(options);
        options.addArguments("--start-maximized", "--ignore-ssl-errors");
        options.setAcceptInsecureCerts(true);
        return options;
    }

    /**
     * Add chrome-specific arguments, prefs and so on
     * 
     * @param options ChromeOptions to which will be added
     */
    private void addChromeOptions(ChromeOptions options) {
        // add default carina options and arguments
        // disable the "unsupported flag" prompt
        options.addArguments("--test-type");
        // prefs
        HashMap<String, Object> chromePrefs = new HashMap<>();
        boolean needsPrefs = false;

        // update browser language
        String browserLang = Configuration.get(Configuration.Parameter.BROWSER_LANGUAGE);
        if (!browserLang.isEmpty()) {
            LOGGER.info("Set Chrome language to: {}", browserLang);
            options.addArguments("--lang=" + browserLang);
            chromePrefs.put("intl.accept_languages", browserLang);
            needsPrefs = true;
        }

        if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD)) {
            chromePrefs.put("download.prompt_for_download", false);
            if (!"zebrunner".equalsIgnoreCase(R.CONFIG.get(SpecialKeywords.PROVIDER))) {
                // don't override auto download dir for Zebrunner Selenium Grid (Selenoid)
                chromePrefs.put("download.default_directory", ReportContext.getArtifactsFolder().getAbsolutePath());
            }
            chromePrefs.put("plugins.always_open_pdf_externally", true);
            needsPrefs = true;
        }


        // [VD] no need to set proxy via options anymore!
        // moreover if below code is uncommented then we have double proxy start and mess in host:port values

        // setup default mobile chrome args and preferences
        String driverType = Configuration.getDriverType();
        if (SpecialKeywords.MOBILE.equals(driverType)) {
            options.addArguments("--no-first-run");
            options.addArguments("--disable-notifications");
            options.setExperimentalOption("w3c", false);
        }

        // add all custom chrome args
        for (String arg : Configuration.get(Configuration.Parameter.CHROME_ARGS).split(",")) {
            if (arg.isEmpty()) {
                continue;
            }
            options.addArguments(arg.trim());
        }

        // add all custom chrome experimental options, w3c=false
        String experimentalOptions = Configuration.get(Configuration.Parameter.CHROME_EXPERIMENTAL_OPTS);
        if (!experimentalOptions.isEmpty()) {
            needsPrefs = true;
            for (String option : experimentalOptions.split(",")) {
                if (option.isEmpty()) {
                    continue;
                }

                // TODO: think about equal sign inside name or value later
                option = option.trim();
                String name = option.split("=")[0].trim();
                String value = option.split("=")[1].trim();
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    chromePrefs.put(name, Boolean.valueOf(value));
                } else if (isNumber(value)) {
                    chromePrefs.put(name, Long.valueOf(value));
                } else {
                    chromePrefs.put(name, value);
                }
            }
        }

        if (needsPrefs) {
            options.setExperimentalOption("prefs", chromePrefs);
        }

        // add all custom chrome mobileEmulation options, deviceName=Nexus 5
        Map<String, String> mobileEmulation = new HashMap<>();
        for (String option : Configuration.get(Configuration.Parameter.CHROME_MOBILE_EMULATION_OPTS).split(",")) {
            if (option.isEmpty()) {
                continue;
            }

            option = option.trim();
            String name = option.split("=")[0].trim();
            String value = option.split("=")[1].trim();
            mobileEmulation.put(name, value);
        }

        if (!mobileEmulation.isEmpty()) {
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
        }

        if (Configuration.getBoolean(Configuration.Parameter.HEADLESS)
                && driverType.equals(SpecialKeywords.DESKTOP)) {
            options.setHeadless(Configuration.getBoolean(Configuration.Parameter.HEADLESS));
        }
    }
}
