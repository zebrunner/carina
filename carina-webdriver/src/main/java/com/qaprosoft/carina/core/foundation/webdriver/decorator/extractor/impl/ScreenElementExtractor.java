/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.decorator.extractor.impl;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.extractor.AbstractElementExtractor;

public class ScreenElementExtractor extends AbstractElementExtractor implements IDriverPool {

    @Override
    public ExtendedWebElement getElementsByCoordinates(int x, int y) {
        String elementName = String.format("Element founded by x:%d - y:%d", x, y);
        WebDriver driver = getDriver();
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
        return generateExtenedElement(result, elementName);
    }

}
