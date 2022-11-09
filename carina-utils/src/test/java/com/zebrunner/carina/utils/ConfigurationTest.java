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
package com.zebrunner.carina.utils;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;
import com.zebrunner.carina.utils.R;

/**
 * Tests for {@link Configuration}
 */
public class ConfigurationTest {

    @AfterClass
    public void tearDown() {
        R.CONFIG.put(SpecialKeywords.PLATFORM_NAME, "");
        R.CONFIG.put(Parameter.BROWSER.getKey(), "chrome");
    }

    @Test
    public void testConfigOverride() {
        R.CONFIG.put("env", "UNITTEST");
        Assert.assertEquals(Configuration.getEnvArg("override"), "override_me");
        R.CONFIG.put("UNITTEST.override", "i_am_overriden");
        Assert.assertEquals(Configuration.getEnvArg("override"), "i_am_overriden");
    }

    @Test
    public void testGetEnvArg() {
        R.CONFIG.put("env", "QA");
        Assert.assertEquals(Configuration.getEnvArg("url"), "local");
        R.CONFIG.put("env", "PROD");
        Assert.assertEquals(Configuration.getEnvArg("url"), "remote");
    }

    @Test(expectedExceptions = { RuntimeException.class })
    public void testInvalidConfigValidation() {
        R.CONFIG.put("selenium_url", "{must_override}");
        Configuration.validateConfiguration();
    }

    @Test
    public void testConfigurationPlacehodler() {
        R.CONFIG.put("env", "STG");
        Assert.assertEquals(Configuration.getEnvArg("url"), "http://localhost:8081");
        Assert.assertEquals(Configuration.get(Parameter.URL), "http://localhost:8081");
    }

    @Test
    public void testAdbExecTimeout() {
        R.CONFIG.put(SpecialKeywords.ADB_EXEC_TIMEOUT, "30000");

        Assert.assertEquals(Configuration.getAdbExecTimeout(), 30000, "capabilities.adbExecTimeout wasn't set");
    }

    @Test
    public void testPlatformVersion() {
        R.CONFIG.put(SpecialKeywords.PLATFORM_VERSION, "11.0.0");

        Assert.assertEquals(Configuration.getPlatformVersion(), "11.0.0", "capabilities.platformVersion wasn't set");
    }

    @Test
    public void testBrowser() {
        R.CONFIG.put(Parameter.BROWSER.getKey(), "firefox");

        Assert.assertEquals(Configuration.getBrowser(), "firefox", "browser wasn't set");
    }

    @Test
    public void testBrowserVersion() {
        R.CONFIG.put("capabilities.browserVersion", "88.0.0");

        Assert.assertEquals(Configuration.getBrowserVersion(), "88.0.0", "capabilities.browserVersion wasn't set");
    }

    @Test
    public void testDeviceType() {
        R.CONFIG.put(SpecialKeywords.PLATFORM_NAME, "Android");

        Assert.assertEquals(Configuration.getDriverType(), "mobile", "Can't find out device type");
    }

    
    // @Test
    // public void testDesktopDeviceTypeWithMutableCapabilities() {
    // MutableCapabilities capabilities = new MutableCapabilities();
    // capabilities.setCapability(CapabilityType.BROWSER_NAME, "safari");
    // capabilities.setCapability(CapabilityType.PLATFORM_NAME, SpecialKeywords.MAC);
    //
    // Assert.assertEquals(Configuration.getDriverType(capabilities), "desktop", "Can't find out device type");
    // }
     
    @Test
    public void testMobileApp() {
        String mobileApp = "https://qaprosoft.s3-us-west-2.amazonaws.com/carinademoexample.apk";

        Configuration.setMobileApp(mobileApp);

        Assert.assertEquals(Configuration.getMobileApp(), mobileApp, "capabilities.app wasn't set");
    }

    @Test
    public void testGetCapability() {
        R.CONFIG.put(SpecialKeywords.PLATFORM_NAME, "Android");

        Assert.assertEquals(Configuration.getCapability("platformName"), "Android",
                Configuration.getCapability("platformName") + " doesn't equal to Android");
    }

    @Test
    public void testAsString() {
        String configStr = Configuration.asString();
        String[] configLines = configStr.split("\n");
        boolean isDriverConfig = false;
        for (int i = 0; i < configLines.length - 1; i++) {
            if (configLines[i].contains("Test configuration")) {
                isDriverConfig = false;
                continue;
            }
            if (configLines[i].contains("Driver capabilities")) {
                isDriverConfig = true;
                continue;
            }
            if (configLines[i].equals(""))
                continue;
            if (isDriverConfig) {
                if (!configLines[i].startsWith("capabilities."))
                    Assert.fail("Driver config: " + configLines[i] + " doesn't start with capabilities.");
            }
            if (!configLines[i].contains("="))
                Assert.fail("Config line: " + configLines[i] + " doesn't have format: key=value");
        }
    }

}
