package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public abstract class AbstractCapabilities {
	private static final String PROXY_PATTERN = ".+:.+:.+";

    protected static final Logger LOGGER = Logger.getLogger(AbstractCapabilities.class);

    public abstract DesiredCapabilities getCapability(String testName);


    protected DesiredCapabilities initBaseCapabilities(DesiredCapabilities capabilities, String browser, String testName) {
        String platform = Configuration.get(Configuration.Parameter.PLATFORM);
        if (!platform.equals("*")) {
            capabilities.setPlatform(Platform.extractFromSysProperty(platform));
        }
        
        
        capabilities.setBrowserName(browser);
        capabilities.setVersion(Configuration.get(Parameter.BROWSER_VERSION));
        capabilities.setCapability("name", testName);
        
        
		if (!Configuration.isNull(Parameter.PROXY) && !Configuration.get(Parameter.PROXY).isEmpty()) {
			String PROXY = Configuration.get(Parameter.PROXY);
			if (Pattern.matches(PROXY_PATTERN, Configuration.get(Parameter.PROXY))) {

				String[] proxies = Configuration.get(Parameter.PROXY).split(":");
				System.setProperty(String.format("%s.proxyHost", proxies[0]), proxies[1]);
				System.setProperty(String.format("%s.proxyPort", proxies[0]), proxies[2]);
				LOGGER.info(
						String.format("WebDriver will use proxy: %s://%s:%s", proxies[0], proxies[1], proxies[2]));

				org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
				switch (proxies[0]) {
				case "http":
					proxy.setHttpProxy(String.format("%s:%s", proxies[1], proxies[2]));
					break;
				case "https":
					proxy.setSslProxy(String.format("%s:%s", proxies[1], proxies[2]));
					break;
				case "ftp":
					proxy.setFtpProxy(String.format("%s:%s", proxies[1], proxies[2]));
					break;
				default:
					Assert.fail("Unsupported proxy protocol specified: " + proxies[0]);
				}
				// proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
				capabilities.setCapability(CapabilityType.PROXY, proxy);
			} else {
				Assert.fail("Invalid proxy is specified: " + PROXY);
			}
		}
        
        return capabilities;
    }
    
}
