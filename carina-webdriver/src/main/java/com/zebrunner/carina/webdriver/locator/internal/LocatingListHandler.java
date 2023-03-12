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
package com.zebrunner.carina.webdriver.locator.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.locator.ExtendedElementLocator;
import com.zebrunner.carina.webdriver.locator.LocatorType;
import com.zebrunner.carina.webdriver.locator.LocatorUtils;

public class LocatingListHandler implements InvocationHandler {
    private final ElementLocator locator;
    private String name;
    private final ClassLoader loader;

    public LocatingListHandler(ClassLoader loader, ElementLocator locator, Field field){
        this.loader = loader;
        this.locator = locator;
        this.name = field.getName();
    }

    public LocatingListHandler(ClassLoader loader, ElementLocator locator, String name) {
        this.loader = loader;
        this.locator = locator;
        this.name = name;
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

    	
    	List<WebElement> elements = locator.findElements();
        By by = getLocatorBy(locator);
        Optional<LocatorType> locatorType = LocatorUtils.getLocatorType(by);
        boolean isByForListSupported = locatorType.isPresent() && locatorType.get().isIndexSupport();
        String locatorAsString = by.toString();
        List<ExtendedWebElement> extendedWebElements = null;
        int i = 0;
        if (elements != null) {
            extendedWebElements = new ArrayList<ExtendedWebElement>();
            for (WebElement element : elements) {
                InvocationHandler handler = new LocatingListsElementHandler(element, locator);
                WebElement proxy = (WebElement) Proxy.newProxyInstance(loader,
                        new Class[] { WebElement.class, WrapsElement.class, WrapsDriver.class, Locatable.class, TakesScreenshot.class },
                        handler);
                ExtendedWebElement webElement = new ExtendedWebElement(proxy, name + i);
                webElement.setIsSingle(false);
                if (isByForListSupported) {
                    webElement.setIsRefreshSupport(true);
                    webElement.setBy(locatorType.get().buildLocatorWithIndex(locatorAsString, i));
                } else {
                    webElement.setIsRefreshSupport(false);
                }
                Field searchContextField = locator.getClass().getDeclaredField("searchContext");
                searchContextField.setAccessible(true);
                webElement.setSearchContext((SearchContext) searchContextField.get(locator));
                extendedWebElements.add(webElement);
                i++;
            }
        }

        try {
            return method.invoke(extendedWebElements, objects);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private By getLocatorBy(ElementLocator locator) {
        try {
            ExtendedElementLocator extendedElementLocator = (ExtendedElementLocator) locator;
            return extendedElementLocator.getBy();
        } catch (ClassCastException e) {
            throw new RuntimeException("Cannot get by from locator", e);
        }
    }

}
