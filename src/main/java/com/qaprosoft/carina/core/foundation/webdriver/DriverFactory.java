/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.webdriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.qaprosoft.carina.core.foundation.exception.InvalidArgsException;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.android.AndroidNativeDriver;

import org.openqa.selenium.Capabilities;

/**
 * DriverFactory produces driver instance with desired capabilities according to
 * configuration.
 * 
 * @author Alexey Khursevich (hursevich@gmail.com)
 */
public class DriverFactory
{
	public static final String HTML_UNIT = "htmlunit";
	public static final String IOS = "iOS";
	public static final String ANDROID = "Android";
	public static final String SAFARI = "safari";

	/**
	 * Creates diver instance for specified test.
	 * 
	 * @param testName
	 *            in which driver will be used
	 * @return WebDriver instance
	 */
	public static synchronized WebDriver create(String testName)
	{
		WebDriver driver = null;
		DesiredCapabilities capabilities = null;
		try
		{
			if (BrowserType.FIREFOX.equalsIgnoreCase(Configuration.get(Parameter.BROWSER)))
			{
				capabilities = getFirefoxCapabilities(testName);

			}
			else if (BrowserType.IEXPLORE.equalsIgnoreCase(Configuration.get(Parameter.BROWSER)))
			{
				capabilities = getInternetExplorerCapabilities(testName);
			}
			else if (HTML_UNIT.equalsIgnoreCase(Configuration.get(Parameter.BROWSER)))
			{
				capabilities = getHtmlUnitCapabilities(testName);
			}
			else if (IOS.equalsIgnoreCase(Configuration.get(Parameter.BROWSER)))
			{
				if (Configuration.isNull(Parameter.MOBILE_OS) 
						|| Configuration.isNull(Parameter.MOBILE_VERSION)
						|| Configuration.isNull(Parameter.MOBILE_PLATFORM) 
						|| Configuration.isNull(Parameter.MOBILE_APP)
						|| Configuration.isNull(Parameter.MOBILE_DEVICE)) throw new InvalidArgsException("'MOBILE_OS', 'MOBILE_DEVICE', 'MOBILE_VERSION', 'MOBILE_PLATFORM', 'MOBILE_APP' should be set!");
				
				capabilities = getIOSCapabilities(testName);
			}
			else if (ANDROID.equalsIgnoreCase(Configuration.get(Parameter.BROWSER)))
			{
				
				if (Configuration.isNull(Parameter.MOBILE_DEVICE) 
						|| Configuration.isNull(Parameter.MOBILE_VERSION)
						|| Configuration.isNull(Parameter.MOBILE_PLATFORM) 
						|| Configuration.isNull(Parameter.MOBILE_APP)
						|| Configuration.isNull(Parameter.MOBILE_APP_PACKAGE)
						|| Configuration.isNull(Parameter.MOBILE_APP_ACTIVITY)) throw new InvalidArgsException("'MOBILE_OS', 'MOBILE_DEVICE', 'MOBILE_VERSION', 'MOBILE_PLATFORM', 'MOBILE_APP', 'MOBILE_APP_PACKAGE', 'MOBILE_APP_ACTIVITY' should be set!");
				
				capabilities = getAndriodCapabilities(testName);
				driver = new AndroidNativeDriver(new URL(Configuration.get(Parameter.SELENIUM_HOST)), capabilities);
				driver = new Augmenter().augment(driver);
				return driver;
			}
			else
			{
				capabilities = getChromeCapabilities(testName);
			}
			driver = new RemoteWebDriver(new URL(Configuration.get(Parameter.SELENIUM_HOST)), capabilities);
			driver = new Augmenter().augment(driver);
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException("Can't connect to selenium server: " + Configuration.get(Parameter.SELENIUM_HOST));
		}
		return driver;
	}

	public static String getBrowserName(WebDriver driver)
	{
		Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
		return cap.getBrowserName().toString();	
	}
	
	public static String getBrowserVersion(WebDriver driver)
	{
		Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
		return cap.getVersion().toString();	
		
	}

	private static DesiredCapabilities getFirefoxCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = DesiredCapabilities.firefox();
		//capabilities = initBaseCapabilities(capabilities, Platform.WINDOWS, Configuration.get(Parameter.BROWSER), R.CONFIG.get("firefox_version"), "name", testName);
		capabilities = initBaseCapabilities(capabilities, Platform.WINDOWS, Configuration.get(Parameter.BROWSER), Configuration.get(Parameter.BROWSER_VERSION), "name", testName);		
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
		FirefoxProfile profile = new FirefoxProfile();
		profile.setEnableNativeEvents(false);
		if (!StringUtils.isEmpty(Configuration.get(Parameter.USER_AGENT)) && !"n/a".equals(Configuration.get(Parameter.USER_AGENT)))
		{
			profile.setPreference("general.useragent.override", Configuration.get(Parameter.USER_AGENT));
		}
		capabilities.setCapability(FirefoxDriver.PROFILE, profile);
		return capabilities;

	}

	private static DesiredCapabilities getInternetExplorerCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities = initBaseCapabilities(capabilities, Platform.WINDOWS, Configuration.get(Parameter.BROWSER), Configuration.get(Parameter.BROWSER_VERSION), "name", testName);
		capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
		return capabilities;
	}

	private static DesiredCapabilities getChromeCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities = initBaseCapabilities(capabilities, Platform.WINDOWS, Configuration.get(Parameter.BROWSER), Configuration.get(Parameter.BROWSER_VERSION), "name", testName);
		capabilities.setCapability("chrome.switches", Arrays.asList("--start-maximized", "--ignore-certificate-errors"));
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
		return capabilities;
	}

	private static DesiredCapabilities getHtmlUnitCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
		capabilities.setPlatform(Platform.WINDOWS);
		capabilities.setJavascriptEnabled(true);
		return capabilities;
	}

	private static DesiredCapabilities getIOSCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
		capabilities.setCapability(CapabilityType.BROWSER_NAME, Configuration.get(Parameter.MOBILE_OS));
		capabilities.setCapability("device", Configuration.get(Parameter.MOBILE_DEVICE));
		capabilities.setCapability(CapabilityType.VERSION, Configuration.get(Parameter.MOBILE_VERSION));
		capabilities.setCapability(CapabilityType.PLATFORM, Configuration.get(Parameter.MOBILE_PLATFORM));
		if(!SAFARI.equals(Configuration.get(Parameter.MOBILE_APP)) && !new File(Configuration.get(Parameter.MOBILE_APP)).exists())
		{
			throw new InvalidArgsException("No application found: " + Configuration.get(Parameter.MOBILE_APP));
		}
		capabilities.setCapability("app", Configuration.get(Parameter.MOBILE_APP));
		capabilities.setCapability("name", testName);
		return capabilities;
	}
	
	private static DesiredCapabilities getAndriodCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
		capabilities.setCapability("device", Configuration.get(Parameter.MOBILE_DEVICE));
		capabilities.setCapability(CapabilityType.VERSION, Configuration.get(Parameter.MOBILE_VERSION));
		capabilities.setCapability(CapabilityType.PLATFORM, Configuration.get(Parameter.MOBILE_PLATFORM));
		
		capabilities.setCapability(CapabilityType.BROWSER_NAME, Configuration.get(Parameter.MOBILE_BROWSER));
		capabilities.setCapability("app", Configuration.get(Parameter.MOBILE_APP));
		capabilities.setCapability("app-package", Configuration.get(Parameter.MOBILE_APP_PACKAGE));
		capabilities.setCapability("app-activity", Configuration.get(Parameter.MOBILE_APP_ACTIVITY));
		capabilities.setCapability("newCommandTimeout", Configuration.get(Parameter.MOBILE_NEW_COMMAND_TIMEOUT));

		capabilities.setCapability("name", testName);
		return capabilities;
	}

	private static DesiredCapabilities initBaseCapabilities(DesiredCapabilities capabilities, Platform platform, String... args)
	{
		capabilities.setPlatform(platform);
		capabilities.setBrowserName(args[0]);
		capabilities.setVersion(args[1]);
		capabilities.setCapability("name", args[2]);
		return capabilities;
	}
}