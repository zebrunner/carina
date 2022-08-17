package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.XCUITestCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringAppiumCommandExecutor;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobilePlatform;

public class IOSMiddleware extends DriverMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.MOBILE)) {
            return false;
        }

        // // for localhost, browserstack, saucelabs we do not create ios driver
        // String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        // if (!customCapabilities.isEmpty()
        // && (customCapabilities.toLowerCase().contains("localhost") ||
        // customCapabilities.toLowerCase().contains("browserstack") ||
        // customCapabilities.toLowerCase().contains("saucelabs"))) {
        // return false;
        // }
        // if (Configuration.getSeleniumUrl().contains("hub.browserstack.com")) {
        // // #1786 mobile drivers on browserstack should be started via CUSTOM - RemoteWebDriver driver
        // return false;
        // }

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

        // if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null &&
        // capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
        // .toString()
        // .equalsIgnoreCase(AutomationName.IOS_XCUI_TEST)) {
        // return true;
        // }

        return false;
    }

    @Override
    protected WebDriver getDriverByRule(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = capabilitiesMiddleware.analyze(new XCUITestCapabilities().getCapabilities(testName, capabilities));
        LOGGER.debug("IOS capabilities: {}", options);

        RemoteWebDriver driver = null;
        URL hostURL = getURL(seleniumHost);

        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if ((!customCapabilities.isEmpty() &&
                customCapabilities.toLowerCase().contains("browserstack")) ||
                Configuration.getSeleniumUrl().contains("hub.browserstack.com") ||
                Configuration.getSeleniumUrl().contains("hub-cloud.browserstack.com")) {

            LOGGER.info("Browserstack was detected! RemoteWebDriver will be used instead of IOSDriver");

            // options.setPlatformName("ANY"); // Browserstack is not understand platform name IOS
            driver = new RemoteWebDriver(hostURL, options);

        } else {
            EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(hostURL);
            driver = new IOSDriver(ce, options);

        }
        registerDevice(driver);
        return driver;
    }
}
