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
import java.util.ArrayList;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.net.PortProber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.webdriver.core.capability.AbstractCapabilities;

public class FirefoxCapabilities extends AbstractCapabilities<FirefoxOptions> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ArrayList<Integer> firefoxPorts = new ArrayList<>();

    /**
     * Generate FirefoxOptions for Firefox with default Carina FirefoxProfile.
     * 
     * @return Firefox capabilities.
     */
    @Override
    public FirefoxOptions getCapability(String testName) {
        FirefoxOptions capabilities = new FirefoxOptions();
        initBaseCapabilities(capabilities, testName);
        addFirefoxOptions(capabilities);
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("media.eme.enabled", true);
        profile.setPreference("media.gmp-manager.updateEnabled", true);

        capabilities.setProfile(profile);
        return capabilities;
    }

    /**
     * Generate FirefoxOptions for Firefox with custom FirefoxProfile.
     *
     * @param testName - String.
     * @param profile - FirefoxProfile.
     * @return FirefoxOptions
     */
    public FirefoxOptions getCapability(String testName, FirefoxProfile profile) {
        FirefoxOptions capabilities = new FirefoxOptions();
        initBaseCapabilities(capabilities, testName);
        addFirefoxOptions(capabilities);
        capabilities.setProfile(profile);
        return capabilities;
    }

    private void addFirefoxOptions(FirefoxOptions options) {
        FirefoxProfile profile = getDefaultFirefoxProfile();
        options.setProfile(profile);

        // add all custom firefox args
        for (String arg : Configuration.get(Configuration.Parameter.FIREFOX_ARGS).split(",")) {
            if (arg.isEmpty()) {
                continue;
            }
            options.addArguments(arg.trim());
        }
        // add all custom firefox preferences
        for (String preference : Configuration.get(Configuration.Parameter.FIREFOX_PREFERENCES).split(",")) {
            if (preference.isEmpty()) {
                continue;
            }
            // TODO: think about equal sign inside name or value later
            preference = preference.trim();
            String name = preference.split("=")[0].trim();
            String value = preference.split("=")[1].trim();
            // TODO: test approach with numbers
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                options.addPreference(name, Boolean.valueOf(value));
            } else {
                options.addPreference(name, value);
            }
        }

        String driverType = Configuration.getDriverType();
        if (Configuration.getBoolean(Configuration.Parameter.HEADLESS)
                && driverType.equals(SpecialKeywords.DESKTOP)) {
            options.setHeadless(Configuration.getBoolean(Configuration.Parameter.HEADLESS));
        }

    }


    /**
     * Generate default default Carina FirefoxProfile.
     *
     * @return Firefox profile.
     */
    // keep it public to be able to get default and override on client layerI
    public FirefoxProfile getDefaultFirefoxProfile() {
        FirefoxProfile profile = new FirefoxProfile();

        // update browser language
        String browserLang = Configuration.get(Configuration.Parameter.BROWSER_LANGUAGE);
        if (!browserLang.isEmpty()) {
            LOGGER.info("Set Firefox lanaguage to: {}, ", browserLang);
            profile.setPreference("intl.accept_languages", browserLang);
        }

        boolean generated = false;
        int newPort = 7055;
        int i = 100;
        while (!generated && (--i > 0)) {
            newPort = PortProber.findFreePort();
            generated = firefoxPorts.add(newPort);
        }
        if (!generated) {
            newPort = 7055;
        }
        if (firefoxPorts.size() > 20) {
            firefoxPorts.remove(0);
        }

        profile.setPreference("webdriver_firefox_port", newPort);
        LOGGER.debug("FireFox profile will use '{}' port number.", newPort);

        profile.setPreference("dom.max_chrome_script_run_time", 0);
        profile.setPreference("dom.max_script_run_time", 0);

        if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD) && !(Configuration.isNull(Configuration.Parameter.AUTO_DOWNLOAD_APPS)
                || "".equals(Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS)))) {
            profile.setPreference("browser.download.folderList", 2);
            if (!"zebrunner".equalsIgnoreCase(R.CONFIG.get(SpecialKeywords.PROVIDER))) {
                // don't override auto download dir for Zebrunner Selenium Grid (Selenoid)
                profile.setPreference("browser.download.dir", ReportContext.getArtifactsFolder().getAbsolutePath());
            }
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS));
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            profile.setPreference("browser.download.saveLinkAsFilenameTimeout", 1);
            profile.setPreference("pdfjs.disabled", true);
            profile.setPreference("plugin.scan.plid.all", false);
            profile.setPreference("plugin.scan.Acrobat", "99.0");
        } else if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD) && Configuration.isNull(Configuration.Parameter.AUTO_DOWNLOAD_APPS)
                || "".equals(Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS))) {
            LOGGER.warn(
                    "If you want to enable auto-download for FF please specify '{}' param", Configuration.Parameter.AUTO_DOWNLOAD_APPS.getKey());
        }

        profile.setAcceptUntrustedCertificates(true);
        profile.setAssumeUntrustedCertificateIssuer(true);

        // TODO: implement support of custom args if any
        return profile;
    }
}
