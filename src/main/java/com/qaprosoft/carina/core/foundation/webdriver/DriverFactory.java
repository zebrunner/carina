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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.SkipException;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.appium.AppiumNativeDriver;

/**
 * DriverFactory produces driver instance with desired capabilities according to
 * configuration.
 * 
 * @author Alexey Khursevich (hursevich@gmail.com)
 */
public class DriverFactory
{
	public static final String CHROME = "chrome";
	public static final String HTML_UNIT = "htmlunit";
	public static final String IOS = "iOS";
	public static final String ANDROID = "Android";
	public static final String SAFARI = "safari";
	public static final String MOBILE_GRID = "mobile_grid";
	public static final String MOBILE = "mobile";
	
	protected static final Logger LOGGER = Logger.getLogger(DriverFactory.class);
	
	private static ArrayList<Integer> firefoxPorts = new ArrayList<Integer>();


/*	public static Object create(String className, Object[] args) {
		Class<?> clazz;
		Object object = null;
		try {
			clazz = Class.forName(className);

			Constructor<?> ctor = clazz.getConstructor(URL.class, Capabilities.class);
			object = ctor.newInstance(args);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return object;
	}*/
	
	public static synchronized WebDriver create(String testName, String browser)
	{
		WebDriver driver = null;
		try {
			if (BrowserType.FIREFOX.equalsIgnoreCase(browser))
			{
				driver = new FirefoxDriver();
			}
			else if (BrowserType.IEXPLORE.equalsIgnoreCase(browser) || BrowserType.IE.equalsIgnoreCase(browser) || browser.equalsIgnoreCase("ie"))
			{
				driver = new InternetExplorerDriver();
			}
			else if (CHROME.equalsIgnoreCase(browser))
			{
				driver = new ChromeDriver();
			}
			else {
				LOGGER.error("Unsupported extra local driver is requested: " + browser);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to initialize extra driver!\r\n" + e.getMessage());
		}		
			
		return driver;
	}

	public static synchronized WebDriver create(String testName, DesiredCapabilities capabilities, String selenium_host)
	{
		RemoteWebDriver driver = null;
		try {
			if (capabilities.getCapability("automationName") == null)
				driver = new RemoteWebDriver(new URL(selenium_host), capabilities);
			else		
				driver = new AppiumNativeDriver(new URL(selenium_host), capabilities);
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to initialize extra driver!\r\n" + e.getMessage());
		}		
			
		return driver;
	}
	/**
	 * Creates driver instance for specified test.
	 * 
	 * @param testName
	 *            in which driver will be used
	 * @return RemoteWebDriver instance
	 * @throws MalformedURLException 
	 */
	public static synchronized WebDriver create(String testName)
	{
		RemoteWebDriver driver = null;
		DesiredCapabilities capabilities = null;
		try
		{
			String browser = Configuration.get(Parameter.BROWSER);
			if (BrowserType.FIREFOX.equalsIgnoreCase(browser))
			{
				capabilities = getFirefoxCapabilities(testName, Configuration.get(Parameter.BROWSER_VERSION), Configuration.get(Parameter.USER_AGENT));
			}
			else if (BrowserType.IEXPLORE.equalsIgnoreCase(browser) || BrowserType.IE.equalsIgnoreCase(browser) || browser.equalsIgnoreCase("ie"))
			{
				capabilities = getInternetExplorerCapabilities(testName, Configuration.get(Parameter.BROWSER_VERSION));
			}
			else if (HTML_UNIT.equalsIgnoreCase(browser))
			{
				capabilities = getHtmlUnitCapabilities(testName);
			}
			else if (MOBILE.equalsIgnoreCase(browser))
			{
				if (!Configuration.get(Parameter.MOBILE_BROWSER_NAME).isEmpty())
				{
					capabilities = getMobileWebCapabilities(false, testName, Configuration.get(Parameter.MOBILE_PLATFORM_NAME), Configuration.get(Parameter.MOBILE_PLATFORM_VERSION),
							Configuration.get(Parameter.MOBILE_DEVICE_NAME), Configuration.get(Parameter.MOBILE_AUTOMATION_NAME),
							Configuration.get(Parameter.MOBILE_NEW_COMMAND_TIMEOUT), Configuration.get(Parameter.MOBILE_BROWSER_NAME));
				}
				else {
					capabilities = getMobileAppCapabilities(false, testName, Configuration.get(Parameter.MOBILE_PLATFORM_NAME), Configuration.get(Parameter.MOBILE_PLATFORM_VERSION),
							Configuration.get(Parameter.MOBILE_DEVICE_NAME), Configuration.get(Parameter.MOBILE_AUTOMATION_NAME),
							Configuration.get(Parameter.MOBILE_NEW_COMMAND_TIMEOUT), Configuration.get(Parameter.MOBILE_APP),
							Configuration.get(Parameter.MOBILE_APP_ACTIVITY), Configuration.get(Parameter.MOBILE_APP_PACKAGE));
				}
			}
			else if (MOBILE_GRID.equalsIgnoreCase(browser))
			{
				if (!Configuration.get(Parameter.MOBILE_BROWSER_NAME).isEmpty())
				{
					capabilities = getMobileWebCapabilities(true, testName, Configuration.get(Parameter.MOBILE_PLATFORM_NAME), Configuration.get(Parameter.MOBILE_PLATFORM_VERSION),
							Configuration.get(Parameter.MOBILE_DEVICE_NAME), Configuration.get(Parameter.MOBILE_AUTOMATION_NAME),
							Configuration.get(Parameter.MOBILE_NEW_COMMAND_TIMEOUT), Configuration.get(Parameter.MOBILE_BROWSER_NAME));
				}
				else
				{
					capabilities = getMobileAppCapabilities(true, testName, Configuration.get(Parameter.MOBILE_PLATFORM_NAME), Configuration.get(Parameter.MOBILE_PLATFORM_VERSION),
							Configuration.get(Parameter.MOBILE_DEVICE_NAME), Configuration.get(Parameter.MOBILE_AUTOMATION_NAME),
							Configuration.get(Parameter.MOBILE_NEW_COMMAND_TIMEOUT), Configuration.get(Parameter.MOBILE_APP),
							Configuration.get(Parameter.MOBILE_APP_ACTIVITY), Configuration.get(Parameter.MOBILE_APP_PACKAGE));
				}					
			}
			else if (SAFARI.equalsIgnoreCase(browser))
			{
				capabilities = getSafariCapabilities(testName, Configuration.get(Parameter.BROWSER_VERSION));
			}			
			else
			{
				capabilities = getChromeCapabilities(testName, Configuration.get(Parameter.BROWSER_VERSION));
			}

	    	LOGGER.debug("-------------------------------------- Driver Factory start ----------------------------------");			
			if (browser.toLowerCase().contains(MOBILE.toLowerCase()))
			{
				//only in case of "mobile" or "mobile_grid" as browser and ANDROID as mobile_platform_name
				driver = new AppiumNativeDriver(new URL(Configuration.get(Parameter.SELENIUM_HOST)), capabilities);
			} else {		
				driver = new RemoteWebDriver(new URL(Configuration.get(Parameter.SELENIUM_HOST)), capabilities);
			}
	    	LOGGER.debug("-------------------------------------- Driver Factory finish ---------------------------------");			
		}
		catch (Exception e)
		{
	    	throw new SkipException("Unable to initialize driver. Test will be SKIPPED due to the\r\n" + e.getMessage());
		}
		return driver;
	}
	
	public static DesiredCapabilities getMobileAppCapabilities(boolean gridMode, String testName, String platform, String platformVersion, String deviceName, 
			String automationName, String commandTimeout, String app, String appActivity, String appPackage) {
		return getMobileCapabilities(gridMode, testName, platform, platformVersion, deviceName, automationName, commandTimeout, null, app, appActivity, appPackage);
	}
	public static DesiredCapabilities getMobileWebCapabilities(boolean gridMode, String testName, String platform, String platformVersion, String deviceName, 
			String automationName, String commandTimeout, String browserName) {
		return getMobileCapabilities(gridMode, testName, platform, platformVersion, deviceName, automationName, commandTimeout, browserName, null, null, null);
	}
	
	private static DesiredCapabilities getMobileCapabilities(boolean gridMode, String testName, String platform, String platformVersion, String deviceName, 
			String automationName, String commandTimeout, String browserName, String app, String appActivity, String appPackage) 
	{
		DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", platform); //Parameter.MOBILE_PLATFORM_NAME
        capabilities.setCapability("platformVersion", platformVersion); //Parameter.MOBILE_PLATFORM_VERSION
        if (deviceName != null) 
        	capabilities.setCapability("deviceName", deviceName); //Parameter.MOBILE_DEVICE_NAME

        capabilities.setCapability("automationName", automationName); //Parameter.MOBILE_AUTOMATION_NAME
        capabilities.setCapability("newCommandTimeout", commandTimeout); //Parameter.MOBILE_NEW_COMMAND_TIMEOUT
        

        if (gridMode) {
	        capabilities.setCapability("platform", platform);
	        capabilities.setCapability("version", platformVersion);
	        capabilities.setCapability("browserName", deviceName);
        }

		if (browserName != null) //Mobile Web
		{
	        capabilities.setCapability("browserName", browserName);
	        if (gridMode && platform.equalsIgnoreCase("iOS")) {
	        	capabilities.setCapability("platform", "MAC");
	        }
		}
		else { //Mobile App
	        capabilities.setCapability("browserName", "");
	        capabilities.setCapability("app", app); //Parameter.MOBILE_APP
	        if (appActivity != null)
	        	capabilities.setCapability("appActivity", appActivity); //Parameter.MOBILE_APP_ACTIVITY
	        
	        if (appPackage != null)
	        	capabilities.setCapability("appPackage", appPackage); //Parameter.MOBILE_APP_PACKAGE
		}
		
        return capabilities;
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

	public static synchronized DesiredCapabilities getFirefoxCapabilities(String testName) {
		return getFirefoxCapabilities(testName, "*", Configuration.get(Parameter.USER_AGENT));
	}
	public static synchronized DesiredCapabilities getFirefoxCapabilities(String testName, String browserVersion, String userAgent) {
		DesiredCapabilities capabilities = DesiredCapabilities.firefox();
		//capabilities = initBaseCapabilities(capabilities, Configuration.get(Parameter.BROWSER), Configuration.get(Parameter.BROWSER_VERSION), testName);		
		capabilities = initBaseCapabilities(capabilities, BrowserType.FIREFOX, browserVersion, testName);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
		FirefoxProfile profile = new FirefoxProfile();
		
		//AUTO-411 eTAF randomly fails a test with 'Unable to bind to locking port 7054 within 45000 ms' error 
		//trying to workaround generating random port for each session
		//also exclude already bind ports for current execution in firefoxPorts array
		boolean generated = false;
		int newPort = 7055;
		int i = 100;
		while (!generated && (--i > 0)) {
			newPort = PortProber.findFreePort();
			generated = firefoxPorts.add(newPort);
		}
		if (!generated) {
			newPort = 7055;
		}
		//limitation not to use last 20 ports for FF binding
		if (firefoxPorts.size() > 20) {
			firefoxPorts.remove(0);
		}
		LOGGER.info(firefoxPorts);
		
		profile.setPreference(FirefoxProfile.PORT_PREFERENCE, newPort);
		LOGGER.info("FireFox profile will use '" + newPort + "' port number.");
        
		profile.setPreference("dom.max_chrome_script_run_time", 0);
		profile.setPreference("dom.max_script_run_time", 0);
		//profile.setEnableNativeEvents(false);
		//VD enable native events to support drag&drop using javascript
		profile.setEnableNativeEvents(true);
		if (!StringUtils.isEmpty(userAgent) && !"n/a".equals(userAgent))
		{
			profile.setPreference("general.useragent.override", userAgent);
		}
		capabilities.setCapability(FirefoxDriver.PROFILE, profile);
		return capabilities;

	}

	public static DesiredCapabilities getInternetExplorerCapabilities(String testName)
	{
		return getInternetExplorerCapabilities(testName, "*");
	}
	public static DesiredCapabilities getInternetExplorerCapabilities(String testName, String browserVersion)
	{
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities = initBaseCapabilities(capabilities, BrowserType.IEXPLORE, browserVersion, testName);
		capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
		return capabilities;
	}

	public static DesiredCapabilities getChromeCapabilities(String testName) {
		return getChromeCapabilities(testName, "*");
	}
	public static DesiredCapabilities getChromeCapabilities(String testName, String browserVersion)
	{
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities = initBaseCapabilities(capabilities, BrowserType.CHROME, browserVersion, testName);
		
		capabilities.setCapability("chrome.switches", Arrays.asList("--start-maximized"/*, "--ignore-certificate-errors"*/));
		
		//capabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("test-type");
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return capabilities;
	}

	public static DesiredCapabilities getHtmlUnitCapabilities(String testName)
	{
		DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
		//capabilities.setBrowserName(BrowserType.CHROME);
		String platform = Configuration.get(Parameter.PLATFORM);
		if (!platform.equals("*")) {
			capabilities.setPlatform(Platform.extractFromSysProperty(platform));
		}
		capabilities.setJavascriptEnabled(true);
		return capabilities;
	}
	
	public static DesiredCapabilities getSafariCapabilities(String testName) {
		return getSafariCapabilities(testName, "*");
	}
	public static DesiredCapabilities getSafariCapabilities(String testName, String browserVersion)
	{
		DesiredCapabilities capabilities = DesiredCapabilities.safari();
		capabilities = initBaseCapabilities(capabilities, BrowserType.SAFARI, browserVersion, testName);		
		capabilities.setCapability("name", testName);
		return capabilities;
	}
	
	private static DesiredCapabilities initBaseCapabilities(DesiredCapabilities capabilities, String browser, String browserVersion, String testName)
	{	
		//platform should be detected and provided into capabilities automatically
		String platform = Configuration.get(Parameter.PLATFORM);
		if (!platform.equals("*")) {
			capabilities.setPlatform(Platform.extractFromSysProperty(platform));
		}

		capabilities.setBrowserName(browser);
		capabilities.setVersion(browserVersion);
		capabilities.setCapability("name", testName);
		return capabilities;
	}
}