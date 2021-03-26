package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile.MobileCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MobileCapabilitiesTest {

    private static final String PLATFORM_NAME_KEY = SpecialKeywords.PLATFORM_NAME;
    private static final String LOCALE_KEY = Configuration.Parameter.LOCALE.getKey();
    private static final String LANGUAGE_KEY = "language";

    private static final String LOCALE = "US";
    private static final String LOCALE_LANGUAGE = "en_US";
    private static final String LANGUAGE = "en";

    @Test(dependsOnGroups = {"AppleTVTestClass"})
    public void getCapabilityWithLocaleTest() {
        R.CONFIG.put(LOCALE_KEY, LOCALE, true);
        R.CONFIG.put(LANGUAGE_KEY, "", true);

        String testName = "mobile - getCapabilityWithLocaleTest";

        MobileCapabilities mobileCapabilities = new MobileCapabilities();
        DesiredCapabilities capabilities = mobileCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getCapability(LOCALE_KEY), LOCALE, "Locale capability is not valid");
        Assert.assertNull(capabilities.getCapability(LANGUAGE_KEY), "Language capability is not empty");
    }

    @Test(dependsOnGroups = {"AppleTVTestClass"})
    public void getCapabilityWithLocaleAndLanguageSeparatelyTest() {
        R.CONFIG.put(LOCALE_KEY, LOCALE, true);
        R.CONFIG.put(LANGUAGE_KEY, LANGUAGE, true);

        String testName = "mobile - getCapabilityWithLocaleAndLanguageSeparatelyTest";

        MobileCapabilities mobileCapabilities = new MobileCapabilities();
        DesiredCapabilities capabilities = mobileCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getCapability(LOCALE_KEY), LOCALE, "Locale capability is not valid");
        Assert.assertEquals(capabilities.getCapability(LANGUAGE_KEY), LANGUAGE, "Language capability is not valid");
    }

    @Test(dependsOnGroups = {"AppleTVTestClass"})
    public void getAndroidCapabilityWithLocaleAndLanguageTogetherTest() {
        R.CONFIG.put(PLATFORM_NAME_KEY, "Android", true);
        R.CONFIG.put(LOCALE_KEY, LOCALE_LANGUAGE, true);

        String testName = "mobile - getAndroidCapabilityWithLocaleAndLanguageTogetherTest";

        MobileCapabilities mobileCapabilities = new MobileCapabilities();
        DesiredCapabilities capabilities = mobileCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getCapability(LOCALE_KEY), LOCALE, "Locale capability is not valid");
        Assert.assertEquals(capabilities.getCapability(LANGUAGE_KEY), LANGUAGE, "Language capability is not valid");
    }

    @Test(dependsOnGroups = {"AppleTVTestClass"})
    public void getIOSCapabilityWithLocaleAndLanguageTogetherTest() {
        R.CONFIG.put(PLATFORM_NAME_KEY, "IOS", true);
        R.CONFIG.put(LOCALE_KEY, LOCALE_LANGUAGE, true);

        String testName = "mobile - getIOSCapabilityWithLocaleAndLanguageTogetherTest";

        MobileCapabilities mobileCapabilities = new MobileCapabilities();
        DesiredCapabilities capabilities = mobileCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getCapability(LOCALE_KEY), LOCALE_LANGUAGE, "Locale capability is not valid");
        Assert.assertEquals(capabilities.getCapability(LANGUAGE_KEY), LANGUAGE, "Language capability is not valid");
    }
}
