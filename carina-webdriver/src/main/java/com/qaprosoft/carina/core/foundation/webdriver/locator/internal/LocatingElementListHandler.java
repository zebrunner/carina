/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.locator.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

public class LocatingElementListHandler implements InvocationHandler {
    private final ElementLocator locator;
    private String name;
    private By by;
    private final WebDriver driver;
    
    private static final Logger LOGGER = Logger.getLogger(LocatingElementListHandler.class);

    public LocatingElementListHandler(WebDriver driver, ElementLocator locator, String name, By by) {
    	this.driver = driver;
        this.locator = locator;
        this.name = name;
        this.by = by;
    }

    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
		// Hotfix for huge and expected regression in carina: we lost managed
		// time delays with lists manipulations
		// Temporary we are going to restore explicit waiter here with hardcoded
		// timeout before we find better solution
		// Pros: super fast regression issue which block UI execution
		// Cons: there is no way to manage timeouts in this places
//    	if (!waitUntil(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(by),
//    			ExpectedConditions.visibilityOfElementLocated(by)))) {
//    		LOGGER.error("List is not present: " + by);
//    	}

    	
    	List<WebElement> elements = null;
    	try {
    		elements = locator.findElements();
		} catch (StaleElementReferenceException | InvalidElementStateException e) {
			LOGGER.debug("catched StaleElementReferenceException: ", e);
			elements = driver.findElements(by);
		}
    	
        List<ExtendedWebElement> extendedWebElements = null;
        if (elements != null) {
            extendedWebElements = new ArrayList<ExtendedWebElement>();
            
            int i = 1;
			for (WebElement element : elements) {
				String tempName = name;
				try {
					tempName = element.getText();
				} catch (Exception e) {
					 //do nothing and keep 'undefined' for control name 
				}

				ExtendedWebElement tempElement = new ExtendedWebElement(element, tempName, by);
				Field searchContextField = locator.getClass().getDeclaredField("searchContext");
				searchContextField.setAccessible(true);
				tempElement.setSearchContext((SearchContext) searchContextField.get(locator));
//				tempElement.setBy(tempElement.generateByForList(by, i));
				extendedWebElements.add(tempElement);
				i++;
			}

        }

        try {
            return method.invoke(extendedWebElements, objects);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
