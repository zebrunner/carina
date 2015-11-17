package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesLoder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.*;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

public class DesktopFactory extends AbstractFactory {

    @Override
    public WebDriver create(String testName, Device device) {
        RemoteWebDriver driver;
        String selenium = Configuration.get(Parameter.SELENIUM_HOST);
        DesiredCapabilities capabilities = getCapabilities(testName);

        try {
            driver = new RemoteWebDriver(new URL(selenium), capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to create desktop driver");
        }
        return driver;
    }


    public DesiredCapabilities getCapabilities(String testName) {
    	String driverType = Configuration.get(Parameter.DRIVER_TYPE);
    	String browser = Configuration.get(Parameter.BROWSER);
    	
    	
		if (driverType.equalsIgnoreCase(SpecialKeywords.CUSTOM)) {
			try {
				return new CapabilitiesLoder(new FileInputStream(Configuration.get(Parameter.CUSTOM_CAPABILITIES)))
						.loadCapabilities();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(
						"Unable read custom capabilities: " + Configuration.get(Parameter.CUSTOM_CAPABILITIES));
			}
		} else if (driverType.equalsIgnoreCase(SpecialKeywords.DESKTOP)) {
			if (BrowserType.FIREFOX.equalsIgnoreCase(browser)) {
				return new FirefoxCapabilities().getCapability(testName);
			} else if (BrowserType.IEXPLORE.equalsIgnoreCase(browser) || BrowserType.IE.equalsIgnoreCase(browser)
					|| browser.equalsIgnoreCase("ie")) {
				return new IECapabilities().getCapability(testName);
			} else if (BrowserType.HTMLUNIT.equalsIgnoreCase(browser)) {
				return new HTMLUnitCapabilities().getCapability(testName);
			} else if (BrowserType.SAFARI.equalsIgnoreCase(browser)) {
				return new SafariCapabilities().getCapability(testName);
			} else if (BrowserType.CHROME.equalsIgnoreCase(browser)) {
				return new ChromeCapabilities().getCapability(testName);
			} else {
				throw new RuntimeException("Unsupported browser: " + browser);
			}
		} else {
			throw new RuntimeException("Unsupported driverType: " + driverType);
		}

    }
}
