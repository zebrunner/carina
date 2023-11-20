/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.report.testrail;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.remote.CapabilityType;

import com.zebrunner.carina.webdriver.config.WebDriverConfiguration;

public interface ITestCases {
    ThreadLocal<List<String>> casesIds = ThreadLocal.withInitial(ArrayList::new);

    default List<String> getCases() {
        return casesIds.get();
    }

    default void setCases(String... cases) {
        for (String _case : cases) {
            casesIds.get().add(_case);
        }
    }

    default void clearCases() {
        casesIds.set(new ArrayList<>());
    }

    default boolean isValidPlatform(String platform) {
        return platform.equalsIgnoreCase(WebDriverConfiguration.getCapability(CapabilityType.PLATFORM_NAME).orElse("*")) || platform.isEmpty();
    }
    
    default boolean isValidLocale(String locale) {
        return locale.equalsIgnoreCase(WebDriverConfiguration.getLocale().getCountry()) || locale.isEmpty();
    }
}
