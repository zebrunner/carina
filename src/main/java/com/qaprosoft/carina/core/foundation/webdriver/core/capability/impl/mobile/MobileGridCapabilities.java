package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import org.openqa.selenium.remote.DesiredCapabilities;

public class MobileGridCapabilities extends MobileCapabilies {


    @Override
    public DesiredCapabilities getCapability(String browserVersion, String testName) {
        if (!Configuration.get(Configuration.Parameter.MOBILE_BROWSER_NAME).isEmpty()) {
            return new MobileWebCapabilities().getCapability(true);
        } else {
            return new MobileNativeCapabilities().getCapability(true);

        }
    }


}
