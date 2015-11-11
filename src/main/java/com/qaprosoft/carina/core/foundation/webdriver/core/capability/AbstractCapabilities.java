package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class AbstractCapabilities {

    protected static final Logger LOGGER = Logger.getLogger(AbstractCapabilities.class);

    public abstract DesiredCapabilities getCapability(String browserVersion, String testName);


    protected DesiredCapabilities initBaseCapabilities(DesiredCapabilities capabilities, String browser, String browserVersion, String testName) {
        String platform = Configuration.get(Configuration.Parameter.PLATFORM);
        if (!platform.equals("*")) {
            capabilities.setPlatform(Platform.extractFromSysProperty(platform));
        }

        capabilities.setBrowserName(browser);
        capabilities.setVersion(browserVersion);
        capabilities.setCapability("name", testName);
        return capabilities;
    }


}
