/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.alice.models.dto.RecognitionMetaType;
import com.qaprosoft.carina.core.foundation.webdriver.ai.FindByAI;
import com.qaprosoft.carina.core.foundation.webdriver.ai.Label;
import com.qaprosoft.carina.core.foundation.webdriver.ai.impl.AliceRecognition;

/**
 * The default element locator, which will lazily locate an element or an
 * element list on a page. This class is designed for use with the
 * {@link org.openqa.selenium.support.PageFactory} and understands the
 * annotations {@link org.openqa.selenium.support.FindBy} and
 * {@link org.openqa.selenium.support.CacheLookup}.
 */
public class ExtendedElementLocator implements ElementLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedElementLocator.class);

    private final SearchContext searchContext;
    private boolean shouldCache;
    private By by;
    private WebElement cachedElement;
    private List<WebElement> cachedElementList;

    private String aiCaption;
    private Label aiLabel;

    /**
     * Creates a new element locator.
     * 
     * @param searchContext The context to use when finding the element
     * @param field The field on the Page Object that will hold the located
     *            value
     */
    public ExtendedElementLocator(SearchContext searchContext, Field field) {
        this.searchContext = searchContext;

        if (field.isAnnotationPresent(FindBy.class)) {
            LocalizedAnnotations annotations = new LocalizedAnnotations(field);
            this.shouldCache = annotations.isLookupCached();
            this.by = annotations.buildBy();
        }
        // Elements to be recognized by Alice
        if (field.isAnnotationPresent(FindByAI.class)) {
            this.aiCaption = field.getAnnotation(FindByAI.class).caption();
            this.aiLabel = field.getAnnotation(FindByAI.class).label();
        }

    }

    /**
     * Find the element.
     */
    public WebElement findElement() {
        if (cachedElement != null && shouldCache) {
        	LOGGER.info("returning element from cache: " + by);
            return cachedElement;
        }

        WebElement element = null;
        List<WebElement> elements = null;
        NoSuchElementException exception = null;
        // Finding element using Selenium
        if (by != null) {
            try {
            	element = searchContext.findElement(by);
            } catch (StaleElementReferenceException | InvalidElementStateException e) {
            	element = ((RemoteWebElement) searchContext).getWrappedDriver().findElement(by);
            } catch (NoSuchElementException e) {
                exception = e;
            	//TODO: on iOS findElement return nothing but findElements return valid single item
            	// maybe migrate to the latest appium java driver
            	elements = searchContext.findElements(by);
            	if (!elements.isEmpty()) {
            		exception = null;
            		element = searchContext.findElements(by).get(0);
            	}
                LOGGER.info("Unable to find element: " + e.getMessage());
            }
        }
        
        // Finding element using AI tool
        if (element == null && AliceRecognition.INSTANCE.isEnabled()) {
            element = findElementByAI((WebDriver) searchContext, aiLabel, aiCaption);
        }
        // If no luck throw general NoSuchElementException
        if (element == null) {
            throw exception != null ? exception : new NoSuchElementException("Unable to find element by Selenium/AI");
        }

		// 1. enable cache for successfully discovered element to minimize selenium calls
        // 2. disable default caching to test iOS click issue 
		//shouldCache = true;
        if (shouldCache) {
            cachedElement = element;
        }
        return element;
    }

    /**
     * Find the element list.
     */
    public List<WebElement> findElements() {
        if (cachedElementList != null && shouldCache) {
        	LOGGER.info("returning element from cache: " + by);
            return cachedElementList;
        }

        List<WebElement> elements = null;
    	NoSuchElementException exception = null;
    	try {
    		elements = searchContext.findElements(by);
        } catch (StaleElementReferenceException | InvalidElementStateException e) {
        	elements = ((RemoteWebElement) searchContext).getWrappedDriver().findElements(by);
        } catch (NoSuchElementException e) {
            exception = e;
            LOGGER.info("Unable to find elements: " + e.getMessage());
        }
    	
    	//TODO: incorporate find by AI???
    	
        // If no luck throw general NoSuchElementException
        if (elements == null) {
            throw exception != null ? exception : new NoSuchElementException("Unable to find elements by Selenium");
        }

		// enable cache for successfully discovered element to minimize selenium calls
		shouldCache = true;
        if (shouldCache) {
            cachedElementList = elements;
        }

        return elements;
    }

    private WebElement findElementByAI(WebDriver drv, Label label, String caption) {
        WebElement element = null;
        File screen = ((TakesScreenshot) drv).getScreenshotAs(OutputType.FILE);
        RecognitionMetaType result = AliceRecognition.INSTANCE.recognize(aiLabel, aiCaption, screen);
        if (result != null) {
            int x = (result.getTopleft().getX() + result.getBottomright().getX()) / 2;
            int y = (result.getTopleft().getY() + result.getBottomright().getY()) / 2;
            element = (WebElement) ((JavascriptExecutor) drv).executeScript("return document.elementFromPoint(arguments[0], arguments[1])", x, y);
        } else {
            throw new NoSuchElementException("Unable to find element by AI label: " + aiLabel + ", caption: " + aiCaption);
        }
        return element;
    }

}
