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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

import io.appium.java_client.android.options.UiAutomator2Options;

public class UiAutomator2Capabilities extends AbstractCapabilities<UiAutomator2Options> {

    @Override
    public UiAutomator2Options getCapability(String testName) {
        UiAutomator2Options capabilities = new UiAutomator2Options();
        // this step should be executed before initCapabilities() to be able to override this capabilities by default appium approach.
        setLocaleAndLanguage(capabilities);
        // add capabilities based on dynamic _config.properties variables
        addConfigurationCapabilities(capabilities);
        return capabilities;
    }
}
