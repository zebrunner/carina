package com.qaprosoft.carina.core.utils;

import com.qaprosoft.carina.core.foundation.utils.LogicUtils;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

public class LogicUtilsTest {

    private static final String URL1 = "http://www.example.com/item/$ignore?param=lamp";
    private static final String URL1_1 = "http://www.example.com/item/$ignore?param=lamp";
    private static final String URL1_UPPER = "http://www.EXAMPLE.com/ITEM/$ignore?param=lamp";
    private static final String URL2 = "http://www.shop.com/item?param=laptop";

    private static final boolean[] CASES1 = {true, true, true};
    private static final boolean[] CASES2 = {true, false, true};

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
