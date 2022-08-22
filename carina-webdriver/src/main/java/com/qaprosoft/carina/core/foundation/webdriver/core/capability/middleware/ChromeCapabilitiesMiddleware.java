package com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware;

import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.ChromeCapabilities;

public class ChromeCapabilitiesMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {

        // it is not nesessary to upgrade capabilities if it is instance of ChromeOptions
        if (capabilities instanceof ChromeOptions) {
            return false;
        }

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.DESKTOP)) {
            return false;
        }

        if (Browser.CHROME.browserName().equalsIgnoreCase(capabilities.getBrowserName())) {
            return true;
        }
        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        return new ChromeCapabilities().getCapabilities(capabilities);
    }
}
