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
package com.zebrunner.carina.webdriver.core.capability.impl.desktop;

import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;

import com.zebrunner.carina.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.remote.options.SupportsBrowserNameOption;
import io.appium.java_client.safari.options.SafariOptions;

public class SafariCapabilities extends AbstractCapabilities<SafariOptions> {

    @Override
    public SafariOptions getCapability(String testName) {
        SafariOptions safariOptions = new SafariOptions();
        // we want to test safari not only on IOS, but on MAC
        safariOptions.setPlatformName(Platform.ANY.toString());
        // it is strange that safari options is not contains browser
        safariOptions.setCapability(SupportsBrowserNameOption.BROWSER_NAME_OPTION, Browser.SAFARI.browserName());
        safariOptions.setCapability(CapabilityType.BROWSER_NAME, Browser.SAFARI.browserName());
        initBaseCapabilities(safariOptions, testName);
        return safariOptions;
    }
}
