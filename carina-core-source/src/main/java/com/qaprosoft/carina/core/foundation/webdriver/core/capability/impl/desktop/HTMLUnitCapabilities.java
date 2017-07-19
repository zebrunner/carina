package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class HTMLUnitCapabilities extends AbstractCapabilities {


    public DesiredCapabilities getCapability(String testName) {
        DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
        String platform = Configuration.get(Configuration.Parameter.PLATFORM);
        if (!platform.equals("*")) {
            capabilities.setPlatform(Platform.extractFromSysProperty(platform));
        }
        capabilities.setJavascriptEnabled(true);
        return capabilities;
    }
}
