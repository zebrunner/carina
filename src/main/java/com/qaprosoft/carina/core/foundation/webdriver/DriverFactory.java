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
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
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
	public static final String HTML_UNIT = "htmlunit";
	public static final String IOS = "iOS";
	public static final String ANDROID = "Android";
	public static final String SAFARI = "safari";
	public static final String SELENDROID = "Selendroid";
	public static final String MOBILE_GRID = "mobile_grid";
	public static final String MOBILE = "mobile";
	
	private static ArrayList<Integer> firefoxPorts = new ArrayList<Integer>();

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
			else if (MOBILE.equalsIgnoreCase(Configuration.get(Parameter.BROWSER)))
			{
				capabilities = new DesiredCapabilities();				
				if (!Configuration.get(Parameter.MOBILE_BROWSER_NAME).equalsIgnoreCase("null"))
				{
					//Mobile Web
			        capabilities.setCapability("browserName", Configuration.get(Parameter.MOBILE_BROWSER_NAME));
			        capabilities.setCapability("platformName", Configuration.get(Parameter.MOBILE_PLATFORM_NAME));
			        capabilities.setCapability("platformVersion", Configuration.get(Parameter.MOBILE_PLATFORM_VERSION));
			        capabilities.setCapability("deviceName", Configuration.get(Parameter.MOBILE_DEVICE_NAME));
			        capabilities.setCapability("automationName", Configuration.get(Parameter.MOBILE_AUTOMATION_NAME));
			        capabilities.setCapability("newCommandTimeout", Configuration.get(Parameter.MOBILE_NEW_COMMAND_TIMEOUT));
				}
				else {
					//Mobile App
			        capabilities.setCapability("browserName", "");
			        capabilities.setCapability("platformName", Configuration.get(Parameter.MOBILE_PLATFORM_NAME));
			        capabilities.setCapability("platformVersion", Configuration.get(Parameter.MOBILE_PLATFORM_VERSION));
			        if (!Configuration.get(Parameter.MOBILE_DEVICE_NAME).equalsIgnoreCase("null"))
				        capabilities.setCapability("deviceName", Configuration.get(Parameter.MOBILE_DEVICE_NAME));
			        capabilities.setCapability("automationName", Configuration.get(Parameter.MOBILE_AUTOMATION_NAME));
			        capabilities.setCapability("app", Configuration.get(Parameter.MOBILE_APP));
			        if (!Configuration.get(Parameter.MOBILE_APP_ACTIVITY).equalsIgnoreCase("null"))
			        	capabilities.setCapability("appActivity", Configuration.get(Parameter.MOBILE_APP_ACTIVITY));
			        
			        if (!Configuration.get(Parameter.MOBILE_APP_PACKAGE).equalsIgnoreCase("null"))
			        	capabilities.setCapability("appPackage", Configuration.get(Parameter.MOBILE_APP_PACKAGE));
			        
			        capabilities.setCapability("newCommandTimeout", Configuration.get(Parameter.MOBILE_NEW_COMMAND_TIMEOUT));			        
			        
				}
			}
			else if (MOBILE_GRID.equalsIgnoreCase(Configuration.get(Parameter.BROWSER)))
			{
				capabilities = new DesiredCapabilities();				
				if (Configuration.get(Parameter.MOBILE_BROWSER_NAME).equalsIgnoreCase("null"))
				{
					//Mobile App
			        capabilities.setCapability("platform", Configuration.get(Parameter.MOBILE_PLATFORM_NAME));
			        capabilities.setCapability("platformName", Configuration.get(Parameter.MOBILE_PLATFORM_NAME));

			        capabilities.setCapability("version", Configuration.get(Parameter.MOBILE_PLATFORM_VERSION));
			        capabilities.setCapability("platformVersion", Configuration.get(Parameter.MOBILE_PLATFORM_VERSION));
			        if (!Configuration.get(Parameter.MOBILE_DEVICE_NAME).equalsIgnoreCase("null")) {
				        capabilities.setCapability("browserName", Configuration.get(Parameter.MOBILE_DEVICE_NAME));
				        capabilities.setCapability("deviceName", Configuration.get(Parameter.MOBILE_DEVICE_NAME));
			        }
			        capabilities.setCapability("automationName", Configuration.get(Parameter.MOBILE_AUTOMATION_NAME));
			        capabilities.setCapability("app", Configuration.get(Parameter.MOBILE_APP));
			        if (!Configuration.get(Parameter.MOBILE_APP_ACTIVITY).equalsIgnoreCase("null"))
			        	capabilities.setCapability("appActivity", Configuration.get(Parameter.MOBILE_APP_ACTIVITY));
			        
			        if (!Configuration.get(Parameter.MOBILE_APP_PACKAGE).equalsIgnoreCase("null"))
			        	capabilities.setCapability("appPackage", Configuration.get(Parameter.MOBILE_APP_PACKAGE));
			        
			        capabilities.setCapability("newCommandTimeout", Configuration.get(Parameter.MOBILE_NEW_COMMAND_TIMEOUT));			        
				}
				else
				{
					//Mobile Web App
					capabilities.setCapability("platformName", Configuration.get(Parameter.MOBILE_PLATFORM_NAME)); //iOS
					if (Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase("iOS")) {
						capabilities.setCapability("platform", "MAC");
					}
					else {
						capabilities.setCapability("platform", Configuration.get(Parameter.MOBILE_PLATFORM_NAME));
					}
			        
			        capabilities.setCapability("version", Configuration.get(Parameter.MOBILE_PLATFORM_VERSION));
			        capabilities.setCapability("platformVersion", Configuration.get(Parameter.MOBILE_PLATFORM_VERSION));
			        
			        if (!Configuration.get(Parameter.MOBILE_DEVICE_NAME).equalsIgnoreCase("null")) {
				        capabilities.setCapability("browserName", Configuration.get(Parameter.MOBILE_DEVICE_NAME));
				        capabilities.setCapability("deviceName", Configuration.get(Parameter.MOBILE_DEVICE_NAME));
			        }
			        capabilities.setCapability("automationName", Configuration.get(Parameter.MOBILE_AUTOMATION_NAME));
			        capabilities.setCapability("newCommandTimeout", Configuration.get(Parameter.MOBILE_NEW_COMMAND_TIMEOUT));			        
				}					
					
			}
			else if (SELENDROID.equalsIgnoreCase(Configuration.get(Parameter.BROWSER)))
			{
				capabilities = getSelendroidCapabilities(testName);
			}			
			else
			{
				capabilities = getChromeCapabilities(testName);
			}

			if (Configuration.get(Parameter.BROWSER).toLowerCase().contains(MOBILE.toLowerCase()))
			{
				//only in case of "mobile" or "mobile_grid" as browser and ANDROID as mobile_platform_name
				driver = new AppiumNativeDriver(new URL(Configuration.get(Parameter.SELENIUM_HOST)), capabilities);
				return driver;
			} else {		
				driver = new RemoteWebDriver(new URL(Configuration.get(Parameter.SELENIUM_HOST)), capabilities);
			}
		}
		catch (MalformedURLException e)
		{
			//throw new RuntimeException("Can't connect to selenium server: " + Configuration.get(Parameter.SELENIUM_HOST));
	    	throw new SkipException("Can't connect to selenium server: " + Configuration.get(Parameter.SELENIUM_HOST) + "\r\nTest will be SKIPPED due to the\r\n" + e.getMessage());			
		}
		catch (Exception e)
		{
	    	throw new SkipException("Unable to initialize driver. Test will be SKIPPED due to the\r\n" + e.getMessage());
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

	private static synchronized DesiredCapabilities getFirefoxCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = DesiredCapabilities.firefox();
		//capabilities = initBaseCapabilities(capabilities, Platform.WINDOWS, Configuration.get(Parameter.BROWSER), R.CONFIG.get("firefox_version"), "name", testName);
		capabilities = initBaseCapabilities(capabilities, Configuration.get(Parameter.BROWSER), Configuration.get(Parameter.BROWSER_VERSION), testName);		
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
		System.out.println(firefoxPorts);
		
		profile.setPreference(FirefoxProfile.PORT_PREFERENCE, newPort);
		System.out.println("FireFox profile will use '" + newPort + "' port numer.");
        
		profile.setPreference("dom.max_chrome_script_run_time", 0);
		profile.setPreference("dom.max_script_run_time", 0);
		//profile.setEnableNativeEvents(false);
		//VD enable native events to support drag&drop using javascript
		profile.setEnableNativeEvents(true);
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
		capabilities = initBaseCapabilities(capabilities, Configuration.get(Parameter.BROWSER), Configuration.get(Parameter.BROWSER_VERSION), testName);
		capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
		return capabilities;
	}

	private static DesiredCapabilities getChromeCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities = initBaseCapabilities(capabilities, Configuration.get(Parameter.BROWSER), Configuration.get(Parameter.BROWSER_VERSION), testName);
		
		capabilities.setCapability("chrome.switches", Arrays.asList("--start-maximized"/*, "--ignore-certificate-errors"*/));
		
		//capabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);

		ChromeOptions options = new ChromeOptions();
		
		options.addArguments("test-type");
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return capabilities;
	}

	private static DesiredCapabilities getHtmlUnitCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
		//capabilities.setBrowserName(BrowserType.CHROME);
		capabilities.setPlatform(Platform.WINDOWS);
		capabilities.setJavascriptEnabled(true);
		return capabilities;
	}
	
	private static DesiredCapabilities getSelendroidCapabilities(String testName) throws MalformedURLException
	{
		DesiredCapabilities capabilities = DesiredCapabilities.android();
		capabilities.setCapability("name", testName);
		return capabilities;
	}
	
	private static DesiredCapabilities initBaseCapabilities(DesiredCapabilities capabilities, String... args)
	{	
		//platform should be detected and provided into capabilities automatically
		String platform = Configuration.get(Parameter.PLATFORM);
		if (!platform.equals("*"))
			capabilities.setPlatform(Platform.extractFromSysProperty(platform));

		capabilities.setBrowserName(args[0]);
		capabilities.setVersion(args[1]);
		capabilities.setCapability("name", args[2]);
		return capabilities;
	}
}