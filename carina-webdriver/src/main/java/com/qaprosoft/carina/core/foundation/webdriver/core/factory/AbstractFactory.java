/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

import io.appium.java_client.ios.IOSStartScreenRecordingOptions.VideoQuality;

/**
 * Base implementation of WebDriver factory.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public abstract class AbstractFactory {
    
    protected final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss z");

    protected static final String vnc_protocol = "vnc_protocol";
    protected static final String vnc_host = "vnc_host";
    protected static final String vnc_port = "vnc_port";
    
    //TODO: refactor to use SpecialKeywords.DEFAULT_VIDEO_FILENAME. Make sure to change uploading approach removing extra sub-folder 
    protected final static String VIDEO_DEFAULT = "video.mp4";
    protected final static String SESSION_LOG_DEFAULT = "session.log";


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
     * 
     * @param quality - video quality for recording
     * @return appropriate bitrate
     */
    abstract protected int getBitrate(VideoQuality quality);

    /**
     * Initialize test artifact for upload.
     * 
     * @param videoName - video file name
     * @return test artifact with video details
     */
    protected TestArtifactType initVideoArtifact(String videoName) {
        TestArtifactType artifact = new TestArtifactType();
        artifact.setName("Video " + SDF.format(new Date()));
        artifact.setLink(String.format(R.CONFIG.get("screen_record_host"), videoName));
        artifact.setExpiresIn(Configuration.getInt(Configuration.Parameter.ARTIFACTS_EXPIRATION_SECONDS));
        return artifact;
    }

    /**
     * Initialize test artifact for upload.
     * 
     * @param sessionLogName - session log file name
     * @return test artifact with session log details
     */
    protected TestArtifactType initSessionLogArtifact(String sessionLogName) {
        TestArtifactType artifact = new TestArtifactType();
        artifact.setName("Session log " + SDF.format(new Date()));
        // TODO: allocate separate configuration property
        artifact.setLink(String.format(R.CONFIG.get("screen_record_host"), sessionLogName));
        artifact.setExpiresIn(Configuration.getInt(Configuration.Parameter.ARTIFACTS_EXPIRATION_SECONDS));
        return artifact;
    }

    protected boolean isVideoEnabled() {
        return R.CONFIG.getBoolean(SpecialKeywords.ENABLE_VIDEO);
    }

}