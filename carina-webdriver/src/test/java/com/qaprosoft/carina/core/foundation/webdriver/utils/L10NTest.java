package com.qaprosoft.carina.core.foundation.webdriver.utils;

import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.resources.LocaleReader;
import com.qaprosoft.carina.core.resources.L10N;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Locale;

public class L10NTest {

    private static final String DEFAULT_LOCALE = LocaleReader.init(R.CONFIG.get("locale")).get(0).toString();

    private static final String KEY = "wish";
    private static final String DEFAULT_VALUE = "good luck";
    private static final String US_VALUE = "good luck";
    private static final String GERMAN_VALUE = "viel Glück";
    private static final String FRANCE_VALUE = "Bonne chance";

    @BeforeClass
    public void initL10N() {
        L10N.init();
    }

    @Test
    public void testDefaultLocale() {
        Locale locale = L10N.getDefaultLocale();

        Assert.assertEquals(locale.toString(), DEFAULT_LOCALE, "Default locale doesn't equal to " + DEFAULT_LOCALE);
    }

    @Test
    public void testDefaultLocaleGetValue() {
        String value = L10N.getText(KEY);

        Assert.assertEquals(value, DEFAULT_VALUE, "Default value doesn't equal to " + DEFAULT_VALUE);
    }

    @Test
    public void testUSLocaleGetValue() {
        String value = L10N.getText(KEY, Locale.US);

        Assert.assertEquals(value, US_VALUE, "US value doesn't equal to " + GERMAN_VALUE);
    }

    @Test
    public void testGermanyLocaleGetValue() {
        String value = L10N.getText(KEY, Locale.GERMANY);

        Assert.assertEquals(value, GERMAN_VALUE, "German value doesn't equal to " + GERMAN_VALUE);
    }

    @Test
    public void testFranceLocaleGetValue() {
        String value = L10N.getText(KEY, Locale.FRANCE);

        Assert.assertEquals(value, FRANCE_VALUE, "France value doesn't equal to " + GERMAN_VALUE);
    }
}
