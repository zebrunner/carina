package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;


import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesLoder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileGridCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileNativeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileWebCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class MobileFactory extends AbstractFactory {

    @SuppressWarnings("rawtypes")
    @Override
    public WebDriver create(String testName, Device device) {

        String selenium = Configuration.get(Configuration.Parameter.SELENIUM_HOST);
        String driverType = Configuration.get(Configuration.Parameter.DRIVER_TYPE);
        String mobile_platform_name = Configuration.get(Configuration.Parameter.MOBILE_PLATFORM_NAME);

        RemoteWebDriver driver = null;
        DesiredCapabilities capabilities = getCapabilities(testName);
        try {
            if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE_GRID)) {
                driver = new RemoteWebDriver(new URL(selenium), capabilities);
            } else if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE_POOL) || driverType.equalsIgnoreCase(SpecialKeywords.MOBILE)) {
                if (mobile_platform_name.toLowerCase().equalsIgnoreCase(SpecialKeywords.ANDROID))
                    driver = new AndroidDriver(new URL(selenium), capabilities);
                else if (mobile_platform_name.toLowerCase().equalsIgnoreCase(SpecialKeywords.IOS)) {
                    driver = new IOSDriver(new URL(selenium), capabilities);
                }
            } else if (driverType.equalsIgnoreCase(SpecialKeywords.CUSTOM)) {
                driver = new RemoteWebDriver(new URL(selenium), capabilities);
            } else {
                throw new RuntimeException("Unsupported browser");
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed selenium URL! " + e.getMessage(), e);
        }

        return driver;
    }

    public DesiredCapabilities getCapabilities(String testName) {
        String customCapabilities = Configuration.get(Parameter.CUSTOM_CAPABILITIES);
        if (!customCapabilities.isEmpty()) {
            return new CapabilitiesLoder().loadCapabilities(customCapabilities);
        } else {
            String driverType = Configuration.get(Configuration.Parameter.DRIVER_TYPE);

            if (driverType.equalsIgnoreCase(SpecialKeywords.MOBILE_GRID)) {
                return new MobileGridCapabilities().getCapability(testName);
            } else if ((driverType.equalsIgnoreCase(SpecialKeywords.MOBILE_POOL)
                    || driverType.equalsIgnoreCase(SpecialKeywords.MOBILE)
                    && !Configuration.get(Configuration.Parameter.BROWSER).isEmpty())) {
                return new MobileWebCapabilities().getCapability(testName);
            } else if ((driverType.equalsIgnoreCase(SpecialKeywords.MOBILE_POOL)
                    || driverType.equalsIgnoreCase(SpecialKeywords.MOBILE)
                    && Configuration.get(Configuration.Parameter.BROWSER).isEmpty())) {
                return new MobileNativeCapabilities().getCapability(testName);
            } else {
                throw new RuntimeException("Unsupported driver type:" + driverType);
            }
        }

    }


}
