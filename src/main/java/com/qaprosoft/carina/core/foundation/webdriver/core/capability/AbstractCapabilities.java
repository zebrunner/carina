package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public abstract class AbstractCapabilities {
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
        
        
		String proxyHost = Configuration.get(Parameter.PROXY_HOST);
		String proxyPort = Configuration.get(Parameter.PROXY_PORT);
		
		if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("http.proxyPort", proxyPort);

			System.setProperty("https.proxyHost", proxyHost);
			System.setProperty("https.proxyPort", proxyPort);

			System.setProperty("ftp.proxyHost", proxyHost);
			System.setProperty("ftp.proxyPort", proxyPort);
			
			System.setProperty("socksProxyHost", proxyHost);
			System.setProperty("socksProxyPort", proxyPort);
			
			org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
			String proxyAddress = String.format("%s:%s", proxyHost, proxyPort);
			proxy.setHttpProxy(proxyAddress);
			proxy.setSslProxy(proxyAddress);
			proxy.setFtpProxy(proxyAddress);
			capabilities.setCapability(CapabilityType.PROXY, proxy);

			LOGGER.info(String.format("WebDriver will use http/https/ftp proxies: %s:%s", proxyHost, proxyPort));
		}
		
		//handle variant with extra capabilities from external property file
    	DesiredCapabilities extraCapabilities = getExtraCapabilities(); 	
    			
    	if (extraCapabilities != null) {
    		capabilities.merge(extraCapabilities);
    	}
		
        return capabilities;
    }
    
    protected DesiredCapabilities getExtraCapabilities() {
		//handle variant with extra capabilities from external property file
    	String extraCapabilitiesFile = Configuration.get(Parameter.EXTRA_CAPABILITIES);
    	DesiredCapabilities extraCapabilities = null;
    	if (!extraCapabilitiesFile.isEmpty()) {
    		LOGGER.info("Append extra Capabilities from '" + extraCapabilitiesFile + "' to desired capabilities");
    		extraCapabilities = new CapabilitiesLoder().loadCapabilities(extraCapabilitiesFile);
    	}
    	
    	return extraCapabilities;
    }
    
}
