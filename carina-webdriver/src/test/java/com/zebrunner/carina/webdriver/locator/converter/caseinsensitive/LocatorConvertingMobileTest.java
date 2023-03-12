package com.zebrunner.carina.webdriver.locator.converter.caseinsensitive;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LocatorConvertingMobileTest {

    private final CaseInsensitiveConverter converter = new CaseInsensitiveConverter(true)
            .setId(false)
            .setName(false)
            .setText(false)
            .setClassAttr(false);

    @Test
    public void convertIdToXpathTest() {
        String idLocator = By.id("some_id")
                .toString();
        String expectedRes = By.xpath("//*[ends-with(@resource-id, ':id/some_id')]")
                .toString();
        Assert.assertEquals(converter.convert(idLocator), expectedRes, "Incorrect converting to xpath!");
    }

    @Test
    public void convertNameToXpathTest() {
        String nameLocator = By.name("some_name")
                .toString();
        String expectedRes = By.xpath("//*[@name='some_name']")
                .toString();
        Assert.assertEquals(converter.convert(nameLocator), expectedRes, "Incorrect converting to xpath!");
    }

    @Test
    public void convertLinkTextToXpathTest() {
        String nameLocator = By.linkText("some_link_text")
                .toString();
        String expectedRes = By.xpath("//a[text()='some_link_text']")
                .toString();
        Assert.assertEquals(converter.convert(nameLocator), expectedRes, "Incorrect converting to xpath!");
    }
}
