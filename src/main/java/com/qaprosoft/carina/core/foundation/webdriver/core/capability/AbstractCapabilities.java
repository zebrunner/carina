package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
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
        return capabilities;
    }


}
