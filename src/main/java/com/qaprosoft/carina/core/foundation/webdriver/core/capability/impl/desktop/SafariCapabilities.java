package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class SafariCapabilities extends AbstractCapabilities {


    public DesiredCapabilities getCapability(String testName) {
        DesiredCapabilities capabilities = DesiredCapabilities.safari();
        capabilities = initBaseCapabilities(capabilities, BrowserType.SAFARI, testName);
        return capabilities;
    }
}
