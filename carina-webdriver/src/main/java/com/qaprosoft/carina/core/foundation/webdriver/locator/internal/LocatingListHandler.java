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
package com.qaprosoft.carina.core.foundation.webdriver.locator.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.locator.LocalizedAnnotations;

public class LocatingListHandler<T extends ExtendedWebElement> implements InvocationHandler {
    private final ElementLocator locator;
    private String name;
    private By by;
    private final ClassLoader loader;
    private final Class<?> clazz;

    public LocatingListHandler(ClassLoader loader, ElementLocator locator, Field field, Class<?> clazz) {
        this.loader = loader;
        this.locator = locator;
        this.name = field.getName();
        this.by = new LocalizedAnnotations(field).buildBy();
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
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
        List<T> extendedWebElements = null;
        int i = 0;
        if (elements != null) {
            extendedWebElements = new ArrayList<T>();
            for (WebElement element : elements) {
                InvocationHandler handler = new LocatingListsElementHandler(element, locator);
                WebElement proxy = (WebElement) Proxy.newProxyInstance(loader, new Class[]{WebElement.class, WrapsElement.class, Locatable.class},
                        handler);
                T webElement;
                try {
                    webElement = (T) clazz.getConstructor(WebElement.class, String.class, By.class).newInstance(proxy,
                            name + i, by);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(
                            "Implement appropriate AbstractUIObject constructor for auto-initialization!", e);
                } catch (Exception e) {
                    throw new RuntimeException("Error creating ExtendedWebElement!", e);
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

}
