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

import com.zebrunner.carina.utils.StringGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringGeneratorTest {

    private static final String ALPHABET_REGEX = "^[a-z]{%d}$";
    private static final String NUMERIC_WORD_REGEX = "^[0-9a-zA-Z]{%d}$";
    private static final String FIXED_NUMERIC_REGEX = "^[0-9]{%d}$";
    private static final String NUMERIC_REGEX = "^[0-9]+$";
    private static final String EMAIL_REGEX = "^[a-z]{10}@gmail.com$";

    private static final int KEY_SIZE = 7;

    private static final String START_UNIQUE_ITEM_NAME = "qatest";

    @Test
    public void testGenerateWord() {
        String word = StringGenerator.generateWord(KEY_SIZE);

        Assert.assertTrue(validate(word, String.format(ALPHABET_REGEX, KEY_SIZE)), word + " wasn't generated properly");
    }

    @Test
    public void testGenerateNumeric() {
        String numeric = StringGenerator.generateNumeric(KEY_SIZE);

        Assert.assertTrue(validate(numeric, String.format(FIXED_NUMERIC_REGEX, KEY_SIZE)), numeric + " wasn't generated properly");
    }

    @Test
    public void testGenerateANWord() {
        String aNWord = StringGenerator.generateWordAN(KEY_SIZE);

        Assert.assertTrue(validate(aNWord, String.format(NUMERIC_WORD_REGEX, KEY_SIZE)), aNWord + " wasn't generated properly");
    }

    @Test
    public void testGenerateEmail() {
        String email = StringGenerator.generateEmail();

        Assert.assertTrue(validate(email, EMAIL_REGEX), email + " wasn't generated properly");
    }

    @Test
    public void testGetUniqueItemName() {
        String uniqueItemName = StringGenerator.getUniqueItemName();

        String[] uniqueItemNameParts = uniqueItemName.split("-");

        Assert.assertEquals(uniqueItemNameParts[0], START_UNIQUE_ITEM_NAME, uniqueItemName + "does not have prefix " + START_UNIQUE_ITEM_NAME);
        Assert.assertTrue(validate(uniqueItemNameParts[1], NUMERIC_REGEX), uniqueItemNameParts[1] + " suffix wasn't generated properly");
    }

    @Test
    public void testGenerateInputParametersWhenNeedToGenerateAllParam() {
        String[] params = { "$generate:" + KEY_SIZE };

        StringGenerator.generateInputParameters(params);

        Assert.assertTrue(validate(params[0], String.format(ALPHABET_REGEX, KEY_SIZE)), params[0] + " wasn't generated properly");
    }

    @Test
    public void testGenerateInputParametersWithoutChanging() {
        String[] params = { "fullParam" };
        String beforeParam = params[0];

        StringGenerator.generateInputParameters(params);

        Assert.assertEquals(params[0], beforeParam, beforeParam + " was changed to " + params[0]);
    }

    @Test
    public void testGenerateInputParametersWhenNeedToGeneratePartialParam() {
        String[] params = { "firstPart_$generate:" + KEY_SIZE };

        String[] paramPartsBefore = params[0].split("\\$");
        String firstPart = paramPartsBefore[0];

        StringGenerator.generateInputParameters(params);

        Assert.assertTrue(params[0].startsWith(firstPart), firstPart + " ready part was changed");

        String generatedPart = params[0].substring(firstPart.length());

        Assert.assertTrue(validate(generatedPart, String.format(ALPHABET_REGEX, KEY_SIZE)), params[0] + " wasn't generated properly");
    }

    private boolean validate(String str, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        return matcher.matches();
    }

}
