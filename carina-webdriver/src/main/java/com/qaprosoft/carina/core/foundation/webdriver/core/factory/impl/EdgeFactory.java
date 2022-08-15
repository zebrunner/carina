package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.EdgeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringSeleniumCommandExecutor;

import io.appium.java_client.remote.options.SupportsAutomationNameOption;

public class EdgeFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, String seleniumHost) {
        return this.create(testName, seleniumHost, null);
    }

    @Override
    public WebDriver create(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = new EdgeCapabilities().getCapabilities(testName, capabilities);
        LOGGER.debug("capabilities: {}", options);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }
        EventFiringSeleniumCommandExecutor ce = new EventFiringSeleniumCommandExecutor(hostURL);
        WebDriver driver = new RemoteWebDriver(ce, options);
        resizeBrowserWindow(driver, options);
        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {

        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.DESKTOP)) {
            return false;
        }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null) {
            return false;
        }

        if (capabilities.getBrowserName().equalsIgnoreCase(Browser.EDGE.browserName())) {
            return true;
        }

        return false;
    }
}
