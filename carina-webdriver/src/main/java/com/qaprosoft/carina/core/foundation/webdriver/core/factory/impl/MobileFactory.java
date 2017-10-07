package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;


import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesLoder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileGridCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileNativeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileWebCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.zafira.models.stf.STFDevice;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;

public class MobileFactory extends AbstractFactory {

    @Override
    public WebDriver create(String name, Device device) {

        String seleniumHost = Configuration.get(Configuration.Parameter.SELENIUM_HOST);
        String driverType = Configuration.get(Configuration.Parameter.DRIVER_TYPE);
        String mobilePlatformName = Configuration.get(Configuration.Parameter.MOBILE_PLATFORM_NAME);

        if (device != null && !device.isNull()) {
        	seleniumHost = device.getSeleniumServer();
        	LOGGER.debug("selenium_host: " + seleniumHost);
        }
        
        LOGGER.debug("selenium: " + seleniumHost);
        
        RemoteWebDriver driver = null;
        DesiredCapabilities capabilities = getCapabilities(name, device);
        try {
			if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE)
					|| driverType.equalsIgnoreCase(SpecialKeywords.MOBILE_GRID)) {
				if (mobilePlatformName.toLowerCase().equalsIgnoreCase(SpecialKeywords.ANDROID)) {
					// handler in case app was installed via adb and there is no
					// need to sign app using appium
					if (Configuration.getBoolean(Configuration.Parameter.MOBILE_APP_INSTALL)
							&& !Configuration.getBoolean(Configuration.Parameter.MOBILE_APP_UNINSTALL)) {
						capabilities.setCapability("app", "");
					}
					LOGGER.info("capabilities.getPlatform(): " + capabilities.getPlatform());
					LOGGER.info("platformName: " +  capabilities.getCapability("platformName"));
					LOGGER.info("platform: " +  capabilities.getCapability("platform"));
					driver = new AndroidDriver<AndroidElement>(new URL(seleniumHost), capabilities);
					
					if (device.isNull()) 
					{
						STFDevice info = getDeviceInfo(seleniumHost, driver.getSessionId().toString());
						if(info != null)
						{
							LOGGER.info("Selenium hub+stf feature is enabled.");
							device = new Device(info.getModel(), "phone", info.getPlatform(), info.getVersion(), info.getSerial(), seleniumHost, (String)info.getRemoteConnectUrl());
							LOGGER.info("Detected device by uuid from driver capabilities: " + device.getName());
							device.connectRemote();
							DevicePool.registerDevice(device);
						}
					}
				} else if (mobilePlatformName.toLowerCase().equalsIgnoreCase(SpecialKeywords.IOS)) {
					driver = new IOSDriver<IOSElement>(new URL(seleniumHost), capabilities);
				}
			} else if (driverType.equalsIgnoreCase(SpecialKeywords.CUSTOM)) {
                driver = new RemoteWebDriver(new URL(seleniumHost), capabilities);
            } else {
                throw new RuntimeException("Unsupported browser");
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed selenium URL! " + e.getMessage(), e);
        }

        
    	if (driver == null ) {
    		Assert.fail("Unable to initialize driver: " + name + "!");
    	}
    	
        return driver;
    }

    public DesiredCapabilities getCapabilities(String name, Device device) {
        String customCapabilities = Configuration.get(Parameter.CUSTOM_CAPABILITIES);
        DesiredCapabilities capabilities = new DesiredCapabilities(); 
        if (!customCapabilities.isEmpty()) {
        	capabilities = new CapabilitiesLoder().loadCapabilities(customCapabilities);
        } else {
            String driverType = Configuration.get(Configuration.Parameter.DRIVER_TYPE);

            if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE_GRID)) {
            	capabilities = new MobileGridCapabilities().getCapability(name);
            } else if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE)
                    && !Configuration.get(Configuration.Parameter.BROWSER).isEmpty()) {
            	capabilities = new MobileWebCapabilities().getCapability(name);
            } else if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE)
                    && Configuration.get(Configuration.Parameter.BROWSER).isEmpty()) {
            	capabilities = new MobileNativeCapabilities().getCapability(name);
            } else {
                throw new RuntimeException("Unsupported driver type:" + driverType);
            }
        }
        
		return capabilities;
    }
    
    private STFDevice getDeviceInfo(String seleniumHost, String sessionId)
	{
		STFDevice device = null;
		try
		{
			HttpClient client = HttpClientBuilder.create().build();
	        HttpGet request = new HttpGet(seleniumHost.split("wd")[0] + "grid/admin/DeviceInfo?session=" + sessionId);
	        HttpResponse response = client.execute(request);
	        
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	        device =  mapper.readValue(response.getEntity().getContent(), STFDevice.class);
		}
		catch (Exception e) 
		{
			LOGGER.error("Unable to get device info: " + e.getMessage());
		}
		return device;
	}
}
