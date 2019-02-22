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
package com.qaprosoft.carina.core.foundation.webdriver.core.factory;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.retry.RetryAnalyzer;
import com.qaprosoft.carina.core.foundation.retry.RetryCounter;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

import io.appium.java_client.ios.IOSStartScreenRecordingOptions.VideoQuality;

/**
 * Base implementation of WebDriver factory.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public abstract class AbstractFactory {
    
    protected static final Logger LOGGER = Logger.getLogger(AbstractFactory.class);
    
    protected final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss z");
    
    protected static final String vnc_protocol = "vnc_protocol";
    protected static final String vnc_host = "vnc_host";
    protected static final String vnc_port = "vnc_port";

    /**
     * Creates new instance of {@link WebDriver} according to specified {@link DesiredCapabilities}.
     * 
     * @param testName - where driver is initiated
     * @param capabilities - driver desired capabilitues
     * @param seleniumHost - selenium server URL
     * @return instance of {@link WebDriver}
     */
    abstract public WebDriver create(String testName, DesiredCapabilities capabilities, String seleniumHost);

    /**
     * If any listeners specified, converts RemoteWebDriver to EventFiringWebDriver and registers all listeners.
     * 
     * @param driver - instance of @link WebDriver}
     * @param listeners - instances of {@link WebDriverEventListener}
     * @return driver with registered listeners
     */
    public WebDriver registerListeners(WebDriver driver, WebDriverEventListener... listeners) {
        if (!ArrayUtils.isEmpty(listeners)) {
            driver = new EventFiringWebDriver(driver);
            for (WebDriverEventListener listener : listeners) {
                ((EventFiringWebDriver) driver).register(listener);
            }
        }
        return driver;
    }

    /**
     * Checks driver capabilities for being not empty.
     * 
     * @param capabilities - driver capabilities
     * @return if capabilities empty or null
     */
    protected boolean isCapabilitiesEmpty(Capabilities capabilities) {
        return capabilities == null || MapUtils.isEmpty(capabilities.asMap());
    }
    
    /**
     * Retrieves VNC URL if available.
     * 
     * @param driver - {@link RemoteWebDriver} instance
     * @return VNC URL
     */
    abstract public String getVncURL(WebDriver driver);
    
    /**
     * Returns bitrate by {@link VideoQuality}
     * @param quality - video quality for recording
     * @return appropriate bitrate
     */
    abstract protected int getBitrate(VideoQuality quality);
    
    /**
     * Generate test artifact for zafira upload.
     * @param videoName - video link name
     * @return test artifact with video details
     */
    protected TestArtifactType initVideoArtifact(String videoName) {
        TestArtifactType artifact = new TestArtifactType();
        artifact.setName("Video " + SDF.format(new Date()));
        ITestResult res = Reporter.getCurrentTestResult();
        if (res != null) {
        	artifact.setTestId((Long) res.getAttribute("ztid"));
        }
        artifact.setLink(String.format(R.CONFIG.get("screen_record_host"), videoName));
        artifact.setExpiresIn(Configuration.getInt(Configuration.Parameter.ARTIFACTS_EXPIRATION_SECONDS));
        return artifact;
    }
    
	protected boolean isVideoEnabled() {
		boolean isEnabled = R.CONFIG.getBoolean(SpecialKeywords.ENABLE_VIDEO);

		if (isEnabled && Configuration.getBoolean(Parameter.OPTIMIZE_VIDEO_RECORDING)) {
			if (RetryCounter.getRunCount() < RetryAnalyzer.getMaxRetryCountForTest()) {
				LOGGER.info("To optimize video recording it will be disabled for attempt {" + RetryCounter.getRunCount()
						+ "} because max retry_count={" + RetryAnalyzer.getMaxRetryCountForTest() + "}");
				// disable video recording for not the final retry if
				// "optimize_video_recording=true"
				isEnabled = false;
			}
		}
		return isEnabled;
	}
}