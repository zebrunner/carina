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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.locator.ExtendedElementLocator;
import com.qaprosoft.carina.core.gui.AbstractUIObject;

public class AbstractUIObjectListHandler<T extends AbstractUIObject> implements InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final Class<?> clazz;
    private final WebDriver webDriver;
    private final ExtendedElementLocator locator;
    private final String name;
    private final ClassLoader classLoader;

    private final By locatorBy;

    public AbstractUIObjectListHandler(ClassLoader classLoader, Class<?> clazz, WebDriver webDriver, ElementLocator locator, String name) {
        this.classLoader = classLoader;
        this.clazz = clazz;
        this.webDriver = webDriver;
        this.locator = (ExtendedElementLocator) locator;
        this.name = name;
        this.locatorBy = this.locator.getBy();
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
        // waitUntil(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(locatorBy),
        // ExpectedConditions.visibilityOfElementLocated(locatorBy)));

        List<T> uIObjects = locator.findElements()
                .parallelStream()
                .map(element -> {
                    try {
                        T uiObject = (T) clazz.getConstructor(WebDriver.class, SearchContext.class)
                                .newInstance(webDriver, element);
                        InvocationHandler handler = new LocatingListsElementHandler(element, locator);
                        WebElement proxy = (WebElement) Proxy.newProxyInstance(classLoader,
                                new Class[] { WebElement.class, WrapsElement.class, Locatable.class },
                                handler);
                        uiObject.setRootElement(new ExtendedWebElement(proxy, null));
                        uiObject.setRootBy(locatorBy);
                        return uiObject;
                    } catch (NoSuchElementException | NoSuchMethodException | IllegalAccessException | InvocationTargetException
                            | InstantiationException e) {
                        LOGGER.error("Implement appropriate AbstractUIObject constructor for auto-initialization: " + e.getMessage(), e);
                        throw new RuntimeException("Implement appropriate AbstractUIObject constructor for auto-initialization: " + e.getMessage(),
                                e);
                    }
                })
                .collect(Collectors.toList());

        int index = 0;

        for (T uiObject : uIObjects) {
            boolean isLocalized = locator.isLocalized();
            uiObject.setName(isLocalized ? locator.getClassName() + "." + name + index++ : name + index++);
        }

        try {
            return method.invoke(uIObjects, objects);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
}