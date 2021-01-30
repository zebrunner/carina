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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class FirefoxCapabilities extends AbstractCapabilities {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Generate DesiredCapabilities for Firefox with default Carina FirefoxProfile.
     *
     * @param testName
     *            - String.
     * @return Firefox desired capabilities.
     */
    public DesiredCapabilities getCapability(String testName) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities = initBaseCapabilities(capabilities, BrowserType.FIREFOX, testName);
        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);

        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("media.eme.enabled", true);
        profile.setPreference("media.gmp-manager.updateEnabled", true);

        FirefoxOptions options = new FirefoxOptions().setProfile(profile);
        capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
        LOGGER.debug("Firefox caps: " + capabilities);
        return capabilities;
    }

    /**
     * Generate DesiredCapabilities for Firefox with custom FirefoxProfile.
     *
     * @param testName
     *            - String.
     * @param profile
     *            - FirefoxProfile.
     * @return Firefox desired capabilities.
     */
    public DesiredCapabilities getCapability(String testName, FirefoxProfile profile) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities = initBaseCapabilities(capabilities, BrowserType.FIREFOX, testName);
        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        
        FirefoxOptions options = new FirefoxOptions().setProfile(profile);
        capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
        
        LOGGER.debug("Firefox caps: " + capabilities);
        return capabilities;
    }

}
