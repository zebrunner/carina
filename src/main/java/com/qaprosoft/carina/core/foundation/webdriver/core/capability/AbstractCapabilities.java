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
				org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
				proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
				capabilities.setCapability(CapabilityType.PROXY, proxy);
				LOGGER.info("WebDriver/Browser will use proxy: " + PROXY);
			} else {
				Assert.fail("Invalid proxy is specified: " + PROXY);
			}
		}
        
        return capabilities;
    }
    
}
