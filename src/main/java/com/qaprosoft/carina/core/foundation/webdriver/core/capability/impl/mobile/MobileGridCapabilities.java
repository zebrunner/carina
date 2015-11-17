package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class MobileGridCapabilities extends MobileCapabilies {


    @Override
    public DesiredCapabilities getCapability(String testName) {
        if (!Configuration.get(Configuration.Parameter.BROWSER).isEmpty()) {
            return new MobileWebCapabilities().getCapability(true);
        } else {
            return new MobileNativeCapabilities().getCapability(true);

        }
    }


}
