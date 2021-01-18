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
package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileCapabilies;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringAppiumCommandExecutor;
import com.qaprosoft.carina.core.foundation.webdriver.listener.MobileRecordingListener;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import io.appium.java_client.android.AndroidStopScreenRecordingOptions;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions.VideoQuality;
import io.appium.java_client.ios.IOSStopScreenRecordingOptions;

/**
 * MobileFactory creates instance {@link WebDriver} for mobile testing.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class MobileFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String name, DesiredCapabilities capabilities, String seleniumHost) {

        if (seleniumHost == null) {
            seleniumHost = Configuration.get(Configuration.Parameter.SELENIUM_HOST);
        }

        String mobilePlatformName = Configuration.getPlatform(capabilities);

        // TODO: refactor to be able to remove SpecialKeywords.CUSTOM property completely

        // use comparison for custom_capabilities here to localize as possible usage of CUSTOM attribute
        String customCapabilities = Configuration.get(Parameter.CUSTOM_CAPABILITIES);
        if (!customCapabilities.isEmpty()
                && (customCapabilities.toLowerCase().contains("localhost") || customCapabilities.toLowerCase().contains("browserstack") || customCapabilities.toLowerCase().contains("saucelabs"))) {
            mobilePlatformName = SpecialKeywords.CUSTOM;
        }

        LOGGER.debug("selenium: " + seleniumHost);

        RemoteWebDriver driver = null;
        // if inside capabilities only singly "udid" capability then generate default one and append udid
        if (isCapabilitiesEmpty(capabilities)) {
            capabilities = getCapabilities(name);
        } else if (capabilities.asMap().size() == 1 && capabilities.getCapability("udid") != null) {
            String udid = capabilities.getCapability("udid").toString();
            capabilities = getCapabilities(name);
            capabilities.setCapability("udid", udid);
            LOGGER.debug("Appended udid to cpabilities: " + capabilities);
        }

        try {
            // TODO: investigate possibility to move this custom listeners logic onto the selenium hub layer
            // So mcloud can support video recording for any framework 
            EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(new URL(seleniumHost));
            
            if (mobilePlatformName.equalsIgnoreCase(SpecialKeywords.ANDROID)) {
                if (isEnabled(SpecialKeywords.ENABLE_VIDEO) && Configuration.getBoolean(Parameter.DRIVER_RECORDER)) {
                    // Details about available parameters
                    // https://github.com/appium/java-client/blob/master/src/main/java/io/appium/java_client/android/AndroidStartScreenRecordingOptions.java
                    AndroidStartScreenRecordingOptions o1 = new AndroidStartScreenRecordingOptions()
                            .withTimeLimit(Duration.ofSeconds(Configuration.getInt(Parameter.SCREEN_RECORD_DURATION)));
                    boolean enableBugReport = false;
                    try {
                        enableBugReport = Configuration.getBoolean(Parameter.ANDROID_ENABLE_BUG_REPORT);
                    } catch (Exception e) {
                        LOGGER.error("Enable bug report value should be boolean.", e);
                    }
                    if (enableBugReport) {
                        LOGGER.debug("Bug report will be enabled.");
                        o1.enableBugReport();
                    }
                    String videoSize = Configuration.get(Parameter.ANDROID_SCREEN_RECORDING_SIZE);
                    if (!videoSize.isEmpty()) {
                        LOGGER.debug("Screen recording size will be set to : " + videoSize);
                        o1.withVideoSize(videoSize);
                    }
                    String bitRateSt = Configuration.get(Parameter.ANDROID_SCREEN_RECORDING_BITRATE);
                    if (!bitRateSt.isEmpty()) {
                        try {
                            int bitRate = Integer.parseInt(bitRateSt);
                            LOGGER.debug("Screen recording bit rate will be set to : " + bitRate);
                            o1.withBitRate(bitRate);
                        } catch (Exception e) {
                            LOGGER.error("Screen record bitrate value should be integer.", e);
                        }
                    }
                    AndroidStopScreenRecordingOptions o2 = new AndroidStopScreenRecordingOptions();
                    
                    ce.getListeners()
                            .add(new MobileRecordingListener<AndroidStartScreenRecordingOptions, AndroidStopScreenRecordingOptions>(ce, o1, o2));
                }

                driver = new AndroidDriver<AndroidElement>(ce, capabilities);

            } else if (mobilePlatformName.equalsIgnoreCase(SpecialKeywords.IOS)
                    || mobilePlatformName.equalsIgnoreCase(SpecialKeywords.TVOS)) {
                if (isEnabled(SpecialKeywords.ENABLE_VIDEO) && Configuration.getBoolean(Parameter.DRIVER_RECORDER)) {
                    // Details about available parameters
                    // https://github.com/appium/java-client/blob/master/src/main/java/io/appium/java_client/ios/IOSStartScreenRecordingOptions.java
                    IOSStartScreenRecordingOptions o1 = new IOSStartScreenRecordingOptions()
                            .withVideoQuality(VideoQuality.valueOf(Configuration.get(Parameter.IOS_SCREEN_RECORDING_QUALITY)))
                            .withVideoType(Configuration.get(Parameter.IOS_SCREEN_RECORDING_CODEC))
                            .withTimeLimit(Duration.ofSeconds(Configuration.getInt(Parameter.SCREEN_RECORD_DURATION)));

                    String fpsSt = Configuration.get(Parameter.IOS_SCREEN_RECORDING_FPS);
                    if (!fpsSt.isEmpty()) {
                        try {
                            int fps = Integer.parseInt(fpsSt);
                            LOGGER.debug("Screen recording fps value will be set to : " + fps);
                            o1.withFps(fps);
                        } catch (Exception e) {
                            LOGGER.error("Screen recording fps value should be integer between 1..60", e);
                        }
                    }

                    if (!Configuration.get(Parameter.VIDEO_SCALE).isEmpty()) {
                        LOGGER.debug("Video scale option will be set to " + Configuration.get(Parameter.VIDEO_SCALE));
                        o1.withVideoScale(Configuration.get(Parameter.VIDEO_SCALE));
                    }
                    
                    IOSStopScreenRecordingOptions o2 = new IOSStopScreenRecordingOptions();

                    ce.getListeners().add(new MobileRecordingListener<IOSStartScreenRecordingOptions, IOSStopScreenRecordingOptions>(ce, o1, o2));
                }

                driver = new IOSDriver<IOSElement>(ce, capabilities);

            } else if (mobilePlatformName.equalsIgnoreCase(SpecialKeywords.CUSTOM)) {
                // that's a case for custom mobile capabilities like browserstack or saucelabs
                driver = new RemoteWebDriver(new URL(seleniumHost), capabilities);
            } else {
                throw new RuntimeException("Unsupported mobile platform: " + mobilePlatformName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        } catch (Exception e) {
            Device device = IDriverPool.nullDevice;
            LOGGER.debug("STF is enabled. Debug info will be extracted from the exception.");
            if (e != null) {
                String debugInfo = getDebugInfo(e.getMessage());
                if (!debugInfo.isEmpty()) {
                    String udid = getUdidFromDebugInfo(debugInfo);
                    String deviceName = getParamFromDebugInfo(debugInfo, "name");
                    device = new Device();
                    device.setUdid(udid);
                    device.setName(deviceName);
                } else {
                    device = new Device(capabilities);
                }
            } 
            IDriverPool.registerDevice(device);
            throw e;
        }

        try {
            Device device = new Device(driver.getCapabilities());
            IDriverPool.registerDevice(device);
            // will be performed just in case uninstall_related_apps flag marked as true
            device.uninstallRelatedApps();
        } catch (Exception e) {
            // use-case when something wrong happen during initialization and registration device information.
            // the most common problem might be due to the adb connection problem
            
            // make sure to initiate driver quit
            driver.quit();
            throw e;
        }

        return driver;
    }

    private DesiredCapabilities getCapabilities(String name) {
        return new MobileCapabilies().getCapability(name);
    }

    /**
     * Method to extract debug info in case exception has been thrown during app installation
     * 
     * @param exceptionMsg
     * @return debug info
     */
    private String getDebugInfo(String exceptionMsg) {
        String debugInfoPattern = "\\[\\[\\[(.*)\\]\\]\\]";

        Pattern p = Pattern.compile(debugInfoPattern);
        Matcher m = p.matcher(exceptionMsg);
        String debugInfo = "";
        if (m.find()) {
            debugInfo = m.group(1);
            LOGGER.debug("Extracted debug info: ".concat(debugInfo));
        } else {
            LOGGER.debug("Debug info hasn'been found");
        }
        return debugInfo;
    }

    private String getUdidFromDebugInfo(String debugInfo) {
        return getParamFromDebugInfo(debugInfo, "udid");
    }

    /**
     * Method to extract specific parameter from debug info in case STF enabled
     * Debug info example: [[[DEBUG info: /opt/android-sdk-linux/platform-tools/adb -P 5037 -s 4d002c7f5b328095 shell pm install -r
     * /data/local/tmp/appium_cache/642637a49a85a430df0f3c4c1b2dd36022c83df4.apk --udid 4d002c7f5b328095 --name Samsung_Galaxy_Note3]]]
     * Example: --{paramName} {paramValue}
     * 
     * @param debugInfo
     * @param paramName
     * @return paramValue
     */
    private String getParamFromDebugInfo(String debugInfo, String paramName) {
        String paramPattern = String.format("-%s ([^\\s]*)", paramName);

        Pattern p = Pattern.compile(paramPattern);
        Matcher m = p.matcher(debugInfo);
        String paramValue = "";
        if (m.find()) {
            paramValue = m.group(1);
            LOGGER.debug(String.format("Found parameter: %s -> ", paramName).concat(paramValue));
        } else {
            LOGGER.debug(String.format("Param '%s' hasn't been found in debug info: [%s]", paramName, debugInfo));
        }

        return paramValue;
    }
    
}