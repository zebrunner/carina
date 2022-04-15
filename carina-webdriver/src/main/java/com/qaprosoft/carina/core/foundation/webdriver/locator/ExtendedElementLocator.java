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
package com.qaprosoft.carina.core.foundation.webdriver.locator;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.CaseInsensitiveXPath;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.Localized;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private By by;
    private String className;
    
    private boolean caseInsensitive = false;
    private boolean localized = false;
    
    /**
     * Creates a new element locator.
     * 
     * @param searchContext The context to use when finding the element
     * @param field The field on the Page Object that will hold the located
     *            value
     */
    public ExtendedElementLocator(SearchContext searchContext, Field field) {
        this.searchContext = searchContext;
        String[] classPath = field.getDeclaringClass().toString().split("\\.");
        this.className = classPath[classPath.length-1];

        if (field.isAnnotationPresent(FindBy.class) || field.isAnnotationPresent(ExtendedFindBy.class)) {
            LocalizedAnnotations annotations = new LocalizedAnnotations(field);
            this.by = annotations.buildBy();
            if (field.isAnnotationPresent(CaseInsensitiveXPath.class)) {
                this.caseInsensitive = true;
            }
            if (field.isAnnotationPresent(Localized.class)) {
                this.localized = true;
            }
        }
    }

    /**
     * Find the element.
     */
    public WebElement findElement() {
        WebElement element = null;
        List<WebElement> elements = null;
        // Finding element using Selenium
        if (by != null) {
            // convert only locators that are xpath
            if (caseInsensitive && by.toString().matches("^By.xpath:.*?")) {
                by = toCaseInsensitive(by.toString());
            }

            //TODO: test how findElements work for web and android
            // maybe migrate to the latest appium java driver and reuse original findElement!
            elements = searchContext.findElements(by);
            if (elements.size() == 1) {
                element = elements.get(0);
            } else if (elements.size() > 1) {
                element = elements.get(0);
                LOGGER.debug(elements.size() + " elements detected by: " + by.toString());
            }
        }

        // If no luck throw general NoSuchElementException
        if (element == null) {
            throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + by);
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

        // If no luck throw general NoSuchElementException
        if (elements == null) {
            throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + by.toString());
        }

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
        String attributePattern = "(?<!(translate\\())((@text|text\\(\\)|@content-desc)\\s*(\\,|\\=)\\s*((['\"])((?:(?!\\6|\\\\).|\\\\.)*)\\6))";
        //TODO: test when xpath globally are declared inside single quota

        // double translate is needed to make xpath and value case insensitive.
        // For example on UI we have "Inscription", so with a single translate we must convert in page object all those values to lowercase
        // double translate allow to use as is and convert everywhere

        // Expected xpath for both side translate
        // *[translate(@text, '$U', '$l')=translate("Inscription", "inscription".UPPER, "inscription".LOWER)]

        Matcher matcher = Pattern.compile(attributePattern).matcher(xpath);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String attribute = matcher.group(3);            // @text or text()
            String value = matcher.group(7);                //  'some text' or "some text"
            String quote = matcher.group(6);                // ' or "
            String delimiter = matcher.group(4);            // , or =
            String replacement =
                    "translate(" + attribute + ", " + quote + value.toUpperCase() + quote + ", " + quote
                            + value.toLowerCase() + quote + ")" + delimiter
                            + "translate(" + quote + value + quote + ", " + quote + value.toUpperCase()
                            + quote + ", " + quote + value.toLowerCase() + quote
                            + ")";
            replacement = replacement.replaceAll("\\$", "\\\\\\$");
            LOGGER.debug(replacement);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    public boolean isLocalized() {
        return localized;
    }

    public String getClassName(){
        return className;
    }

}

