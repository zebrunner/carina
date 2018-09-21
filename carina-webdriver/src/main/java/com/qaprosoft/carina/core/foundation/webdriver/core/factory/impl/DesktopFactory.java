/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.EdgeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.FirefoxCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.IECapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop.SafariCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.listener.DesktopRecordingListener;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringSeleniumCommandExecutor;

import io.appium.java_client.ios.IOSStartScreenRecordingOptions.VideoQuality;

public class DesktopFactory extends AbstractFactory {

    private static DesiredCapabilities staticCapabilities;
    
    @Override
    public WebDriver create(String name, Device device, DesiredCapabilities capabilities, String seleniumHost) {
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
                final String videoName = UUID.randomUUID().toString();
                capabilities.setCapability("videoName", videoName + ".mp4");
                capabilities.setCapability("videoFrameRate", getBitrate(VideoQuality.valueOf(R.CONFIG.get("screen_record_quality"))));
                ce.getListeners().add(new DesktopRecordingListener(initVideoArtifact(videoName)));
            }
            
            driver = new RemoteWebDriver(ce, capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to create desktop driver", e);
        }

        R.CONFIG.put(SpecialKeywords.ACTUAL_BROWSER_VERSION, getBrowserVersion(driver));
        return driver;
    }

    public DesiredCapabilities getCapabilities(String name) {
        String browser = Configuration.get(Parameter.BROWSER);

        if (BrowserType.FIREFOX.equalsIgnoreCase(browser)) {
            return new FirefoxCapabilities().getCapability(name);
        } else if (BrowserType.IEXPLORE.equalsIgnoreCase(browser) || BrowserType.IE.equalsIgnoreCase(browser) || browser.equalsIgnoreCase("ie")) {
            DesiredCapabilities caps = new IECapabilities().getCapability(name);
            return caps;
        } else if (BrowserType.SAFARI.equalsIgnoreCase(browser)) {
            return new SafariCapabilities().getCapability(name);
        } else if (BrowserType.CHROME.equalsIgnoreCase(browser)) {
            return new ChromeCapabilities().getCapability(name);
        } else if (BrowserType.EDGE.toLowerCase().contains(browser.toLowerCase())) {
            DesiredCapabilities caps = new EdgeCapabilities().getCapability(name);
            // forcibly override browser name to edge for support 3rd party solutions like browserstack
            caps.setBrowserName(browser);
            return caps;
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
			// If VNC host/port not set user them from Selenim
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
}
