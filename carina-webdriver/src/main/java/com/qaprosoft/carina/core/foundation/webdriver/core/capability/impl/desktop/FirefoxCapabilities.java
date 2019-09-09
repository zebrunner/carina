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

import java.util.ArrayList;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class FirefoxCapabilities extends AbstractCapabilities {
    private static final Logger LOGGER = Logger.getLogger(FirefoxCapabilities.class);

    private static ArrayList<Integer> firefoxPorts = new ArrayList<Integer>();

    public DesiredCapabilities getCapability(String testName) {

        FirefoxProfile profile = getDefaultFirefoxProfile();
        return getCapability(testName, profile);
    }

    public DesiredCapabilities getCapability(String testName, FirefoxProfile profile) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities = initBaseCapabilities(capabilities, BrowserType.FIREFOX, testName);
        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        FirefoxOptions options = new FirefoxOptions().setProfile(profile);
        capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS,options);
        return capabilities;
    }

    public FirefoxProfile getDefaultFirefoxProfile() {
        FirefoxProfile profile = new FirefoxProfile();
        
        //update browser language
        String browserLang = Configuration.get(Parameter.BROWSER_LANGUAGE); 
        if (!browserLang.isEmpty()) {
        	LOGGER.info("Set Firefox lanaguage to: " + browserLang);
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

        profile.setPreference(FirefoxProfile.PORT_PREFERENCE, newPort);
        LOGGER.debug("FireFox profile will use '" + newPort + "' port number.");

        profile.setPreference("dom.max_chrome_script_run_time", 0);
        profile.setPreference("dom.max_script_run_time", 0);

        if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD) && !(Configuration.isNull(Configuration.Parameter.AUTO_DOWNLOAD_APPS)
                || "".equals(Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS)))) {
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.download.dir", ReportContext.getArtifactsFolder().getAbsolutePath());
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS));
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            profile.setPreference("browser.download.saveLinkAsFilenameTimeout", 1);
            profile.setPreference("pdfjs.disabled", true);
            profile.setPreference("plugin.scan.plid.all", false);
            profile.setPreference("plugin.scan.Acrobat", "99.0");
        } else if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD) && Configuration.isNull(Configuration.Parameter.AUTO_DOWNLOAD_APPS)
                || "".equals(Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS))) {
            LOGGER.warn(
                    "If you want to enable auto-download for FF please specify '" + Configuration.Parameter.AUTO_DOWNLOAD_APPS.getKey() + "' param");
        }

        profile.setAcceptUntrustedCertificates(true);
        profile.setAssumeUntrustedCertificateIssuer(true);

        return profile;
    }
}
