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

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
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
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringAppiumCommandExecutor;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;

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
            seleniumHost = Configuration.getSeleniumUrl();
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

        LOGGER.debug("capabilities: " + capabilities);

        try {
            EventFiringAppiumCommandExecutor ce = new EventFiringAppiumCommandExecutor(new URL(seleniumHost));
            
            if (mobilePlatformName.equalsIgnoreCase(SpecialKeywords.ANDROID)) {
                driver = new AndroidDriver<AndroidElement>(ce, capabilities);
            } else if (mobilePlatformName.equalsIgnoreCase(SpecialKeywords.IOS)
                    || mobilePlatformName.equalsIgnoreCase(SpecialKeywords.TVOS)) {
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
                    IDriverPool.registerDevice(device);
                }
                // there is no sense to register device in the pool as driver is not started and we don't have custom exception from MCloud
            } 
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
            LOGGER.error("Unable to register device!", e);
            //TODO: try to handle use-case if quit in this place can hangs for minutes!
            LOGGER.error("starting driver quit...");
            driver.quit();
            LOGGER.error("finished driver quit...");
            throw e;
        }

        return driver;
    }

    private DesiredCapabilities getCapabilities(String name) {
        return new MobileCapabilities().getCapability(name);
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
            LOGGER.debug(String.format("Found parameter: %s -> ", paramName).concat(paramValue));
        } else {
            LOGGER.debug(String.format("Param '%s' hasn't been found in debug info: [%s]", paramName, debugInfo));
        }

        return paramValue;
    }
    
}
