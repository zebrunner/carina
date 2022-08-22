package com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware;

import java.util.Objects;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.UIAutomator2Capabilities;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;

import io.appium.java_client.android.options.UiAutomator2Options;

public class UIAutomaror2CapabilitiesMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {

        if (capabilities instanceof UiAutomator2Options) {
            return false;
        }

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.MOBILE)) {
            return false;
        }

        if (Platform.ANDROID.is(capabilities.getPlatformName())) {
            return true;
        }

        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        return new UIAutomator2Capabilities().getCapabilities(capabilities);
    }
}
