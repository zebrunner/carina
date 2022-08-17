package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringSeleniumCommandExecutor;

public class ChromeMiddleware extends DriverMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        if (!Objects.equals(Configuration.getDriverType(capabilities), SpecialKeywords.DESKTOP)) {
            return false;
        }

        if (capabilities.getBrowserName().equalsIgnoreCase(Browser.CHROME.browserName())) {
            return true;
        }
        return false;
    }

    @Override
    protected WebDriver getDriverByRule(String testName, String seleniumHost, Capabilities capabilities) {
        Capabilities options = capabilitiesMiddleware.analyze(new ChromeCapabilities().getCapabilities(testName, capabilities));
        LOGGER.debug("Chrome capabilities: {}", options);
        EventFiringSeleniumCommandExecutor ce = new EventFiringSeleniumCommandExecutor(getURL(seleniumHost));
        WebDriver driver = new RemoteWebDriver(ce, options);
        resizeBrowserWindow(driver, options);
        return driver;
    }
}
