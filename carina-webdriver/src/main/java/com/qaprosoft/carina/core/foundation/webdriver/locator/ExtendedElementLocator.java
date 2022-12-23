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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.CaseInsensitiveXPath;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.Localized;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.LocalizedLocatorConverter;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.LocatorConverter;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.CaseInsensitiveConverter;
import com.zebrunner.carina.utils.commons.SpecialKeywords;

import io.appium.java_client.remote.MobileCapabilityType;

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
    // original locator without any transformations
    private By originalBy;
    // final locator obtained after transformations
    private By by;
    private String className;
    private boolean localized = false;
    // all converters that apply to the current element's locator
    private final LinkedList<LocatorConverter> converters = new LinkedList<>();
    
    /**
     * Creates a new element locator.
     *
     * @param driver see {@link WebDriver}
     * @param searchContext The context to use when finding the element
     * @param field The field on the Page Object that will hold the located
     *            value
     */
    public ExtendedElementLocator(WebDriver driver, SearchContext searchContext, Field field) {
        this.searchContext = searchContext;
        String[] classPath = field.getDeclaringClass().toString().split("\\.");
        this.className = classPath[classPath.length-1];

        if (field.isAnnotationPresent(FindBy.class) || field.isAnnotationPresent(ExtendedFindBy.class)) {
            DefaultAnnotations annotations = new DefaultAnnotations(field);
            this.originalBy = annotations.buildBy();
            this.by = this.originalBy;

            converters.add(new LocalizedLocatorConverter(field));

            if (field.isAnnotationPresent(CaseInsensitiveXPath.class)) {
                boolean isMobile = ((HasCapabilities) driver).getCapabilities()
                        .getCapability(MobileCapabilityType.APP) != null;
                converters.add(new CaseInsensitiveConverter(field.getAnnotation(CaseInsensitiveXPath.class), isMobile));
            }

            if (field.isAnnotationPresent(Localized.class)) {
                this.localized = true;
            }
            buildBy();
        }
    }

    /**
     * Get locator converters
     * 
     * @return {@link LinkedList} of {@link LocatorConverter}
     */
    public LinkedList<LocatorConverter> getLocatorConverters() {
        return this.converters;
    }

    /**
     * Build (rebuild) current locator, applying consistently all {@link LocatorConverter}, listed in {@link #converters}
     */
    public void buildBy() {
        String cloneBy = this.originalBy.toString();
        for (LocatorConverter converter : converters) {
            cloneBy = converter.convert(cloneBy);
        }
        this.by = createBy(cloneBy);
    }

    /**
     * Find the element.
     */
    public WebElement findElement() {

        if (by == null) {
            throw new NullPointerException("By cannot be null");
        }

        //TODO: test how findElements work for web and android
        // maybe migrate to the latest appium java driver and reuse original findElement!
        List<WebElement> elements = searchContext.findElements(by);

        WebElement element = null;
        if (elements.size() == 1) {
            element = elements.get(0);
        } else if (elements.size() > 1) {
            element = elements.get(0);
            LOGGER.debug(elements.size() + " elements detected by: " + by.toString());
        }

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

        if (elements == null) {
            throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + by.toString());
        }

        return elements;
    }

    public SearchContext getSearchContext() {
        return this.searchContext;
    }

    public boolean isLocalized() {
        return this.localized;
    }

    public By getBy() {
        return this.by;
    }

    public String getClassName() {
        return className;
    }

    private By createBy(String locator) {
        return Arrays.stream(LocatorType.values())
                .filter(lt -> lt.is(locator))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Unable to generate By using locator: '%s'!", locator)))
                .buildLocatorFromString(locator);
    }
}
