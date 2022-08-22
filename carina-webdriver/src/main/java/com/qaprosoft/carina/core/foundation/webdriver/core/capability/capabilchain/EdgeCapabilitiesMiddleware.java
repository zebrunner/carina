package com.qaprosoft.carina.core.foundation.webdriver.core.capability.capabilchain;

import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.EdgeCapabilities;

public class EdgeCapabilitiesMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {

        // it is not necessary to upgrade capabilities if it is instance of ChromiumOptions and contains ms:edgeOptions
        if (capabilities instanceof ChromiumOptions<?> &&
                capabilities.getCapability("ms:edgeOptions") != null) {
            return false;
        }

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.DESKTOP)) {
            return false;
        }

        if (Browser.EDGE.is(capabilities)) {
            return true;
        }

        // browserstack-specific
        if ("Edge".equalsIgnoreCase(capabilities.getBrowserName())) {
            return true;
        }

        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        return new EdgeCapabilities().getCapabilities(capabilities);
    }
}
