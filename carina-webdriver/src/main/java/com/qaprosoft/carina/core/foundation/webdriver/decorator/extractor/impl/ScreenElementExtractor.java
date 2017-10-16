package com.qaprosoft.carina.core.foundation.webdriver.decorator.extractor.impl;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.extractor.AbstractElementExtractor;

public class ScreenElementExtractor extends AbstractElementExtractor {

    @Override
    public ExtendedWebElement getElementsByCoordinates(int x, int y) {
        String elementName = String.format("Element founded by x:%d - y:%d", x, y);
        WebDriver driver = DriverPool.getDriver();
        List<WebElement> elements = getEndLevelElements(driver);
        List<WebElement> result = new ArrayList<WebElement>();
        Rectangle rect;
        for (WebElement webElement : elements) {
            try {
                rect = getRect(webElement);
            } catch (Exception e) {
                continue;
            }
            if (isInside(rect, x, y)) {
                result.add(webElement);
            }
        }
        return generateExtenedElement(result, driver, elementName);
    }

}
