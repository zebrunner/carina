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

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;

import com.zebrunner.carina.webdriver.core.capability.AbstractCapabilities;

public class OperaCapabilities extends AbstractCapabilities<MutableCapabilities> {

    @Override
    public MutableCapabilities getCapability(String testName) {
        MutableCapabilities capabilities = new MutableCapabilities();
        initBaseCapabilities(capabilities, testName);
        capabilities.setCapability(CapabilityType.BROWSER_NAME, Browser.OPERA.browserName());

        //TODO: add support for AUTO_DOWNLOAD and PROXY
        return capabilities;
    }
}
