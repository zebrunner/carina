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

import com.zebrunner.carina.utils.LogicUtils;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

public class LogicUtilsTest {

    private static final String URL1 = "http://www.example.com/item/$ignore?param=lamp";
    private static final String URL1_1 = "http://www.example.com/item/$ignore?param=lamp";
    private static final String URL1_2 = "http://www.example.com/item/lamp/led";
    private static final String URL1_UPPER = "http://www.EXAMPLE.com/ITEM/$ignore?param=lamp";
    private static final String URL2 = "http://www.shop.com/item?param=laptop";

    private static final boolean[] CASES1 = {true, true, true};
    private static final boolean[] CASES2 = {true, false, true};

    @Test
    public void testDifferentUrlLevels() {
        Assert.assertTrue(LogicUtils.isURLEqual(URL1, URL1_2), URL1 + " is not equal " + URL1_2);
    }

    @Test
    public void testUrlsIsEqual() {
        Assert.assertTrue(LogicUtils.isURLEqual(URL1, URL1_1), URL1 + " is different than " + URL1_1);
    }

    @Test
    public void testUrlsIsEqualIgnoreCase() {
        Assert.assertTrue(LogicUtils.isURLEqual(URL1, URL1_UPPER), URL1 + " is different than " + URL1_UPPER);
    }

    @Test
    public void testAllIsTrue() {
        Assert.assertTrue(LogicUtils.isAllTrue(CASES1), Arrays.toString(CASES1) + " doesn't not contains all true elements");
    }

    @Test
    public void testNotAllIsTrue() {
        Assert.assertFalse(LogicUtils.isAllTrue(CASES2), Arrays.toString(CASES2) + " contains all true elements");
    }


    @Test
    public void testUrlsIsNotEqual() {
        Assert.assertFalse(LogicUtils.isURLEqual(URL1, URL2), URL1 + " is equal to " + URL2);
    }

    @Test
    public void testSelectRandomElement() {
        List<WebElement> webElements = Arrays.asList(
                mock(WebElement.class),
                mock(WebElement.class),
                mock(WebElement.class),
                mock(WebElement.class),
                mock(WebElement.class),
                mock(WebElement.class)
        );
        Assert.assertNotNull(LogicUtils.selectRandomElement(webElements));
    }

}
