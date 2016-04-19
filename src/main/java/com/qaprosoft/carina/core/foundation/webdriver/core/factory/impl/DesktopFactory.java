package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesLoder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.FirefoxCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.HTMLUnitCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.IECapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.SafariCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public class DesktopFactory extends AbstractFactory {

	private static DesiredCapabilities staticCapabilities;

    @Override
    public WebDriver create(String testName, Device device) {
        RemoteWebDriver driver;
        String selenium = Configuration.get(Parameter.SELENIUM_HOST);

        DesiredCapabilities capabilities = getCapabilities(testName);
		// setting proxy server if needed
		String PROXY = Configuration.get(Parameter.PROXY_SERVER);
		if (!StringUtils.EMPTY.equals(PROXY))
		{
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
			capabilities.setCapability(CapabilityType.PROXY, proxy);
		}
		// setting static capabilities if not empty
		if (staticCapabilities != null)
		{
			capabilities.merge(staticCapabilities);
		}

        try {
            driver = new RemoteWebDriver(new URL(selenium), capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to create desktop driver");
        }
        return driver;
    }


    public DesiredCapabilities getCapabilities(String testName) {
        String customCapabilities = Configuration.get(Parameter.CUSTOM_CAPABILITIES);
        if (!customCapabilities.isEmpty()) {
            return new CapabilitiesLoder().loadCapabilities(customCapabilities);
        } else {
            String browser = Configuration.get(Parameter.BROWSER);

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
        }
    }

	public static void addCapability(String name, Object value)
	{
		if (staticCapabilities == null)
		{
			staticCapabilities = new DesiredCapabilities();
		}
		staticCapabilities.setCapability(name, value);
	}
}
