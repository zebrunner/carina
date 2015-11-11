package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import org.openqa.selenium.remote.DesiredCapabilities;

public class MobileNativeCapabilities extends MobileCapabilies {


    public DesiredCapabilities getCapability(String browserVersion, String testName) {
        return getCapability(false);
    }

    public DesiredCapabilities getCapability(boolean isGrid) {
        DesiredCapabilities capabilities = getMobileCapabilities(isGrid, Configuration.get(Configuration.Parameter.MOBILE_PLATFORM_NAME),
                Configuration.get(Configuration.Parameter.MOBILE_PLATFORM_VERSION), Configuration.get(Configuration.Parameter.MOBILE_DEVICE_NAME),
                Configuration.get(Configuration.Parameter.MOBILE_AUTOMATION_NAME), Configuration.get(Configuration.Parameter.MOBILE_NEW_COMMAND_TIMEOUT),
                null, Configuration.get(Configuration.Parameter.MOBILE_APP), Configuration.get(Configuration.Parameter.MOBILE_APP_ACTIVITY),
                Configuration.get(Configuration.Parameter.MOBILE_APP_PACKAGE));
        return capabilities;
    }
}
