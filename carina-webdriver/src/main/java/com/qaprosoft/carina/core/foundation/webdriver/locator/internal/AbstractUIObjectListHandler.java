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
package com.qaprosoft.carina.core.foundation.webdriver.locator.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedFieldDecorator;
import com.qaprosoft.carina.core.gui.AbstractUIObject;

public class AbstractUIObjectListHandler<T extends AbstractUIObject> implements InvocationHandler {
    private Class<?> clazz;
    private WebDriver webDriver;
    private final ElementLocator locator;
    private String name;

    private By locatorBy;
    private Logger LOGGER = Logger.getLogger(ExtendedFieldDecorator.class);

    public AbstractUIObjectListHandler(Class<?> clazz, WebDriver webDriver, ElementLocator locator, String name) {
        this.clazz = clazz;
        this.webDriver = webDriver;
        this.locator = locator;
        this.name = name;
        this.locatorBy = getLocatorBy(locator);
    }

    @SuppressWarnings("unchecked")
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {

        List<WebElement> elements = locator.findElements();
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
                uiObject.setName(String.format("%s - %d", name, index++));
                uiObject.setRootElement(element);
                uiObject.setRootBy(locatorBy);
                uIObjects.add(uiObject);
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