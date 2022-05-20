package com.qaprosoft.carina.core.foundation.webdriver.locator;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.CaseInsensitiveConverter;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.ParamsToConvert;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.Platform;

public class LocatorConvertingWebTest {

    private final CaseInsensitiveConverter converter = new CaseInsensitiveConverter(new ParamsToConvert(false, false, false, false), Platform.WEB);

    @Test
    public void convertIdToXpathTest() {
        By idLocator = By.id("some_id");
        By expectedRes = By.xpath(
                ".//*[@id='some_id']");

        By result = converter.convert(idLocator);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to xpath!");
    }

    @Test
    public void convertNameToXpathTest() {
        By nameLocator = By.name("some_name");
        By expectedRes = By.xpath(
                ".//*[@name='some_name']");

        By result = converter.convert(nameLocator);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to xpath!");
    }

    @Test
    public void convertLinkTextToXpathTest() {
        By nameLocator = By.linkText("some_link_text");
        By expectedRes = By.xpath(
                ".//a[text()='some_link_text']");

        By result = converter.convert(nameLocator);
        Assert.assertEquals(result, expectedRes, "Incorrect converting to xpath!");
    }
}
