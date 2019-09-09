/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.EdgeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.FirefoxCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.IECapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.OperaCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.SafariCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.DriverFactory.HubType;
import com.qaprosoft.carina.core.foundation.webdriver.listener.DesktopRecordingListener;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringSeleniumCommandExecutor;
import com.qaprosoft.carina.core.foundation.webdriver.listener.ZebrunnerRecordingListener;

import io.appium.java_client.ios.IOSStartScreenRecordingOptions.VideoQuality;

public class DesktopFactory extends AbstractFactory {
    private static final Logger LOGGER = Logger.getLogger(DesktopFactory.class);

    private static DesiredCapabilities staticCapabilities;
    
    @Override
    public WebDriver create(String name, DesiredCapabilities capabilities, String seleniumHost) {
        RemoteWebDriver driver = null;
        if (seleniumHost == null) {
            seleniumHost = Configuration.get(Configuration.Parameter.SELENIUM_HOST);
        }

        if (isCapabilitiesEmpty(capabilities)) {
            capabilities = getCapabilities(name);
        }

        if (staticCapabilities != null) {
            LOGGER.info("Static DesiredCapabilities will be merged to basic driver capabilities");
            capabilities.merge(staticCapabilities);
        }

        try {
            
            EventFiringSeleniumCommandExecutor ce = new EventFiringSeleniumCommandExecutor(new URL(seleniumHost));
            if (isVideoEnabled()) {
            	final String videoName = getVideoName();
            	capabilities.setCapability("videoName", videoName);
                capabilities.setCapability("videoFrameRate", getBitrate(VideoQuality.valueOf(R.CONFIG.get("web_screen_record_quality"))));
            	
                switch (HubType.valueOf(Configuration.get(Parameter.HUB_MODE).toUpperCase())) {
				case DEFAULT:
					ce.getListeners().add(new DesktopRecordingListener(initVideoArtifact(videoName)));
					break;
				case ZEBRUNNER:
					ce.getListeners().add(new ZebrunnerRecordingListener(initVideoArtifact("%s/" + videoName)));
					break;
				}
            }
            
            driver = new RemoteWebDriver(ce, capabilities);
            
            resizeBrowserWindow(driver, capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to create desktop driver", e);
        }

        R.CONFIG.put(SpecialKeywords.ACTUAL_BROWSER_VERSION, getBrowserVersion(driver));
        return driver;
    }

    @SuppressWarnings("deprecation")
	public DesiredCapabilities getCapabilities(String name) {
        String browser = Configuration.getBrowser();

        if (BrowserType.FIREFOX.equalsIgnoreCase(browser)) {
            return new FirefoxCapabilities().getCapability(name);
        } else if (BrowserType.IEXPLORE.equalsIgnoreCase(browser) || BrowserType.IE.equalsIgnoreCase(browser) || browser.equalsIgnoreCase("ie")) {
            return new IECapabilities().getCapability(name);
        } else if (BrowserType.SAFARI.equalsIgnoreCase(browser)) {
            return new SafariCapabilities().getCapability(name);
        } else if (BrowserType.CHROME.equalsIgnoreCase(browser)) {
            return new ChromeCapabilities().getCapability(name);
        } else if (BrowserType.OPERA_BLINK.equalsIgnoreCase(browser) || BrowserType.OPERA.equalsIgnoreCase(browser)) {
            return new OperaCapabilities().getCapability(name);
        } else if (BrowserType.EDGE.toLowerCase().contains(browser.toLowerCase())) {
            return  new EdgeCapabilities().getCapability(name);
        } else {
            throw new RuntimeException("Unsupported browser: " + browser);
        }
    }

    public static void addStaticCapability(String name, Object value) {
        if (staticCapabilities == null) {
            staticCapabilities = new DesiredCapabilities();
        }
        staticCapabilities.setCapability(name, value);
    }

	@Override
	public String getVncURL(WebDriver driver) {
		String vncURL = null;
		if (driver instanceof RemoteWebDriver && "true".equals(Configuration.getCapability("enableVNC"))) {
			// TODO: resolve negative case when VNC is not supported
			final RemoteWebDriver rwd = (RemoteWebDriver) driver;
		    String protocol = R.CONFIG.get(vnc_protocol);
			String host = R.CONFIG.get(vnc_host);
			String port = R.CONFIG.get(vnc_port); 
			// If VNC host/port not set user them from Selenium
			if(StringUtils.isEmpty(host) || StringUtils.isEmpty(port)) {
			    host = ((HttpCommandExecutor) rwd.getCommandExecutor()).getAddressOfRemoteServer().getHost();
			    port = String.valueOf(((HttpCommandExecutor) rwd.getCommandExecutor()).getAddressOfRemoteServer().getPort());
			}
			vncURL = String.format(R.CONFIG.get("vnc_desktop"), protocol, host, port, rwd.getSessionId().toString());
		}
		return vncURL;
	}
	
	@Override
    protected int getBitrate(VideoQuality quality) {
        switch (quality) {
        case LOW:
            return 6;
        case MEDIUM:
            return 12;
        case HIGH:
            return 24;
        default:
            return 1;
        }
    }
	
    private String getBrowserVersion(WebDriver driver) {
        String browser_version = Configuration.get(Parameter.BROWSER_VERSION);
        try {
            Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
            browser_version = cap.getVersion().toString();
            if (browser_version != null) {
                if (browser_version.contains(".")) {
                    browser_version = StringUtils.join(StringUtils.split(browser_version, "."), ".", 0, 2);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to get actual browser version!", e);
        }
        return browser_version;
    }
    
    /**
	 * Sets browser window according to capabilites.resolution value, otherwise
	 * maximizes window.
	 * 
	 * @param driver       - instance of desktop @WebDriver
	 * @param capabilities - driver capabilities
	 */
	private void resizeBrowserWindow(WebDriver driver, DesiredCapabilities capabilities) {
		try {
			if (capabilities.getCapability("resolution") != null) {
				String resolution = (String) capabilities.getCapability("resolution");
				int width = Integer.valueOf(resolution.split("x")[0]);
				int height = Integer.valueOf(resolution.split("x")[1]);
				driver.manage().window().setPosition(new Point(0, 0));
				driver.manage().window().setSize(new Dimension(width, height));
				LOGGER.info(String.format("Browser window size set to %dx%d", width, height));
			} else {
				driver.manage().window().maximize();
				LOGGER.info("Browser window was maximized");
			}
		} catch (Exception e) {
			LOGGER.error("Unable to resize browser window", e);
		}
	}
}
