/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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

import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.EspressoCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.UiAutomator2Capabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.XCUITestCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringAppiumCommandExecutor;
import com.zebrunner.agent.core.registrar.Artifact;
import com.zebrunner.carina.commons.artifact.IArtifactManager;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.exception.InvalidConfigurationException;
import com.zebrunner.carina.utils.mobile.ArtifactProvider;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.internal.CapabilityHelpers;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.safari.SafariDriver;

/**
 * MobileFactory creates instance {@link WebDriver} for mobile testing.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class MobileFactory extends AbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Map<String, String> CACHE_MOBILE_APP_LINKS = new ConcurrentHashMap<>();

    /**
     * Get a direct (pre-sign) link to the application.
     * If the passed link was previously accessed to get a direct link,
     * it will be taken from the cache, otherwise it will be generated and cached.
     *
     * @param originalAppLink link of which a direct link will be made
     * @return direct link to the mobile application
     */
    public static String getAppLink(String originalAppLink) {
        String updatedMobileApp;
        if (!CACHE_MOBILE_APP_LINKS.containsKey(originalAppLink)) {
            IArtifactManager artifactProvider = ArtifactProvider.getInstance();
            updatedMobileApp = artifactProvider.getDirectLink(originalAppLink);
            CACHE_MOBILE_APP_LINKS.put(originalAppLink, updatedMobileApp);
            LOGGER.debug("For the 'app' capability with current value '{}', will be cached link: {}", originalAppLink, updatedMobileApp);
        } else {
            updatedMobileApp = CACHE_MOBILE_APP_LINKS.get(originalAppLink);
            LOGGER.debug("Original value of capability 'app': '{}' will be replaced by cached link: {}", originalAppLink, updatedMobileApp);
        }
        return updatedMobileApp;
    }

    @Override
    public WebDriver create(String name, Capabilities capabilities, String seleniumHost) {
        if (seleniumHost == null) {
            seleniumHost = Configuration.getSeleniumUrl();
        }
        LOGGER.debug("Selenium URL: {}", seleniumHost);

        // if inside capabilities only singly "udid" capability then generate default one and append udid
        if (isCapabilitiesEmpty(capabilities)) {
            capabilities = getCapabilities();
        } else if (capabilities.asMap().size() == 1 &&
                CapabilityHelpers.getCapability(capabilities, MobileCapabilityType.UDID, String.class) != null) {
            String udid = CapabilityHelpers.getCapability(capabilities, MobileCapabilityType.UDID, String.class);
            MutableCapabilities capsWithUdid = getCapabilities();
            capsWithUdid.setCapability(MobileCapabilityType.UDID, udid);
            capabilities = capabilities.merge(capsWithUdid);
            LOGGER.debug("Appended udid to capabilities: {}", capabilities);
        }

        Object mobileAppCapability = capabilities.getCapability(MobileCapabilityType.APP);
        if (mobileAppCapability != null) {
            MutableCapabilities capsWithCachedApp = new MutableCapabilities();
            capsWithCachedApp.setCapability("appium:" + MobileCapabilityType.APP, getCachedAppLink(String.valueOf(mobileAppCapability)));
            capabilities = capabilities.merge(capsWithCachedApp);
        }

        LOGGER.debug("capabilities: {}", capabilities);

        WebDriver driver = null;
        try {
            EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(new URL(seleniumHost));
            String platformName = CapabilityHelpers.getCapability(capabilities, CapabilityType.PLATFORM_NAME, String.class);
            String browserName = CapabilityHelpers.getCapability(capabilities, CapabilityType.BROWSER_NAME, String.class);

            if (SpecialKeywords.ANDROID.equalsIgnoreCase(platformName)) {
                driver = new AndroidDriver(ce, capabilities);
            } else if (SpecialKeywords.IOS.equalsIgnoreCase(platformName)) {
                if (Browser.SAFARI.browserName().equalsIgnoreCase(browserName)) {
                    driver = new SafariDriver(ce, capabilities);
                } else if (StringUtils.isBlank(browserName)) {
                    driver = new IOSDriver(ce, capabilities);
                } else {
                    throw new InvalidConfigurationException("Unsupported browser for IOS: " + browserName);
                }
            } else if (SpecialKeywords.TVOS.equalsIgnoreCase(platformName)) {
                driver = new IOSDriver(ce, capabilities);
            } else {
                throw new InvalidConfigurationException("Unsupported mobile platform: " + platformName);
            }
        } catch (MalformedURLException e) {
            throw new UncheckedIOException("Malformed selenium URL!", e);
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
                    IDriverPool.registerDevice(device);
                }
                // there is no sense to register device in the pool as driver is not started and we don't have custom exception from MCloud
            } 
            throw e;
        }

        try {
            Device device = new Device(((HasCapabilities) driver).getCapabilities());
            IDriverPool.registerDevice(device);
            // will be performed just in case uninstall_related_apps flag marked as true
            device.uninstallRelatedApps();
        } catch (Exception e) {
            // use-case when something wrong happen during initialization and registration device information.
            // the most common problem might be due to the adb connection problem
            
            // make sure to initiate driver quit
            LOGGER.error("Unable to register device!", e);
            //TODO: try to handle use-case if quit in this place can hangs for minutes!
            LOGGER.error("starting driver quit...");
            driver.quit();
            LOGGER.error("finished driver quit...");
            throw e;
        }
        return driver;
    }

    /**
     * Get cached link to the app
     * 
     * @param appLink original link
     * @return cached (pre-signed) link
     */
    private String getCachedAppLink(String appLink) {
        String updatedMobileApp;
        if (!CACHE_MOBILE_APP_LINKS.containsKey(appLink)) {
            IArtifactManager artifactProvider = ArtifactProvider.getInstance();
            updatedMobileApp = artifactProvider.getDirectLink(appLink);
            CACHE_MOBILE_APP_LINKS.put(appLink, updatedMobileApp);
            LOGGER.debug("For the 'app' capability with current value '{}', will be cached link: {}", appLink, updatedMobileApp);
            Artifact.attachReferenceToTestRun("app", updatedMobileApp);
        } else {
            updatedMobileApp = CACHE_MOBILE_APP_LINKS.get(appLink);
            LOGGER.debug("Original value of capability 'app': '{}' will be replaced by cached link: {}", appLink, updatedMobileApp);
        }
        return updatedMobileApp;
    }

    private MutableCapabilities getCapabilities() {
        String platform = R.CONFIG.get("capabilities." + CapabilityType.PLATFORM_NAME);
        String automationName = R.CONFIG.get("capabilities." + MobileCapabilityType.AUTOMATION_NAME);

        AbstractCapabilities<?> capabilities = null;

        if (AutomationName.ESPRESSO.equalsIgnoreCase(automationName)) {
            capabilities = new EspressoCapabilities();
        } else if (SpecialKeywords.ANDROID.equalsIgnoreCase(platform)) {
            capabilities = new UiAutomator2Capabilities();
        } else if (platform.equalsIgnoreCase(SpecialKeywords.IOS)
                || platform.equalsIgnoreCase(SpecialKeywords.TVOS)) {
            capabilities = new XCUITestCapabilities();
        } else {
            throw new InvalidConfigurationException("Unsupported platform: " + platform);
        }
        return capabilities.getCapabilities();
    }

    /**
     * Method to extract debug info in case exception has been thrown during app installation
     * 
     * @param exceptionMsg List&lt;WebElement&gt;
     * @return debug info
     */
    private String getDebugInfo(String exceptionMsg) {
        String debugInfoPattern = "\\[\\[\\[(.*)\\]\\]\\]";

        Pattern p = Pattern.compile(debugInfoPattern);
        Matcher m = p.matcher(exceptionMsg);
        String debugInfo = "";
        if (m.find()) {
            debugInfo = m.group(1);
            LOGGER.debug("Extracted debug info: {}", debugInfo);
        } else {
            LOGGER.debug("Debug info hasn't been found");
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
     * @param debugInfo String
     * @param paramName String
     * @return paramValue
     */
    private String getParamFromDebugInfo(String debugInfo, String paramName) {
        String paramPattern = String.format("-%s ([^\\s]*)", paramName);

        Pattern p = Pattern.compile(paramPattern);
        Matcher m = p.matcher(debugInfo);
        String paramValue = "";
        if (m.find()) {
            paramValue = m.group(1);
            LOGGER.debug("Found parameter: {} -> {}", paramName, paramValue);
        } else {
            LOGGER.debug("Param '{}' hasn't been found in debug info: [{}]", paramName, debugInfo);
        }

        return paramValue;
    }
}
