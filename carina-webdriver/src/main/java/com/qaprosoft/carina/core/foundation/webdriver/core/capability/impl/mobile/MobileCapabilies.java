package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;


import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public abstract class MobileCapabilies extends AbstractCapabilities {


    public DesiredCapabilities getMobileCapabilities(boolean gridMode, String platform, String platformVersion, String deviceName,
                                                        String automationName, String commandTimeout, String browserName, String app, String appActivity, String appPackage) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        capabilities.setCapability("newCommandTimeout", commandTimeout);

        if (gridMode) {
        	//do not set automationName for the grid
            capabilities.setCapability("platform", platform);
            capabilities.setCapability("version", platformVersion);
            capabilities.setCapability("browserName", deviceName);
            capabilities.setCapability("deviceName", "*");
        } else {
            capabilities.setCapability("automationName", automationName);
            capabilities.setCapability("platformName", platform);
            capabilities.setCapability("platformVersion", platformVersion);
            if (deviceName != null) {
                capabilities.setCapability("deviceName", deviceName);
            }
        }

        if (browserName != null)
        {
            capabilities.setCapability("browserName", browserName);
            if (gridMode && platform.equalsIgnoreCase("iOS")) {
                capabilities.setCapability("platform", "MAC");
            }
        } else {
            capabilities.setCapability("browserName", "");
            capabilities.setCapability("app", app);
            if (appActivity != null)
                capabilities.setCapability("appActivity", appActivity);

            if (appPackage != null)
                capabilities.setCapability("appPackage", appPackage);
        }
        
        
		// add capabilities based on dynamic _config.properties variables
		capabilities = initCustomCapabilities(capabilities);

		//handle variant with extra capabilities from external property file
    	DesiredCapabilities extraCapabilities = getExtraCapabilities(); 	
    			
    	if (extraCapabilities != null) {
    		capabilities.merge(extraCapabilities);
    	}
    	
        return capabilities;
    }

}
