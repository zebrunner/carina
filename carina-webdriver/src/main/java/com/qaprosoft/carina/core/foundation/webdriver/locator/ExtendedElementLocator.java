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
package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.CaseInsensitiveXPath;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.DisableCacheLookup;

/**
 * The default element locator, which will lazily locate an element or an
 * element list on a page. This class is designed for use with the
 * {@link org.openqa.selenium.support.PageFactory} and understands the
 * annotations {@link org.openqa.selenium.support.FindBy} and
 * {@link org.openqa.selenium.support.CacheLookup}.
 */
public class ExtendedElementLocator implements ElementLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SearchContext searchContext;
    private boolean shouldCache;
    private boolean caseInsensitive;
    private By by;
    private WebElement cachedElement;

    /**
     * Creates a new element locator.
     * 
     * @param searchContext The context to use when finding the element
     * @param field The field on the Page Object that will hold the located
     *            value
     */
    public ExtendedElementLocator(SearchContext searchContext, Field field) {
        this.searchContext = searchContext;

        if (field.isAnnotationPresent(FindBy.class) || field.isAnnotationPresent(ExtendedFindBy.class)) {
            LocalizedAnnotations annotations = new LocalizedAnnotations(field);
            this.shouldCache = true;
            this.caseInsensitive = false;
            this.by = annotations.buildBy();
            if (field.isAnnotationPresent(DisableCacheLookup.class)) {
                this.shouldCache = false;
            }
            if (field.isAnnotationPresent(CaseInsensitiveXPath.class)) {
                this.caseInsensitive = true;
            }
        }

    }

    /**
     * Find the element.
     */
    public WebElement findElement() {
        if (cachedElement != null && shouldCache) {
            return cachedElement;
        }

        WebElement element = null;
        List<WebElement> elements = null;
        NoSuchElementException exception = null;
        // Finding element using Selenium
        if (by != null) {
            if (caseInsensitive && !by.toString().contains("translate(")) {
                by = toCaseInsensitive(by.toString());
            }
            try {
                element = searchContext.findElement(by);
            } catch (NoSuchElementException e) {
                exception = e;
                //TODO: on iOS findElement return nothing but findElements return valid single item
                // maybe migrate to the latest appium java driver
                elements = searchContext.findElements(by);
                if (!elements.isEmpty()) {
                    exception = null;
                    element = elements.get(0);
                }
                // hide below debug message as it is to often displayed in logs due to the fluent waits etc
                //LOGGER.debug("Unable to find element: " + e.getMessage());
            }
        }
        
        // If no luck throw general NoSuchElementException
        if (element == null) {
            throw exception != null ? exception : new NoSuchElementException("Unable to find element by Selenium/AI");
        }

        // 1. enable cache for successfully discovered element to minimize selenium calls
        if (shouldCache) {
            cachedElement = element;
        }
        return element;
    }

    /**
     * Find the element list.
     */
    public List<WebElement> findElements() {
        List<WebElement> elements = null;

        try {
            elements = searchContext.findElements(by);
        } catch (NoSuchElementException e) {
            LOGGER.debug("Unable to find elements: " + e.getMessage());
        }

        //TODO: incorporate find by AI???
        
        // If no luck throw general NoSuchElementException
        if (elements == null) {
            throw new NoSuchElementException("Unable to find elements by Selenium");
        }

        // we can't enable cache for lists by default as we can't handle/catch list.get(index).action(). And for all dynamic lists
        // As result for all dynamic lists we have too often out of bound index exceptions

        return elements;
    }
    
    /**
     * Transform XPath locator to case insensitive
     * 
     * @param locator - locator as a String
     * @return By
     */
    public static By toCaseInsensitive(String locator) {
        String xpath = StringUtils.remove(locator, "By.xpath: ");
        String attributePattern = "((@text|text\\(\\)|@content-desc)\\s*(\\,|\\=)\\s*(\\'|\\\")(.+?)(\\'|\\\")(\\)(\\s*\\bor\\b\\s*)?|\\]|\\)\\]))";
        //TODO: test when xpath globally are declared inside single quota
        
        // @text of text() - group(2)
        // , or = - group(3)
        // ' or " - group(4)
        // value - group(5)
        // ' or " - group(6)
        // ] or ) - group(7)
        
        // double translate is needed to make xpath and value case insensitive.
        // For example on UI we have "Inscription", so with a single translate we must convert in page object all those values to lowercase
        // double translate allow to use as is and convert everywhere
        
        // Expected xpath for both side translate
        // *[translate(@text, '$U', '$l')=translate("Inscription", "inscription".UPPER, "inscription".LOWER)]
        
        Matcher matcher = Pattern.compile(attributePattern).matcher(xpath);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String value = matcher.group(5);
            String replacement = "translate(" + matcher.group(2) + ", " + matcher.group(4) + value.toUpperCase() + matcher.group(4) + ", " + matcher.group(4) + value.toLowerCase() + matcher.group(4) + ")" + matcher.group(3)
                    + "translate(" + matcher.group(4) + value + matcher.group(4)+ ", " + matcher.group(4) + value.toUpperCase() + matcher.group(4) + ", " + matcher.group(4) + value.toLowerCase() + matcher.group(6)
                    + ")" + matcher.group(7);
            LOGGER.debug(replacement);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    public void setShouldCache(boolean shouldCache) {
        this.shouldCache = shouldCache;
    }

}
