package com.qaprosoft.carina.core.foundation.webdriver.core.capability.capabilchain;

import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;

public class ZebrunnerPreMiddleware extends CapabilitiesMiddleware {

    private String testName;

    public ZebrunnerPreMiddleware(String testName) {
        this.testName = testName;
    }

    @Override
    protected boolean isDetected(Capabilities capabilities) {
        if (Configuration.getSeleniumUrl().contains("zebrunner")) {
            return true;
        }
        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        if (Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.DESKTOP)) {
            if (!IDriverPool.DEFAULT.equalsIgnoreCase(testName)) {
                // #1573: remove "default" driver name capability registration
                // todo investigate to wrap capabilities specific to zebrunner in ???:options
                capabilities.setCapability("name", testName);
            }
        }
        return capabilities;
    }
}
