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

import java.lang.invoke.MethodHandles;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractUIObject;
import com.zebrunner.carina.webdriver.locator.LocatorType;
import com.zebrunner.carina.webdriver.locator.LocatorUtils;

public class AbstractUIObjectListHandler<T extends AbstractUIObject> implements InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ClassLoader loader;
    private Class<?> clazz;
    private WebDriver webDriver;
    private final ElementLocator locator;
    private String name;

    private By locatorBy;

    public AbstractUIObjectListHandler(ClassLoader loader, Class<?> clazz, WebDriver webDriver, ElementLocator locator, String name) {
        this.loader = loader;
        this.clazz = clazz;
        this.webDriver = webDriver;
        this.locator = locator;
        this.name = name;
        this.locatorBy = getLocatorBy(locator);
    }

    @SuppressWarnings("unchecked")
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
    	
		// Hotfix for huge and expected regression in carina: we lost managed
		// time delays with lists manipulations
		// Temporary we are going to restore explicit waiter here with hardcoded
		// timeout before we find better solution
		// Pros: super fast regression issue which block UI execution
		// Cons: there is no way to manage timeouts in this places

        // #1458: AbstractUIObjectListHandler waitUntil pause
//    	waitUntil(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(locatorBy),
//    			ExpectedConditions.visibilityOfElementLocated(locatorBy)));

    	List<WebElement> elements = locator.findElements();
        Optional<LocatorType> locatorType = LocatorUtils.getLocatorType(locatorBy);
        boolean isByForListSupported = locatorType.isPresent() && locatorType.get().isIndexSupport();
        String locatorAsString = locatorBy.toString();
        List<T> uIObjects = new ArrayList<T>();
        int index = 0;
        if (elements != null) {
            for (WebElement element : elements) {
                T uiObject;
                try {
                    uiObject = (T) clazz.getConstructor(WebDriver.class, SearchContext.class)
                            .newInstance(
                                    webDriver, element);
                } catch (NoSuchMethodException e) {
                    LOGGER.error("Implement appropriate AbstractUIObject constructor for auto-initialization: "
                            + e.getMessage());
                    throw new RuntimeException(
                            "Implement appropriate AbstractUIObject constructor for auto-initialization: "
                                    + e.getMessage(),
                            e);
                }

                InvocationHandler handler = new LocatingListsElementHandler(element, locator);
                WebElement proxy = (WebElement) Proxy.newProxyInstance(loader,
                        new Class[] { WebElement.class, WrapsElement.class, WrapsDriver.class, Locatable.class, TakesScreenshot.class },
                        handler);
                ExtendedWebElement webElement = new ExtendedWebElement(proxy, String.format("%s - %d", name, index), locatorBy);
                webElement.setIsSingle(false);
                if (isByForListSupported) {
                    webElement.setIsRefreshSupport(true);
                    webElement.setBy(locatorType.get().buildLocatorWithIndex(locatorAsString, index));
                } else {
                    webElement.setIsRefreshSupport(false);
                }
                uiObject.setRootExtendedElement(webElement);
                uiObject.setName(String.format("%s - %d", name, index));
                uiObject.setRootElement(element);
                uiObject.setRootBy(locatorBy);
                uIObjects.add(uiObject);
                index++;
            }
        }

        try {
            return method.invoke(uIObjects, objects);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    private By getLocatorBy(ElementLocator locator) {
    	By rootBy = null;
    	
        //TODO: get root by annotation from ElementLocator to be able to append by for those elements and reuse fluent waits
		try {
			Field byContextField = null;

			byContextField = locator.getClass().getDeclaredField("by");
			byContextField.setAccessible(true);
			rootBy = (By) byContextField.get(locator);

		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (Throwable thr) {
			thr.printStackTrace();
			LOGGER.error("Unable to get rootBy via reflection!", thr);
		}
    	
    	return rootBy;
    }
    
}