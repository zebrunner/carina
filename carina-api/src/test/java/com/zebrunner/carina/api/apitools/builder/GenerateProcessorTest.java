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
package com.zebrunner.carina.api.apitools.builder;

import com.zebrunner.carina.api.apitools.builder.GenerateProcessor;
import com.zebrunner.carina.api.apitools.util.GenerationUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Properties;
import java.util.regex.Pattern;

public class GenerateProcessorTest {

    @Test
    public void testWordMatcher() {
        GenerateProcessor generateProcessor = new GenerateProcessor();
        Properties properties = new Properties();
        String text = "generate_word(5)";
        String reg = "[a-zA-Z]{5}";
        String key = "username";
        properties.setProperty(key, text);
        Pattern pattern = Pattern.compile(reg);
        String actual = generateProcessor.process(properties).getProperty(key);
        Assert.assertTrue(pattern.matcher(actual).matches());
        Assert.assertNotEquals(actual, text);
    }

    @Test
    public void testNumberMatcher() {
        GenerateProcessor generateProcessor = new GenerateProcessor();
        Properties properties = new Properties();
        String text = "generate_number(10)";
        String reg = "[0-9]{10}";
        String key = "number";
        properties.setProperty(key, text);
        Pattern pattern = Pattern.compile(reg);
        String actual = generateProcessor.process(properties).getProperty(key);
        Assert.assertTrue(pattern.matcher(actual).matches());
        Assert.assertNotEquals(actual, text);
    }

    @Test
    public void testDateMatcherWithPositiveOffSet() {
        GenerateProcessor generateProcessor = new GenerateProcessor();
        Properties properties = new Properties();
        String dateFormat = "yyyy-MM-dd";
        int offSet = 4;
        String text = String.format("generate_date(%s;%d)", dateFormat, offSet);
        String reg = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
        String key = "date";
        properties.setProperty(key, text);
        Pattern pattern = Pattern.compile(reg);
        String actual = generateProcessor.process(properties).getProperty(key);
        Assert.assertTrue(pattern.matcher(actual).matches());
        Assert.assertNotEquals(actual, text);
        Assert.assertEquals(actual, GenerationUtil.generateTime(dateFormat, offSet, Calendar.DAY_OF_YEAR));
    }

    @Test
    public void testDateMatcherWithNegativeOffSet() {
        GenerateProcessor generateProcessor = new GenerateProcessor();
        Properties properties = new Properties();
        String dateFormat = "yyyy-MM-dd";
        int offSet = -25;
        String text = String.format("generate_date(%s;%d)", dateFormat, offSet);
        String reg = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
        String key = "date";
        properties.setProperty(key, text);
        Pattern pattern = Pattern.compile(reg);
        String actual = generateProcessor.process(properties).getProperty(key);
        Assert.assertTrue(pattern.matcher(actual).matches());
        Assert.assertNotEquals(actual, text);
        Assert.assertEquals(actual, GenerationUtil.generateTime(dateFormat, offSet, Calendar.DAY_OF_YEAR));
    }

    @Test
    public void testMixedMatcher() {
        GenerateProcessor generateProcessor = new GenerateProcessor();
        Properties properties = new Properties();
        String dateFormat = "yyyy-MM-dd";
        int offSet = -25;
        String text = String.format("generate_word(2)generate_date(%s;%d)generate_word(10)generate_number(5)generate_date(%s;%d)generate_number(3)",
                dateFormat, offSet, dateFormat, offSet);
        String key = "date";
        properties.setProperty(key, text);
        String actual = generateProcessor.process(properties).getProperty(key);
        Assert.assertEquals(actual.length(), 40);
        Assert.assertNotEquals(actual, text);
        Assert.assertFalse(actual.contains("generate_word"));
        Assert.assertFalse(actual.contains("generate_number"));
        Assert.assertFalse(actual.contains("generate_date"));
    }
}
