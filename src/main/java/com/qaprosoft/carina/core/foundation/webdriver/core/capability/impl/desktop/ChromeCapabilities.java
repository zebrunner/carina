package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import java.util.Arrays;
import java.util.HashMap;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class ChromeCapabilities extends AbstractCapabilities
{
	public DesiredCapabilities getCapability(String testName)
	{
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities = initBaseCapabilities(capabilities, BrowserType.CHROME, testName);
		capabilities.setCapability("chrome.switches", Arrays.asList("--start-maximized"));
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("test-type");

		if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD))
		{
			HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
			chromePrefs.put("download.prompt_for_download", false);
			chromePrefs.put("download.default_directory", ReportContext.getArtifactsFolder().getAbsolutePath());
			chromePrefs.put("plugins.always_open_pdf_externally", true);
			options.setExperimentalOption("prefs", chromePrefs);
		}

		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return capabilities;
	}
}
