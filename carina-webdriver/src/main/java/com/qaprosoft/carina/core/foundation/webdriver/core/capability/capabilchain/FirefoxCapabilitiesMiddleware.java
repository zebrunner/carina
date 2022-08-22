package com.qaprosoft.carina.core.foundation.webdriver.core.capability.capabilchain;

import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.FirefoxCapabilities;

public class FirefoxCapabilitiesMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {

        // it is not nesessary to upgrade capabilities if it is instance of ChromeOptions
        if (capabilities instanceof FirefoxOptions) {
            return false;
        }

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.DESKTOP)) {
            return false;
        }

        if (Browser.FIREFOX.is(capabilities)) {
            return true;
        }
        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        return new FirefoxCapabilities().getCapabilities(capabilities);
    }
}
