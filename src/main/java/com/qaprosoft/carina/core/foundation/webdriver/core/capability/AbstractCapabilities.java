package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qaprosoft.carina.core.foundation.http.HttpClient;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import net.lightbody.bmp.BrowserMobProxy;
import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        
		Proxy proxy = setupProxy();

		if (proxy != null && !BrowserType.FIREFOX.equalsIgnoreCase(Configuration.get(Parameter.BROWSER))) {
			capabilities.setCapability(CapabilityType.PROXY, proxy);
		}
		if (proxy != null && BrowserType.FIREFOX.equalsIgnoreCase(Configuration.get(Parameter.BROWSER))) {
			int port = DriverPool.getProxy().getPort();
			String proxyHost = HttpClient.getIpAddress();
			JsonObject proxyObject = new JsonParser().parse("{proxy:{" +
					"proxyType:" + "manual" + "," +
					"httpProxy:\"" + proxy + "\"," +
					"httpProxyPort:" + port + "," +
					"sslProxy:\"" + proxyHost + "\"," +
					"sslProxyPort:" + port + "," +
					"ftpProxy:\"" + proxyHost + "\"," +
					"ftpProxyPort:" + port + "," +
					"socksProxy:\"" + proxyHost + "\"," +
					"socksProxyPort:" + port +
					"}}").getAsJsonObject();

			capabilities.setCapability("requiredCapabilities", proxyObject);
			capabilities.setCapability("acceptInsecureCerts", true);
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
    
    protected Proxy setupProxy() {
		String proxyHost = Configuration.get(Parameter.PROXY_HOST);
		String proxyPort = Configuration.get(Parameter.PROXY_PORT);
		List<String> protocols = new ArrayList<>();

		if (Configuration.getBoolean(Configuration.Parameter.BROWSERMOB_PROXY)) {

			BrowserMobProxy proxy = DriverPool.startProxy();
			Integer port = proxy.getPort();

			//redefine proxy settings using custom data
			proxyHost = HttpClient.getIpAddress();
			proxyPort = port.toString();
			protocols.add("http");

			LOGGER.debug("Set http proxy settings to use BrowserMobProxy host: " + proxyHost + "; port: " + proxyPort);
		} else {
			protocols = Arrays.asList(Configuration.get(Parameter.PROXY_PROTOCOLS).split("[\\s,]+"));
			HttpClient.setupProxy();
		}

		if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
			
			org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
			String proxyAddress = String.format("%s:%s", proxyHost, proxyPort);
			
			if (protocols.contains("http")) {
				LOGGER.info("Http proxy will be set to: "+ proxyAddress);
				proxy.setHttpProxy(proxyAddress);
			}

			if (protocols.contains("https")) {
				LOGGER.info("Https proxy will be set to: "+ proxyAddress);
				proxy.setSslProxy(proxyAddress);
			}

			if (protocols.contains("ftp")) {
				LOGGER.info("FTP proxy will be set to: "+ proxyAddress);
				proxy.setFtpProxy(proxyAddress);
			}

			if (protocols.contains("socks")) {
				LOGGER.info("Socks proxy will be set to: "+ proxyAddress);
				proxy.setSocksProxy(proxyAddress);
			}
			
			return proxy;
		}
		
		return null;
    }
    
}
