package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import java.util.ArrayList;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class FirefoxCapabilities extends AbstractCapabilities {

    private static ArrayList<Integer> firefoxPorts = new ArrayList<Integer>();

    public DesiredCapabilities getCapability(String testName) {

        FirefoxProfile profile = getDefaultFirefoxProfile();
        return getCapability(testName, profile);
    }

    public DesiredCapabilities getCapability(String testName, FirefoxProfile profile) {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities = initBaseCapabilities(capabilities, BrowserType.FIREFOX, testName);
        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        return capabilities;
    }

    public FirefoxProfile getDefaultFirefoxProfile() {
        FirefoxProfile profile = new FirefoxProfile();

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
        LOGGER.debug(firefoxPorts);

        profile.setPreference(FirefoxProfile.PORT_PREFERENCE, newPort);
        LOGGER.debug("FireFox profile will use '" + newPort + "' port number.");

        profile.setPreference("dom.max_chrome_script_run_time", 0);
        profile.setPreference("dom.max_script_run_time", 0);
        profile.setEnableNativeEvents(true);

		if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD) && !(Configuration.isNull(Configuration.Parameter.AUTO_DOWNLOAD_APPS)
				|| "".equals(Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS))))
		{
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.download.dir", ReportContext.getArtifactsFolder().getAbsolutePath());
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS));
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            profile.setPreference("browser.download.saveLinkAsFilenameTimeout", 1);
            profile.setPreference("pdfjs.disabled", true);
            profile.setPreference("plugin.scan.plid.all", false);
            profile.setPreference("plugin.scan.Acrobat", "99.0");
        }
		else if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD) && Configuration.isNull(Configuration.Parameter.AUTO_DOWNLOAD_APPS)
				|| "".equals(Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_APPS)))
		{
			LOGGER.warn(
					"If you want to enable auto-download for FF please specify '" + Configuration.Parameter.AUTO_DOWNLOAD_APPS.getKey() + "' param");
		}

        profile.setAcceptUntrustedCertificates(true);
        profile.setAssumeUntrustedCertificateIssuer(true);
        
        return profile;
    }
}
