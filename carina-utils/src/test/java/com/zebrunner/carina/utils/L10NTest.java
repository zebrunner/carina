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

import com.zebrunner.carina.utils.resources.L10N;
import org.testng.Assert;
import org.testng.annotations.Test;

public class L10NTest {

    private static final String KEY = "wish";
    private static final String DEFAULT_VALUE = "good luck";
    private static final String US_VALUE = "good luck";
    private static final String GERMAN_VALUE = "viel Glück";
    private static final String FRANCE_VALUE = "Bonne chance";

    @Test
    public void testDefaultLocaleGetValue() {
        L10N.setLocale("");
        L10N.load();
        String value = L10N.getText(KEY);

        Assert.assertEquals(value, DEFAULT_VALUE, "Default value doesn't equal to " + DEFAULT_VALUE);
    }

    @Test
    public void testUSLocaleGetValue() {
        L10N.setLocale("en_US");
        L10N.load();
        String value = L10N.getText(KEY);

        Assert.assertEquals(value, US_VALUE, "US value doesn't equal to " + GERMAN_VALUE);
    }

    @Test
    public void testGermanyLocaleGetValue() {
        L10N.setLocale("de_DE");
        L10N.load();
        String value = L10N.getText(KEY);

        Assert.assertEquals(value, GERMAN_VALUE, "German value doesn't equal to " + GERMAN_VALUE);
    }

    @Test
    public void testFranceLocaleGetValue() {
        L10N.setLocale("fr_FR");
        L10N.load();

        String value = L10N.getText(KEY);

        Assert.assertEquals(value, FRANCE_VALUE, "France value doesn't equal to " + GERMAN_VALUE);
    }
}
