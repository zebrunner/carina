package com.qaprosoft.carina.core.foundation.webdriver.core.capability.middleware;

import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.XCUITestCapabilities;

import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.remote.MobilePlatform;

public class XCUITestCapabilitiesMiddleware extends CapabilitiesMiddleware {

    @Override
    protected boolean isDetected(Capabilities capabilities) {
        if (capabilities instanceof XCUITestOptions) {
            return false;
        }

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.MOBILE)) {
            return false;
        }

        // use safari factory for testing
        if (Browser.SAFARI.browserName().equalsIgnoreCase(capabilities.getBrowserName())) {
            return false;
        }

        if (Platform.IOS.is(capabilities.getPlatformName())) {
            return true;
        }

        if (capabilities.getCapability("platformName") != null &&
                MobilePlatform.TVOS.equalsIgnoreCase(capabilities.getCapability("platformName").toString())) {
            return true;
        }

        return false;
    }

    @Override
    protected MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities) {
        return new XCUITestCapabilities().getCapabilities(capabilities);
    }
}
