package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class ChromeCapabilities extends AbstractCapabilities<ChromeOptions> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public ChromeOptions getCapabilities(String testName, Capabilities customCapabilities) {
        ChromeOptions options = new ChromeOptions();

        if (customCapabilities != null) {
            setCapabilities(options, customCapabilities);
            return options;
        }
        setCapabilities(options, getBrowserConfigurationCapabilities(testName));
        addChromeOptions(options);

        if (Configuration.getBoolean(Configuration.Parameter.HEADLESS)) {
            options.setHeadless(true);
            LOGGER.info("Browser will be started in headless mode. VNC and Video will be disabled.");
            options.setCapability("enableVNC", false);
            options.setCapability("enableVideo", false);
        }
        options.addArguments("--start-maximized", "--ignore-ssl-errors");
        options.setAcceptInsecureCerts(true);
        return options;
    }

    private void addChromeOptions(ChromeOptions options) {
        // add default carina options and arguments
        options.addArguments("test-type");

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
            if (!"zebrunner".equalsIgnoreCase(R.CONFIG.get("capabilities.provider"))) {
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