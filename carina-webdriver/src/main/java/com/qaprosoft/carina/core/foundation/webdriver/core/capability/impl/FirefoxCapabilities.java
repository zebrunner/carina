package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstactCapabilities;

public class FirefoxCapabilities extends AbstactCapabilities<FirefoxOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ArrayList<Integer> firefoxPorts = new ArrayList<Integer>();

    @Override
    public FirefoxOptions getCapabilities() {
        FirefoxOptions options = new FirefoxOptions();
        if (isProxyConfigurationAvailable()) {
            options.setCapability(CapabilityType.PROXY, setupProxy());
        }
        setCapabilitiesSafe(options, getConfigurationCapabilities());
        addFirefoxOptions(options);
        options.setAcceptInsecureCerts(true);

        if (Configuration.getBoolean(Configuration.Parameter.HEADLESS)) {
            options.setHeadless(true);
            LOGGER.info("Browser will be started in headless mode. VNC and Video will be disabled.");
            options.setCapability("enableVNC", false);
            options.setCapability("enableVideo", false);
        }
        return options;
    }

    @Override
    public FirefoxOptions createCapabilitiesFromCustom(Capabilities customCapabilities) {
        FirefoxOptions options = new FirefoxOptions();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.setCapability(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    @Override
    public FirefoxOptions getCapabilitiesWithCustom(Capabilities customCapabilities) {
        FirefoxOptions options = getCapabilities();
        if (customCapabilities != null) {
            for (String capabilityName : customCapabilities.getCapabilityNames()) {
                options.setCapability(capabilityName, customCapabilities.getCapability(capabilityName));
            }
        }
        return options;
    }

    private void setCapabilitiesSafe(FirefoxOptions options, Capabilities capabilities) {
        for (String capabilityName : capabilities.getCapabilityNames()) {
            options.setCapability(capabilityName, capabilities.getCapability(capabilityName));
        }
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
    }

    /**
     * Generate default default Carina FirefoxProfile.
     *
     * @return Firefox profile.
     */
    public FirefoxProfile getDefaultFirefoxProfile() {
        // keep it public to be able to get default and override on client layer

        FirefoxProfile profile = new FirefoxProfile();

        // update browser language
        String browserLang = Configuration.get(Configuration.Parameter.BROWSER_LANGUAGE);
        if (!browserLang.isEmpty()) {
            LOGGER.info("Set Firefox lanaguage to: {}", browserLang);
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
            profile.setPreference("browser.download.dir", ReportContext.getArtifactsFolder().getAbsolutePath());
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS));
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            profile.setPreference("browser.download.saveLinkAsFilenameTimeout", 1);
            profile.setPreference("pdfjs.disabled", true);
            profile.setPreference("plugin.scan.plid.all", false);
            profile.setPreference("plugin.scan.Acrobat", "99.0");
        } else if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD) && Configuration.isNull(Configuration.Parameter.AUTO_DOWNLOAD_APPS)
                || "".equals(Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS))) {
            LOGGER.warn("If you want to enable auto-download for FF please specify '{}' param", Configuration.Parameter.AUTO_DOWNLOAD_APPS.getKey());
        }

        profile.setAcceptUntrustedCertificates(true);
        profile.setAssumeUntrustedCertificateIssuer(true);

        // TODO: implement support of custom args if any
        return profile;
    }

}
