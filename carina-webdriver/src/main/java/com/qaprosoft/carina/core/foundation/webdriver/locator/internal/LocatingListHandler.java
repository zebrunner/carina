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
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.locator.ExtendedElementLocator;

public class LocatingListHandler implements InvocationHandler {
    private final ExtendedElementLocator locator;
    private final String name;
    private final ClassLoader loader;

    public LocatingListHandler(ClassLoader loader, ElementLocator locator, Field field){
        this.loader = loader;
        this.locator = (ExtendedElementLocator) locator;
        this.name = field.getName();
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
        List<ExtendedWebElement> extendedWebElements = elements.parallelStream()
                .map(element -> {
                    InvocationHandler handler = new LocatingListsElementHandler(element, locator);
                    WebElement proxy = (WebElement) Proxy.newProxyInstance(loader,
                            new Class[] { WebElement.class, WrapsElement.class, Locatable.class },
                            handler);
                    return new ExtendedWebElement(proxy, null);
                }).collect(Collectors.toList());
        int i = 0;
        boolean isLocalized = locator.isLocalized();
        for (ExtendedWebElement el : extendedWebElements) {
            el.setName(isLocalized ? locator.getClassName() + "." + name + i++ : name + i++);
        }

        try {
            return method.invoke(extendedWebElements, objects);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
