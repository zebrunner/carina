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

import com.qaprosoft.carina.commons.models.RemoteDevice;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesLoder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileCapabilies;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;

public class MobileFactory extends AbstractFactory {

	@Override
    public WebDriver create(String name, Device device, DesiredCapabilities capabilities, String seleniumHost) {

		if (seleniumHost == null) {
			seleniumHost = Configuration.get(Configuration.Parameter.SELENIUM_HOST);
		}
        String driverType = Configuration.getDriverType();
        String mobilePlatformName = Configuration.getPlatform();

        LOGGER.debug("selenium: " + seleniumHost);
        
        RemoteWebDriver driver = null;
		if (capabilities == null) {
			capabilities = getCapabilities(name, device);
		}
        try {
			if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE)) {
				if (mobilePlatformName.toLowerCase().equalsIgnoreCase(SpecialKeywords.ANDROID)) {
					driver = new AndroidDriver<AndroidElement>(new URL(seleniumHost), capabilities);
					
				} else if (mobilePlatformName.toLowerCase().equalsIgnoreCase(SpecialKeywords.IOS)) {
					driver = new IOSDriver<IOSElement>(new URL(seleniumHost), capabilities);
				}
				
				if (device.isNull()) 
				{
					//TODO: double check that local run with direct appium works fine
					RemoteDevice remoteDevice = getDeviceInfo(seleniumHost, driver.getSessionId().toString());
					if (remoteDevice != null) {
						device = new Device(remoteDevice);
					}
					else {
						device = new Device(driver.getCapabilities());
					}
					
					boolean stfEnabled = R.CONFIG.getBoolean(SpecialKeywords.CAPABILITIES + "." + SpecialKeywords.STF_ENABLED);
					if (stfEnabled) {
						device.connectRemote();
					}
					DevicePool.registerDevice(device);
				}
				// will be performed just in case uninstall_related_apps flag marked as true
				device.uninstallRelatedApps();
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
			capabilities = new MobileCapabilies().getCapability(name);
        }
        
        if (!device.isNull()) {
        	capabilities.setCapability("udid", device.getUdid());
			// disable Selenium Hum <-> STF verification as device already
			// connected by this test (restart driver on the same device is
			// invoked)
       		capabilities.setCapability("STF_ENABLED", "false");

        }
        
		return capabilities;
    }
    
    private RemoteDevice getDeviceInfo(String seleniumHost, String sessionId)
	{
    	RemoteDevice device = null;
		try
		{
			HttpClient client = HttpClientBuilder.create().build();
	        HttpGet request = new HttpGet(seleniumHost.split("wd")[0] + "grid/admin/DeviceInfo?session=" + sessionId);
	        HttpResponse response = client.execute(request);
	        
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	        device =  mapper.readValue(response.getEntity().getContent(), RemoteDevice.class);
		}
		catch (Exception e) 
		{
			LOGGER.error("Unable to get device info: " + e.getMessage());
		}
		return device;
	}
}
