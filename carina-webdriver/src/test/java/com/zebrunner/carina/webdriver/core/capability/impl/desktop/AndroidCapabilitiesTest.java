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

import com.zebrunner.carina.webdriver.core.capability.impl.mobile.UiAutomator2Capabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.R;

import io.appium.java_client.android.options.UiAutomator2Options;

public class AndroidCapabilitiesTest {

    private static final String PLATFORM_NAME_KEY = SpecialKeywords.PLATFORM_NAME;
    private static final String LOCALE_KEY = Configuration.Parameter.LOCALE.getKey();
    private static final String LANGUAGE_KEY = "language";

    private static final String LOCALE = "US";
    private static final String LOCALE_LANGUAGE = "en_US";
    private static final String LANGUAGE = "en";

    @Test// (dependsOnGroups = { "AppleTVTestClass" })
    public void getCapabilityWithLocaleTest() {
        R.CONFIG.put(LOCALE_KEY, LOCALE, true);
        R.CONFIG.put(LANGUAGE_KEY, "", true);

        String testName = "mobile - getCapabilityWithLocaleTest";

        UiAutomator2Options capabilities = new UiAutomator2Capabilities().getCapability(testName);

        Assert.assertEquals(capabilities.getCapability(LOCALE_KEY), LOCALE, "Locale capability is not valid");
        Assert.assertNull(capabilities.getCapability(LANGUAGE_KEY), "Language capability is not empty");
    }

    @Test(dependsOnGroups = { "AppleTVTestClass" })
    public void getCapabilityWithLocaleAndLanguageSeparatelyTest() {
        R.CONFIG.put(PLATFORM_NAME_KEY, "Android", true);
        R.CONFIG.put(LOCALE_KEY, LOCALE, true);
        R.CONFIG.put(LANGUAGE_KEY, LANGUAGE, true);

        String testName = "mobile - getCapabilityWithLocaleAndLanguageSeparatelyTest";

        UiAutomator2Options capabilities = new UiAutomator2Capabilities().getCapability(testName);

        Assert.assertEquals(capabilities.getCapability(LOCALE_KEY), LOCALE, "Locale capability is not valid");
        Assert.assertEquals(capabilities.getCapability(LANGUAGE_KEY), LANGUAGE, "Language capability is not valid");
    }

    @Test(dependsOnGroups = { "AppleTVTestClass" })
    public void getAndroidCapabilityWithLocaleAndLanguageTogetherTest() {
        R.CONFIG.put(PLATFORM_NAME_KEY, "Android", true);
        R.CONFIG.put(LOCALE_KEY, LOCALE_LANGUAGE, true);

        String testName = "mobile - getAndroidCapabilityWithLocaleAndLanguageTogetherTest";

        UiAutomator2Options capabilities = new UiAutomator2Capabilities().getCapability(testName);

        Assert.assertEquals(capabilities.getCapability(LOCALE_KEY), LOCALE, "Locale capability is not valid");
        Assert.assertEquals(capabilities.getCapability(LANGUAGE_KEY), LANGUAGE, "Language capability is not valid");
    }

}
