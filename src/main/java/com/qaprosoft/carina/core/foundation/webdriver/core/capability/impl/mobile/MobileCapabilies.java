package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;


import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class MobileCapabilies extends AbstractCapabilities {


    public DesiredCapabilities getMobileCapabilities(boolean gridMode, String platform, String platformVersion, String deviceName,
                                                        String automationName, String commandTimeout, String browserName, String app, String appActivity, String appPackage) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", platform);
        capabilities.setCapability("platformVersion", platformVersion);
        if (deviceName != null)
            capabilities.setCapability("deviceName", deviceName);

        capabilities.setCapability("automationName", automationName);
        capabilities.setCapability("newCommandTimeout", commandTimeout);


        if (gridMode) {
            capabilities.setCapability("platform", platform);
            capabilities.setCapability("version", platformVersion);
            capabilities.setCapability("browserName", deviceName);
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
        
		//handle variant with extra capabilities from external property file
    	DesiredCapabilities extraCapabilities = getExtraCapabilities(); 	
    			
    	if (extraCapabilities != null) {
    		capabilities.merge(extraCapabilities);
    	}
    	

        return capabilities;
    }

}
